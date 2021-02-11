package org.openhab.binding.mideaac.internal.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security coding and decoding.
 *
 * @author Jacek Dobrowolski
 */
public class Security {
    private final static String appKey = "434a209a5ce141c3b726de067835d7f0";
    private final static String signKey = "xhdiwjnchekd4d512chdjx5d8e4c394D2D7S";
    private static SecretKeySpec encKey = null;
    private static Logger logger = LoggerFactory.getLogger(Security.class);

    public static byte[] aes_decrypt(byte[] encrypt_data) {
        byte[] plainText = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = getEncKey();

            try {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                logger.warn("AES decryption error: InvalidKeyException: {}", e.getMessage());
                return null;
            }

            try {
                plainText = cipher.doFinal(encrypt_data);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES decryption error: IllegalBlockSizeException: {}", e.getMessage());
                return null;
            } catch (BadPaddingException e) {
                logger.warn("AES decryption error: BadPaddingException: {}", e.getMessage());
                return null;
            }

        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES decryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return null;
        } catch (NoSuchPaddingException e) {
            logger.warn("AES decryption error: NoSuchPaddingException: {}", e.getMessage());
            return null;
        }

        return plainText;
    }

    public static byte[] aes_encrypt(byte[] plainText) {
        byte[] encryptData = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            SecretKeySpec key = getEncKey();

            try {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                logger.warn("AES encryption error: InvalidKeyException: {}", e.getMessage());
            }

            try {
                encryptData = cipher.doFinal(plainText);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES encryption error: IllegalBlockSizeException: {}", e.getMessage());
                return null;
            } catch (BadPaddingException e) {
                logger.warn("AES encryption error: BadPaddingException: {}", e.getMessage());
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES encryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return null;
        } catch (NoSuchPaddingException e) {
            logger.warn("AES encryption error: NoSuchPaddingException: {}", e.getMessage());
            return null;
        }

        return encryptData;
    }

    private static SecretKeySpec getEncKey() throws NoSuchAlgorithmException {
        if (encKey == null) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(signKey.getBytes(StandardCharsets.US_ASCII));
            byte[] key = md.digest();
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            encKey = skeySpec;
        }

        return encKey;
    }

    public static byte[] encode32_data(byte[] raw) {
        byte[] combine = ByteBuffer.allocate(raw.length + signKey.getBytes(StandardCharsets.US_ASCII).length).put(raw)
                .put(signKey.getBytes(StandardCharsets.US_ASCII)).array();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(combine);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }
}
