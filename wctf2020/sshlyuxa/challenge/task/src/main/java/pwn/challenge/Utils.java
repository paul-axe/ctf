package pwn.challenge;

import java.io.*;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.spec.X509EncodedKeySpec;
import java.security.PublicKey;
import java.security.KeyFactory;

public class Utils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String sessionBaseDir = "keys";
    private static String socketsBaseDir = "sockets";
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    static {
        {
            File f = new File(socketsBaseDir);
            if (!f.exists() || !f.isDirectory()) {
                f.delete();
                f.mkdir();
            }
        }
        {
            File f = new File(sessionBaseDir);
            if (!f.exists() || !f.isDirectory()) {
                f.delete();
                f.mkdir();
            }
        }
    }


    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String inHex) {
        String hex = inHex.toUpperCase();
        String ALPHA = new String(HEX_ARRAY);
        if (hex.length()%2 != 0)
            return null;
        byte[] bytes = new byte[hex.length()/2];
        for (int j = 0; j < bytes.length; j++) {
            int v;
            byte b = (byte) 0;
            v = ALPHA.indexOf(hex.charAt(2*j));
            if (v < 0) return null;
            b |=  v << 4;
            v = ALPHA.indexOf(hex.charAt(2*j+1));
            if (v < 0) return null;
            b |= v;
            bytes[j] = b;
        }
        return bytes;
    }

    public static byte[] generateRandom(int num) {
        byte[] bytes = new byte[num];
        new Random().nextBytes(bytes);
        return bytes;
    }

    public static byte[] decrypt(byte[] key, byte[] msg) throws Exception {
        if (msg == null)
            return null;

        X509EncodedKeySpec ks = new X509EncodedKeySpec(key);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(ks);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, pub);
        try {
            return cipher.doFinal(msg);
        } catch (BadPaddingException |  IllegalBlockSizeException e) {
            return null;
        }
    }

    public static Boolean userExists(String username) {
        File file = new File(String.format("%s/%s.pub", sessionBaseDir, username));
        return file.exists();
    }

    public static String ask(String prefix) throws Exception {
        System.out.print(prefix+" ");
        return reader.readLine();
    }

    public static void saveUserKey(String username, byte[] key) throws Exception {
        File file = new File(String.format("%s/%s.pub", sessionBaseDir, username));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(key);
        fos.flush();
        fos.close();
    }

    public static byte[] getUserKey(String username) throws Exception {
        File file = new File(String.format("%s/%s.pub", sessionBaseDir, username));
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return data;
    }

    public static String getUserSocketPath(String username) {
        return String.format("%s/%s", socketsBaseDir, username);
    }

}
