package com.lcl.lclmeasurementtool.Utils;

import android.org.apache.commons.codec.DecoderException;
import android.org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

// TODO(sudheesh001) security check
public class SecurityUtils {

    public static final String RSA = "RSA";
    public static final String SHA_256_HASH = "SHA-256";
    public static final String SHA_256_WITH_RSA_SIGNATURE = "SHA256withRSA";

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
     *
     * @param messageInHex
     * @param receivedSignature
     * @param publicKey
     * @param algorithm
     * @return true if the key is valid;  false otherwise
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException
     */
    public static boolean verify(byte[] messageInHex, byte[] receivedSignature, PublicKey publicKey, String algorithm) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicKey);
        signature.update(messageInHex);
        return signature.verify(receivedSignature);
    }

    public static PublicKey genPublicKey(String sk_t, String algorithm) throws DecoderException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPrivateCrtKey sk = (RSAPrivateCrtKey) decodePrivateKey(sk_t, algorithm);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(sk.getModulus(), sk.getPublicExponent());
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePublic(keySpec);
    }

    public static PublicKey decodePublicKey(String pk_t, String algorithm) throws DecoderException, NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance(algorithm)
                .generatePublic(new X509EncodedKeySpec(Hex.decodeHex(pk_t)));
    }

    public static PrivateKey decodePrivateKey(String sk_t, String algorithm) throws DecoderException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] sk = Hex.decodeHex(sk_t);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(sk);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(keySpec);
    }
}
// TODO(sudheesh001) security check
