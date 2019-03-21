package localserver;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class ExternalListener implements Runnable {

    @Override
    public void run() {
        try (PollingSocket socket = new PollingSocket()) {
            while (true) {
                String[] cmd = socket.poll();
                if (cmd == null)
                    continue;
                Long token = Long.parseLong(cmd[0]);
                String source = cmd[1];
                String opcode = cmd[2];
                switch (opcode) {
                    case "dhreq":
                        try {
                            BigInteger n = new BigInteger(cmd[3]);
                            BigInteger g = new BigInteger(cmd[4]);
                            BigInteger publicBG = new BigInteger(cmd[5]);

                            boolean sendBack = ExternalCall.isSendBack(token);
                            //Generate ag public key
                            PublicKey pub;
                            KeyAgreement ka;
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
                                break;
                            }
                            //public AG is only used if we're Bob; otherwise just used to set up the KeyAgreement the same way
                            BigInteger publicAG = ((DHPublicKey) pub).getY();
                            KeyFactory kf = KeyFactory.getInstance("DiffieHellman");
                            PublicKey publicBGKey = kf.generatePublic(new DHPublicKeySpec(publicBG, n, g));
                            ka.doPhase(publicBGKey, true);

                            //store our secret key
                            ExternalCall.setSecretKey(token, ka.generateSecret("AES"));

                            //allow the code in ExternallCall.open() to advance
                            ExternalCall.satisfyOtherPartyPublicKey(token, publicBG);

                            //send back our public key to Alice if we're Bob
                            if (sendBack) {
                                CentralServerSession session = new CentralServerSession();
                                session.open();
                                session.sendOnly("dhreq", source, token.toString(), publicAG.toString());
                                session.close();
                            }
                        } catch (NumberFormatException e) {
                            break;
                        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
                            e.printStackTrace();
                            break;
                        }
                    case "decode":
                        String encodedMsg = cmd[3];
                        try {
                            String decodedMsg = decode(token, encodedMsg);
                            if (decodedMsg == null)
                                throw new IllegalStateException("null command after decode");
                            External.receiveEnqueue(token, decodedMsg);
                            break;
                        } catch (NumberFormatException e) {
                            break;
                        }
                    case "connectiontest":
                        CentralServerSession session = new CentralServerSession();
                        session.open();
                        session.sendOnly("message", source, token.toString(), "connectiontestsuccess");
                        break;
                    case "connectiontestsuccess":
                        External.receiveEnqueue(token, "success");
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String decode(Long token, String encodedMsg) {
        String decoded = ExternalCall.decode(token, encodedMsg);
        if (decoded == null)
            return null;
        return decoded;
    }
}
