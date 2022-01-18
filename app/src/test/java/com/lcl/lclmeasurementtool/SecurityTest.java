package com.lcl.lclmeasurementtool;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import com.lcl.lclmeasurementtool.Models.QRCodeKeysModel;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
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
            generator.initialize(2048);
            KeyPair masterPair = generator.generateKeyPair();
            pk_a = masterPair.getPublic();
            sk_a = masterPair.getPrivate();
            KeyPair pair = generator.generateKeyPair();
            sk_t = pair.getPrivate();
            pk_t = pair.getPublic();
            sigma_t = SecurityUtils.sign(sk_t.getEncoded(), sk_a, SecurityUtils.SHA256withRSA);
            QRCodeKeysModel keysModel = new QRCodeKeysModel(Hex.encodeHexString(sigma_t, false),
                            Hex.encodeHexString(sk_t.getEncoded(), false),
                            Hex.encodeHexString(pk_a.getEncoded(), false));
            json = JsonStream.serialize(keysModel);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
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
                    SecurityUtils.SHA256withRSA);
            assertTrue(result);
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSign() {
        String test_data = "test!";
        try {
            byte[] signature = SecurityUtils.sign(test_data.getBytes(StandardCharsets.UTF_8), sk_t, SecurityUtils.SHA256withRSA);
            boolean result = SecurityUtils.verify(test_data.getBytes(StandardCharsets.UTF_8), signature, pk_t, SecurityUtils.SHA256withRSA);
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
