package localserver;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
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
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.*;

public class ExternalCall {

    static HashMap<Long, ExternalCall> activeCalls = new HashMap<>();

    static final int QUEUE_SIZE_DEFAULT = 128;
    private static final int IV_LENGTH = 16;
    private static final long SESSION_TIMEOUT_MILLIS = 0;
    private CompletableFuture<BigInteger> awaitingPublicBN;
    private SecretKeySpec secretKey;
    private String target;
    private BlockingQueue<SendMessage> sendQueue;
    private BlockingQueue<String> receiveQueue;
    private Long token;


    public ExternalCall(String target) {
        this(target, QUEUE_SIZE_DEFAULT);
    }

    public ExternalCall(String target, int queueSize) {
        this.target = target;
        sendQueue = new LinkedBlockingQueue<>(queueSize);
        receiveQueue = new LinkedBlockingQueue<>(queueSize);
        awaitingPublicBN = new CompletableFuture<>();

        Runnable sendIfAvailable = () -> {
            while (true) {
                CentralServerSession session = null;
                try {
                    SendMessage toSend = sendQueue.take();
                    String msg = toSend.message;
                    session = new CentralServerSession();
                    session.open();
                    session.sendOnly("message", target, Long.toString(token), toSend.command ? "decode_cmd" : "decode_msg", Base64.getEncoder().encodeToString(msg.getBytes(StandardCharsets.UTF_8)));
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread executeSend = new Thread(sendIfAvailable);
        executeSend.setName("Send Queue Thread");
        executeSend.start();
        //don't call get on the future because the thread's function never returns.
    }

    //local constructor for if we didn't initiate the conversation (we are Bob).
    ExternalCall(String target, int queueSize, Long token, BigInteger publicAG) {
        this(target, queueSize);
        this.token = token;
        activeCalls.put(token, this);
        awaitingPublicBN.complete(publicAG);
    }

    private String encrypt(String message) {

        byte[] input = message.getBytes(StandardCharsets.UTF_8);

        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
            byte[] encrypted = new byte[cipher.getOutputSize(input.length)];
            int enc_len = cipher.update(input, 0, input.length, encrypted, 0);
            enc_len += cipher.doFinal(encrypted, enc_len);

            //encoded string format: <nonce length> + ' ' + <nonce> + <encrypted message>
            String ivStr = Base64.getEncoder().encodeToString(iv);
            return ivStr.length() + " " + ivStr + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decode(Long token, String encodedMsg) {
        //decode outer Base64 from network transfer
        encodedMsg = new String(Base64.getDecoder().decode(encodedMsg));
        //encoded string format: <nonce length> + ' ' + <nonce> + <encrypted message>

        //get the length of the initialization vector
        int endNonceLen = encodedMsg.indexOf(" ");
        int ivLength = Integer.parseInt(encodedMsg.substring(0, endNonceLen));

        //read initialization vector (nonce) from beginning of message
        byte[] initializationVector = Base64.getDecoder().decode(encodedMsg.substring(endNonceLen + 1, endNonceLen + 1 + ivLength));
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector);

        //partition out the actual message
        String strEncrypted = encodedMsg.substring(endNonceLen + 1 + ivLength);
        byte[] encrypted = Base64.getDecoder().decode(strEncrypted);
        int enc_len = encrypted.length;

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(token), ivParameterSpec);
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
        BigInteger n = new BigInteger(env[0]);
        BigInteger g = new BigInteger(env[1]);
        token = Long.parseLong(env[2]);

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

        activeCalls.put(token, this);

        BigInteger publicAG = ((DHPublicKey) pub).getY();
        serverSession.sendOnly("dhreq", target, token.toString(), publicAG.toString(), Boolean.toString(true));
        serverSession.close();

        //wait for the other party to send their public key
        BigInteger publicBG = null;
        try {
            publicBG = awaitingPublicBN.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        try {
            KeyFactory kf = KeyFactory.getInstance("DiffieHellman");
            PublicKey publicBNKey = kf.generatePublic(new DHPublicKeySpec(publicBG, n, g));
            ka.doPhase(publicBNKey, true);
            byte[] privateKey = ka.generateSecret();
            SecretKeySpec key = new SecretKeySpec(privateKey, 0, 16, "AES");
          //  System.out.println("Alice's secret key: " + Arrays.toString(privateKey));
            secretKey = key;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            e.printStackTrace();
        }


    }

    void sendMessage(String message) throws IOException {
        send(message, false);
    }

    private void send(String message, boolean command) {
        String encrypted = encrypt(message);
        if (encrypted == null)
            return;
        //handled by thread above, will be sent "at some point".
        sendQueue.add(new SendMessage(encrypt(message), command));
    }


    String receive() {
        try {
            return receiveQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void satisfyOtherPartyPublicKey(Long token, WhoAmI whoAmI, BigInteger key) {
        getActiveInstance(token, whoAmI).awaitingPublicBN.complete(key);
    }

    static void setSecretKey(Long token, WhoAmI whoAmI, SecretKeySpec privateKey) {
        getActiveInstance(token, whoAmI).secretKey = privateKey;
    }

    private static SecretKeySpec getSecretKey(long token) {
        ExternalCall activeInstanceA = getActiveInstance(token, WhoAmI.ALICE);
        if (activeInstanceA != null) {
            return activeInstanceA.secretKey;
        }
        ExternalCall activeInstanceB = getActiveInstance(token, WhoAmI.BOB);
        if (activeInstanceB != null) {
            return activeInstanceB.secretKey;
        }
        return null;
    }

    void receiveEnqueue(String message) {
        receiveQueue.add(message);
    }

    private static ExternalCall getActiveInstance(Long token, WhoAmI whoAmI) {
        if (whoAmI == WhoAmI.ALICE) {
            return activeCalls.get(token);
        } else if (whoAmI == WhoAmI.BOB) {
            return activeCalls.get(token);
        }
        throw new IllegalStateException("not alice and not bob");
    }

    //called by ExternalListener to execute commands received by another domain server
    String command(String command, Long token) {
        String[] arguments = command.split("\\s+");
        Command cmd = Command.getAsType(arguments[0], arguments, token);
        try {
            return cmd.execute();
        } catch (WrongArgumentTypeException e) {
            return "error : wrong argument type exception occurred";
        } catch (SQLException e) {
            return "error : database exception occurred";
        }
    }

    public void sendCommand(String command) {
        send(command, true);
    }

    public enum WhoAmI {
        ALICE, BOB
    }

    private class SendMessage {
        String message;
        boolean command;

        public SendMessage(String message, boolean command) {
            this.message = message;
            this.command = command;
        }
    }
}
