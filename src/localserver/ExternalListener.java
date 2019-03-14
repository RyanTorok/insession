package localserver;

import server.Extern;

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
                switch (cmd[0]) {
                    case "dhreq":
                        try {
                            Long token = Long.parseLong(cmd[1]);
                            BigInteger n = new BigInteger(cmd[2]);
                            BigInteger g = new BigInteger(cmd[3]);
                            BigInteger publicBG = new BigInteger(cmd[4]);
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
                            ExternalCall.setSecretKey(token, ka.generateSecret());

                            //allow the code in ExternallCall.open() to advance
                            ExternalCall.satisfyOtherPartyPublicKey(token, publicBG);

                            //send back our public key if we're Bob
                            if (sendBack) {
                                CentralServerSession session = new CentralServerSession();
                                session.sendOnly("dhreq", token.toString(), n.toString(), g.toString(), publicAG.toString());
                            }
                        } catch (NumberFormatException e) {
                            break;
                        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
                            e.printStackTrace();
                            break;
                        }
                    case "decode":
                        String token = cmd[1];
                        String encodedMsg = cmd[2];
                        try {
                            String[] decodedMsg = decode(token, ExternalCall.getSecretKey(Long.parseLong(token)));
                            
                        } catch (NumberFormatException e) {
                            break;
                        }
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] decode(String token, byte[] secretKey) {
        return null;
    }
}
