package localserver;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

public class ExternalCall {

    private static final int QUEUE_SIZE_DEFAULT = 128;
    private static HashMap<Long, CompletableFuture<BigInteger>> awaitingPublicBNEntries;
    private static HashMap<Long, byte[]> secretKeys;
    private BlockingQueue<String> sendQueue, receiveQueue;


    public ExternalCall() {
        this(QUEUE_SIZE_DEFAULT);
    }

    public ExternalCall(int queueSize) {
        sendQueue = new LinkedBlockingQueue<>(queueSize);
        receiveQueue = new LinkedBlockingQueue<>(queueSize);
    }

    static {
        awaitingPublicBNEntries = new HashMap<>();
        secretKeys = new HashMap<>();
    }

    void open() throws IOException {
        CentralServerSession serverSession = new CentralServerSession();
        serverSession.open();
        String[] env = serverSession.callAndResponse("dhgen");
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
        serverSession.sendOnly("dhreq", token.toString(), publicAG.toString());

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
        sendQueue.add(message);
        CentralServerSession session = new CentralServerSession();
//        session.sendOnly("msg", )
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

    static void setSecretKey(Long token, byte[] privateKey) {
        secretKeys.put(token, privateKey);
    }

    static byte[] getSecretKey(Long token) {
        return secretKeys.get(token);
    }

}
