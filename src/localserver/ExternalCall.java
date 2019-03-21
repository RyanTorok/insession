package localserver;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.*;

public class ExternalCall {

    private static final int QUEUE_SIZE_DEFAULT = 128;
    private static final int IV_LENGTH = 32;
    private static final long SESSION_TIMEOUT_MILLIS = 0;
    private static HashMap<Long, CompletableFuture<BigInteger>> awaitingPublicBNEntries;
    private static HashMap<Long, SecretKey> secretKeys;
    private String target;
    private BlockingQueue<String> sendQueue;
    private BlockingQueue<String> receiveQueue;
    private Long token;


    public ExternalCall(String target) {
        this(target, QUEUE_SIZE_DEFAULT);
    }

    public ExternalCall(String target, int queueSize) {
        this.target = target;
        sendQueue = new LinkedBlockingQueue<>(queueSize);
        receiveQueue = new LinkedBlockingQueue<>(queueSize);

        Runnable sendIfAvailable = () -> {
            CentralServerSession session = null;
            try {
                session = new CentralServerSession();
                session.open();
                session.sendOnly("message", target, "decode", sendQueue.poll());
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ExecutorService execution = Executors.newSingleThreadExecutor();
        Future<?> submit = execution.submit(sendIfAvailable);
        try {
            submit.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    //local constructor for if we didn't initiate the conversation (we are Bob).
    ExternalCall(String target, int queueSize, Long token, BigInteger publicAG) {
        this(target, queueSize);
        this.token = token;
        satisfyOtherPartyPublicKey(token, publicAG);
    }

    static {
        awaitingPublicBNEntries = new HashMap<>();
        secretKeys = new HashMap<>();
    }

    private String encrypt(String message) {

        byte[] input = message.getBytes(StandardCharsets.UTF_8);

        SecretKey privateKey = getSecretKey(token);
        SecretKeySpec secretKeySpec = new SecretKeySpec(privateKey.getEncoded(), "AES");

        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = new byte[cipher.getOutputSize(input.length)];
            int enc_len = cipher.update(input, 0, input.length, encrypted, 0);
            enc_len += cipher.doFinal(encrypted, enc_len);

            //encoded string format: <nonce length> + ' ' + <nonce> + <encrypted message>
            return IV_LENGTH + " " + new String(iv, StandardCharsets.UTF_8) + new String(encrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decode(Long token, String encodedMsg) {
        //encoded string format: <nonce length> + ' ' + <nonce> + <encrypted message>
        SecretKey privateKey = getSecretKey(token);
        SecretKeySpec secretKeySpec = new SecretKeySpec(privateKey.getEncoded(), "AES");

        //get the length of the initialization vector
        int endNonceLen = encodedMsg.indexOf(" ");
        int ivLength = Integer.parseInt(encodedMsg.substring(0, endNonceLen));

        //read initialization vector (nonce) from beginning of message
        byte[] initializationVector = encodedMsg.substring(endNonceLen + 1, endNonceLen + 1 + ivLength).getBytes(StandardCharsets.UTF_8);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);

        //partition out the actual message
        String strEncrypted = encodedMsg.substring(endNonceLen + 1 + ivLength);
        int enc_len = strEncrypted.length();
        byte[] encrypted = strEncrypted.getBytes(StandardCharsets.UTF_8);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decrypted = new byte[cipher.getOutputSize(enc_len)];
            int dec_len = cipher.update(encrypted, 0, enc_len, decrypted, 0);
            dec_len += cipher.doFinal(decrypted, dec_len);
            CharBuffer decode = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(decrypted));
            return decode.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void open() throws IOException {
        CentralServerSession serverSession = new CentralServerSession();
        boolean open = serverSession.open();
        if (!open) throw new IOException("failed to open central server session");
        String[] env = serverSession.callAndResponse("dhgen");
        System.out.println(Arrays.toString(env));
        BigInteger n = new BigInteger(env[0]);
        BigInteger g = new BigInteger(env[1]);
        Long token = Long.parseLong(env[2]);

        KeyAgreement ka;
        PublicKey pub;
        try {
            DHParameterSpec params = new DHParameterSpec(n, g);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DiffieHellman");
            kpg.initialize(params);
            KeyPair kp = kpg.generateKeyPair();
            ka = KeyAgreement.getInstance("DiffieHellman");
            ka.init(kp.getPrivate());
            pub = kp.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        BigInteger publicAG = ((DHPublicKey) pub).getY();
        awaitingPublicBNEntries.put(token, new CompletableFuture<>());
        serverSession.sendOnly("dhreq", target, token.toString(), publicAG.toString());
        serverSession.close();

        //wait for the other party to send their public key
        BigInteger publicBG = null;
        try {
            publicBG = awaitingPublicBNEntries.get(token).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        try {
            KeyFactory kf = KeyFactory.getInstance("DiffieHellman");
            PublicKey publicBNKey = kf.generatePublic(new DHPublicKeySpec(publicBG, n, g));
            ka.doPhase(publicBNKey, true);
            byte[] privateKey = ka.generateSecret();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    void send(String message) throws IOException {
        String encrypted = encrypt(message);
        if (encrypted == null)
            return;
        //handled by thread above, will be sent "at some point".
        sendQueue.add(encrypted);
    }


    String receive() {
        return receiveQueue.poll();
    }

    static boolean isSendBack(Long token) {
        return awaitingPublicBNEntries.containsKey(token);
    }

    static void satisfyOtherPartyPublicKey(Long token, BigInteger key) {
        awaitingPublicBNEntries.computeIfAbsent(token, k -> new CompletableFuture<>()).complete(key);
    }

    static void setSecretKey(Long token, SecretKey privateKey) {
        secretKeys.put(token, privateKey);
    }

    static SecretKey getSecretKey(Long token) {
        return secretKeys.get(token);
    }

    void receiveEnqueue(String message) {
        receiveQueue.add(message);
    }
}
