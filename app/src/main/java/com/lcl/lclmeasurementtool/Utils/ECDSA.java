package com.lcl.lclmeasurementtool.Utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * ECDSA helper class that provides convenient helper functions to assist cryptographic operations
 * related to EC algorithm
 */
public class ECDSA {
    // This is a custom easy helper implementation for the EC prime256v1 or the secp256r1 curves.
    // The raw ASN.1 Encoding structures will contain the necessary metadata for interoperation, it would need corresponding
    // constant size changes below as necessary to support non 256 bit length curves.

    // statically load the BC provider
    static {
        Security.removeProvider("BC");
        Security.addProvider(new BouncyCastleProvider());
    }

    // the EC algorithm
    private static final String ALGORITHM = "EC";

    // SHA256 with ECDSA
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

    // public key encoding size
    private static final int PublicKeyEncodingSize = 65;  // 64 + 1 byte 0x04

    // padding
    private static final byte PADDING = 0x04;

    /**
     * Generate the corresponding public key given an EC private key
     *
     * @param sk  the EC private key from which the public key will be generated
     * @return    an EC public key corresponding to the private key
     * @throws IOException                if the key gen process failed because of IO issue
     * @throws NoSuchAlgorithmException   if the key gen process failed because of incorrect algorithm
     * @throws InvalidKeySpecException    if the key gen process failed because of the invalid private key spec
     * @throws NoSuchProviderException    if the key gen process failed because of incorrect provider
     */
    public static ECPublicKey DerivePublicKey(ECPrivateKey sk) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM, "BC");
        ECParameterSpec keyParams = sk.getParams();
        byte[] skEncoded = sk.getEncoded(); // PKCS8 Encoding
        byte[] pkBytesEmbedded = new byte[PublicKeyEncodingSize];
        System.arraycopy(skEncoded, skEncoded.length - PublicKeyEncodingSize, pkBytesEmbedded, 0, PublicKeyEncodingSize);

        ECPoint p = decodePoint(pkBytesEmbedded, keyParams.getCurve());
        ECPublicKeySpec pkSpec = new ECPublicKeySpec(p, keyParams);
        return (ECPublicKey) kf.generatePublic(pkSpec);
    }

    /**
     * Deserialize the private key from raw bytes
     *
     * @param raw  the raw bytes to convert to private key
     * @return     an EC private key corresponds to the bytes
     * @throws NoSuchAlgorithmException   if the key gen process failed because of incorrect algorithm
     * @throws InvalidKeySpecException    if the key gen process failed because of invalid key spec
     */
    public static ECPrivateKey DeserializePrivateKey(byte[] raw) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Use the following two lines and comment out the last line if ECUtil is unavailable.
        // KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        // return (ECPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(raw));
        return decodePKCS8ECPrivateKey(raw);
    }

    /**
     * Deserialize the public key from raw bytes
     *
     * @param rawSPKIEncoded  the raw bytes to convert to public key
     * @return                an EC public key corresponds to the bytes
     * @throws NoSuchAlgorithmException if the key gen process failed because of the incorrect algorithm
     * @throws InvalidKeySpecException  if the key gen process failed because of the invalid key spec
     * @throws NoSuchProviderException  if the key gen process failed because of the incorrect provider
     */
    public static ECPublicKey DeserializePublicKey(byte[] rawSPKIEncoded) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM, "BC");
        return (ECPublicKey) kf.generatePublic(new X509EncodedKeySpec(rawSPKIEncoded));
    }

    /**
     * Verify the signature of the message using the public key
     * @param message      the message whose signature will be verified
     * @param signature    the signature to be verified
     * @param pk           the public key that will be used to verify the signature
     * @return             true if the signature given matches the one from the message using the public key;
     *                     false otherwise.
     * @throws NoSuchAlgorithmException   if the verification process failed because of the incorrect algorithm
     * @throws InvalidKeyException        if the verification process failed because of the invalid key
     * @throws SignatureException         if the verification process failed because of the invalid signature
     * @throws NoSuchProviderException    if the verification process failed because of the incorrect provider
     */
    public static boolean Verify(byte[] message, byte[] signature, ECPublicKey pk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        Signature s = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
        s.initVerify((PublicKey) pk);
        s.update(message);
        return s.verify(signature);
    }

    /**
     * Sign the message using the private key
     * @param message  the message to be signed
     * @param sk       the private key used to sign the message
     * @return         the signature of the message
     * @throws NoSuchAlgorithmException  if the signing process failed because of the incorrect algorithm
     * @throws InvalidKeyException       if the signing process failed because of the invalid key
     * @throws SignatureException        if the signing process failed because of the invalid signature
     * @throws NoSuchProviderException   if the signing process failed because of the incorrect provider
     */
    public static byte[] Sign(byte[] message, ECPrivateKey sk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        Signature s = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
        s.initSign(sk);
        s.update(message);
        return s.sign();
    }

    /////////////////////////////////////  ECUtils  //////////////////////////////////////////////

    /**
     * Decode the bytes data using the EC algorithm
     * @param data    the bytes data to be decoded
     * @param curve   the EllipticCurve used to decode the data
     * @return        an EC point corresponds to the raw bytes
     * @throws IOException  when the data is ill-formatted
     */
    private static ECPoint decodePoint(byte[] data, EllipticCurve curve)
            throws IOException {
        if ((data.length == 0) || (data[0] != 4)) {
            throw new IOException("Only uncompressed point format supported");
        }
        // Per ANSI X9.62, an encoded point is a 1 byte type followed by
        // ceiling(log base 2 field-size / 8) bytes of x and the same of y.
        int n = (data.length - 1) / 2;
        if (n != ((curve.getField().getFieldSize() + 7) >> 3)) {
            throw new IOException("Point does not match field size");
        }

        byte[] xb = Arrays.copyOfRange(data, 1, 1 + n);
        byte[] yb = Arrays.copyOfRange(data, n + 1, n + 1 + n);

        return new ECPoint(new BigInteger(1, xb), new BigInteger(1, yb));
    }

    /**
     * Decode the PKCS8 private key
     * @param encoded the encoded bytes of the private key in PKCS8 format
     * @return        the private key associated with the raw bytes
     * @throws InvalidKeySpecException  if the key spec is invalid
     */
    private static ECPrivateKey decodePKCS8ECPrivateKey(byte[] encoded) throws InvalidKeySpecException {
        KeyFactory keyFactory = getKeyFactory();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

        return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * Return the key factory for the BC algorithm
     * @return  a key factor corresponds to the BC algorithm
     */
    private static KeyFactory getKeyFactory() {
        try {
            return KeyFactory.getInstance(ALGORITHM, "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }
}
