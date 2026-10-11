package security;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
    private static final int ITERATIONS = 600000;
    private Passwords() { }
    public static String hash(String password) {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(derive(password,salt,ITERATIONS));
    }
    private static byte[] derive(String password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),salt,iterations,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (Exception e) { throw new IllegalStateException("Password hashing failed",e); }
        finally { spec.clearPassword(); }
    }
    public static boolean verify(String password, String stored) {
        try {
            String[] parts=stored.split("\\$");
            if(parts.length!=4 || !parts[0].equals("pbkdf2")) return false;
            int rounds=Integer.parseInt(parts[1]); if(rounds<100000 || rounds>1000000) return false;
            return MessageDigest.isEqual(Base64.getDecoder().decode(parts[3]),derive(password,Base64.getDecoder().decode(parts[2]),rounds));
        } catch (RuntimeException e) { return false; }
    }
}
