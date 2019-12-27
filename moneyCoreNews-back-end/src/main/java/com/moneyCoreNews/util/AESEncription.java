package com.moneyCoreNews.util;
import com.google.common.base.Charsets;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Component
public class AESEncription {

    private final Logger log = LoggerFactory.getLogger(AESEncription.class);

    public static final String CIPHER_ALGORITHM = "AES";
    public static final String KEY_ALGORITHM = "AES";
    public static final byte[] SECRET_KEY = "1J34LKS09L3HJ5LK" .getBytes(Charsets.UTF_8); // exactly 16 bytes to not use JCE (Java Cryptography Extension)

    public  String decrypt(String encryptedInput) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY, KEY_ALGORITHM));
            return new String(cipher.doFinal(Base64.decodeBase64(encryptedInput)), Charsets.UTF_8);

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String encrypt(String str) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET_KEY, KEY_ALGORITHM));
            return Base64.encodeBase64URLSafeString(cipher.doFinal(str.getBytes(Charsets.UTF_8)));

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}