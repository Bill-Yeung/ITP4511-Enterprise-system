package hk.edu.hkiit.jakarta.webapp.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {

    private static final String ALGORITHM    = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LEN   = 12;  // 96-bit IV recommended for GCM
    private static final int    GCM_TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static byte[] keyBytes;

    public static void init(String aesKey) {
        byte[] raw = aesKey.getBytes(StandardCharsets.UTF_8);
        // Pad or truncate to 32 bytes (AES-256)
        keyBytes = Arrays.copyOf(raw, 32);
    }

    private static SecretKey getKey() {
        if (keyBytes == null) {
            throw new IllegalStateException("EncryptionUtil not initialized. Call init() first.");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV so decrypt can recover it: Base64(iv + ciphertext+tag)
            byte[] combined = new byte[GCM_IV_LEN + encrypted.length];
            System.arraycopy(iv,        0, combined, 0,          GCM_IV_LEN);
            System.arraycopy(encrypted, 0, combined, GCM_IV_LEN, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            if (combined.length < GCM_IV_LEN) {
                return ciphertext;
            }
            byte[] iv   = Arrays.copyOfRange(combined, 0, GCM_IV_LEN);
            byte[] data = Arrays.copyOfRange(combined, GCM_IV_LEN, combined.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return ciphertext;
        }
    }

}
