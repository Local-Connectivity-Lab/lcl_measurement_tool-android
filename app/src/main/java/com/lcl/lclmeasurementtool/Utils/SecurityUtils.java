package com.lcl.lclmeasurementtool.Utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class SecurityUtils {

    /**
     * Sign the data with given private key and algorithm
     * @param data         the data to be signed
     * @param privateKey   the private key used to sign the data
     * @param algorithm    the algorithm used to sign the data
     * @return             the signed data in a byte array.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey, String algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature s = Signature.getInstance(algorithm);
        s.initSign(privateKey);
        s.update(data);
        return s.sign();
    }

    /**
     * Digest the message with given algorithm
     *
     * @param data          the data to be hashed
     * @param algorithm     the algorithm used to digest the message
     * @return              the digested message in byte array
     * @throws NoSuchAlgorithmException
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
     * @throws NoSuchAlgorithmException
     */
    public static byte[] digest(byte[] data, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        messageDigest.update(data);
        return messageDigest.digest();
    }

    /**
     * Verify a signed data with public key and given algorithm
     *
     * @param data               the data to be verified
     * @param publicKey          the public key to verify the the signed data
     * @param signatureBytes     the signature
     * @param algorithm          the algorithm used for the signature
     * @return
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     */
    public static boolean verify(byte[] data, PublicKey publicKey, byte[] signatureBytes, String algorithm) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signatureBytes);
    }
}
