package com.lcl.lclmeasurementtool.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    public static final String RSA = "RSA";
    public static final String SHA_256_WITH_RSA_SIGNATURE = "SHA256withRSA";
    public static final String SHA_256_HASH = "SHA-256";

    /**
     * Digest the message with given algorithm
     *
     * @param data          the data to be hashed
     * @param algorithm     the algorithm used to digest the message
     * @return              the digested message in byte array
     * @throws NoSuchAlgorithmException if the provided algorithm doesn't exist
     */
    public static byte[] digest(String data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(data.getBytes(StandardCharsets.UTF_8));
        return messageDigest.digest();
    }

    /**
     * Digest the message with given algorithm
     *
     * @param data          the data to be hashed
     * @param algorithm     the algorithm used to digest the message
     * @return              the digested message in byte array
     * @throws NoSuchAlgorithmException if the provided algorithm doesn't exist
     */
    public static byte[] digest(byte[] data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(data);
        return messageDigest.digest();
    }
}
