package localserver;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
                            boolean sendBack = Boolean.valueOf(cmd[6]);
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
                            byte[] sharedSecret = ka.generateSecret();
                            SecretKeySpec key = new SecretKeySpec(sharedSecret, 0, 16, "AES");

                            //create new external call instance for bob
                            if (sendBack) {
                                ExternalCall.activeCalls.put(token, new ExternalCall(source, ExternalCall.QUEUE_SIZE_DEFAULT, token, publicBG));
                                ExternalCall.setSecretKey(token, sendBack ? ExternalCall.WhoAmI.BOB : ExternalCall.WhoAmI.ALICE, key);

                                //System.out.println("Bob's   secret key: " + Arrays.toString(sharedSecret));

                                //send back our public key to Alice if we're Bob
                                CentralServerSession session = new CentralServerSession();
                                session.open();
                                session.sendOnly("dhreq", source, token.toString(), publicAG.toString(), Boolean.toString(false));
                                session.close();
                            }

                            //allow the code in ExternallCall.open() to advance. We do this after the send back just for testing reasons (so we don't crash if we send to ourself).
                            ExternalCall.satisfyOtherPartyPublicKey(token, sendBack ? ExternalCall.WhoAmI.BOB : ExternalCall.WhoAmI.ALICE, publicBG);

                            break;

                        } catch (NumberFormatException e) {
                            break;
                        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
                            e.printStackTrace();
                            break;
                        }
                    case "decode_cmd": {
                        String encodedMsg = cmd[3];
                        try {
                            String decodedMsg = decode(token, encodedMsg);
                            if (decodedMsg == null)
                                throw new IllegalStateException("null command after decode");
                            ExternalCall call = ExternalCall.activeCalls.get(token);
                            String result = call.command(decodedMsg, token);
                            call.sendMessage(result);
                            break;
                        } catch (NumberFormatException e) {
                            break;
                        }
                    }
                    case "decode_msg": {
                        String encodedMsg = cmd[3];
                        try {
                            String decodedMsg = decode(token, encodedMsg);
                            if (decodedMsg == null)
                                throw new IllegalStateException("null command after decode");
                            ExternalCall call = ExternalCall.activeCalls.get(token);
                            call.receiveEnqueue(decodedMsg);
                            break;
                        } catch (NumberFormatException e) {
                            break;
                        }
                    }
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
