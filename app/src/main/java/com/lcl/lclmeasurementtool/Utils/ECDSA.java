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

public class ECDSA {
    // This is a custom easy helper implementation for the EC prime256v1 or the secp256r1 curves.
    // The raw ASN.1 Encoding structures will contain the necessary metadata for interoperation, it would need corresponding
    // constant size changes below as necessary to support non 256 bit length curves.

    static {
        Security.removeProvider("BC");
        Security.addProvider(new BouncyCastleProvider());
    }


    private static final String ALGORITHM = "EC";
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    private static final int PublicKeyEncodingSize = 65;  // 64 + 1 byte 0x04
    private static final byte PADDING = 0x04;

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

    public static ECPrivateKey DeserializePrivateKey(byte[] raw) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Use the following two lines and comment out the last line if ECUtil is unavailable.
        // KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        // return (ECPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(raw));
        return decodePKCS8ECPrivateKey(raw);
    }

    public static ECPublicKey DeserializePublicKey(byte[] rawSPKIEncoded) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM, "BC");
        return (ECPublicKey) kf.generatePublic(new X509EncodedKeySpec(rawSPKIEncoded));
    }

    public static boolean Verify(byte[] message, byte[] signature, ECPublicKey pk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        Signature s = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
        s.initVerify((PublicKey) pk);
        s.update(message);
        return s.verify(signature);
    }

    public static byte[] Sign(byte[] message, ECPrivateKey sk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchProviderException {
        Signature s = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
        s.initSign(sk);
        s.update(message);
        return s.sign();
    }

    /////////////////////////////////////  ECUtils  //////////////////////////////////////////////

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

    private static ECPrivateKey decodePKCS8ECPrivateKey(byte[] encoded)
            throws InvalidKeySpecException {
        KeyFactory keyFactory = getKeyFactory();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

        return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private static KeyFactory getKeyFactory() {
        try {
            return KeyFactory.getInstance(ALGORITHM, "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }
}
