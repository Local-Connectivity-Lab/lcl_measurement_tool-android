package com.lcl.lclmeasurementtool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import com.lcl.lclmeasurementtool.Models.QRCodeKeysModel;
import com.lcl.lclmeasurementtool.Utils.DecoderException;
import com.lcl.lclmeasurementtool.Utils.Hex;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;

public class SecurityTest {

    PrivateKey sk_a;
    PublicKey pk_a;

    PrivateKey sk_t;
    PublicKey pk_t;
    byte[] sigma_t;
    String json;

    @Before
    public void init() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(SecurityUtils.RSA);
            ECGenParameterSpec genKeySpec = new ECGenParameterSpec("secp256k1");
            generator.initialize(genKeySpec);
            KeyPair masterPair = generator.generateKeyPair();
            pk_a = masterPair.getPublic();
            sk_a = masterPair.getPrivate();
            KeyPair pair = generator.generateKeyPair();
            sk_t = pair.getPrivate();
            pk_t = pair.getPublic();
            sigma_t = SecurityUtils.sign(sk_t.getEncoded(), sk_a, SecurityUtils.SHA_256_WITH_RSA_SIGNATURE);
            QRCodeKeysModel keysModel = new QRCodeKeysModel(Hex.encodeHexString(sigma_t, false),
                            Hex.encodeHexString(sk_t.getEncoded(), false),
                            Hex.encodeHexString(pk_a.getEncoded(), false));
            json = JsonStream.serialize(keysModel);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    @After
    public void deinit() {
        sk_a = null;
        pk_a = null;
        sk_t = null;
        pk_t = null;
        sigma_t = null;
        System.out.println("test completes");
    }

    @Test
    public void testPrivateDecoding() {
//        for (String s : Security.getAlgorithms("AlgorithmParameters")) {
////            p.getService("AlgorithmParameters", "EC").getAttribute("SupportedCurves");
//            System.out.println(s);
//        }

//        String s = Security.getProviders("AlgorithmParameters.EC")[0]
//                .getService("AlgorithmParameters", "EC").getAttribute("SupportedCurves");
//        System.out.println(s);

//        String sk = "30740201010420b344907a583424a15cd3107a6f0214f85339b55139826ce9e48604ced55588fda00706052b8104000aa14403420004142103104a89071afb74508cccc6107d51054e30ef0635d81cd4b46edcc1f307c158f48f512d0ec64a718472ddf1fca624ccfe2e446b8e3bd65af5fe88c3528d";
//        try {
////            ECPrivateKey ecPrivateKey = (ECPrivateKey) SecurityUtils.decodePrivateKey(sk, SecurityUtils.ALGORITHM);
//
//        } catch (DecoderException | InvalidKeySpecException | NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }

    }

    @Test
    public void testAlgorithm() {
        assertEquals(sk_a.getAlgorithm(), SecurityUtils.RSA);
        assertEquals(pk_a.getAlgorithm(), SecurityUtils.RSA);

        assertEquals(sk_t.getAlgorithm(), SecurityUtils.RSA);
        assertEquals(pk_t.getAlgorithm(), SecurityUtils.RSA);
    }

    @Test
    public void testSerializationAndDeserialization() {
        QRCodeKeysModel keysModel = JsonIterator.deserialize(json, QRCodeKeysModel.class);
        try {
            assertArrayEquals(sk_t.getEncoded(), Hex.decodeHex(keysModel.getSk_t()));
            assertArrayEquals(pk_a.getEncoded(), Hex.decodeHex(keysModel.getPk_a()));
            assertArrayEquals(sigma_t, Hex.decodeHex(keysModel.getSigma_t()));
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVerify() {
        try {
            boolean result = SecurityUtils.verify(sk_t.getEncoded(),
                    sigma_t,
                    pk_a,
                    SecurityUtils.SHA_256_WITH_RSA_SIGNATURE);
            assertTrue(result);
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSign() {
        String test_data = "test!";
        try {
            byte[] signature = SecurityUtils.sign(test_data.getBytes(StandardCharsets.UTF_8), sk_t, SecurityUtils.SHA_256_WITH_RSA_SIGNATURE);
            boolean result = SecurityUtils.verify(test_data.getBytes(StandardCharsets.UTF_8), signature, pk_t, SecurityUtils.SHA_256_WITH_RSA_SIGNATURE);
            assertTrue(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHexEncodeAndDecode() {
        String sk_t_hex = Hex.encodeHexString(sk_t.getEncoded());
        String pk_t_hex = Hex.encodeHexString(pk_t.getEncoded());
        try {
            byte[] sk_t_hexArray = Hex.decodeHex(sk_t_hex);
            byte[] pk_t_hexArray = Hex.decodeHex(pk_t_hex);

            assertArrayEquals(sk_t.getEncoded(), sk_t_hexArray);
            assertArrayEquals(pk_t.getEncoded(), pk_t_hexArray);
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGenPublicKey() {
        try {
            RSAPublicKey test_pk = (RSAPublicKey) SecurityUtils.genPublicKey(Hex.encodeHexString(sk_t.getEncoded()), SecurityUtils.RSA);
            RSAPublicKey rsa_pk = ((RSAPublicKey) pk_t);
            assertEquals(test_pk.getModulus(), rsa_pk.getModulus());
            assertEquals(test_pk.getPublicExponent(), rsa_pk.getPublicExponent());
        } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDecodePrivateKey() {
        try {
            PrivateKey privateKey = SecurityUtils.decodePrivateKey(Hex.encodeHexString(sk_t.getEncoded(), false), SecurityUtils.RSA);
            assertArrayEquals(privateKey.getEncoded(), sk_t.getEncoded());
        } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDecodePublicKey() {
        try {
            PublicKey publicKey = SecurityUtils.decodePublicKey(Hex.encodeHexString(pk_t.getEncoded(), false), SecurityUtils.RSA);
            assertArrayEquals(publicKey.getEncoded(), pk_t.getEncoded());
        } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRandomLBits() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] R = new byte[16];
        secureRandom.nextBytes(R);
        assertEquals(R.length, 16);
    }

}
