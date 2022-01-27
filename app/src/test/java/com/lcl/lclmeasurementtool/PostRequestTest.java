package com.lcl.lclmeasurementtool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jsoniter.output.JsonStream;
import com.lcl.lclmeasurementtool.Models.MeasurementDataModel;
import com.lcl.lclmeasurementtool.Models.RegistrationMessageModel;
import com.lcl.lclmeasurementtool.Models.SignalStrengthMessageModel;
import com.lcl.lclmeasurementtool.Utils.DecoderException;
import com.lcl.lclmeasurementtool.Utils.ECDSA;
import com.lcl.lclmeasurementtool.Utils.Hex;
import com.lcl.lclmeasurementtool.Utils.SecurityUtils;
import com.lcl.lclmeasurementtool.Utils.SerializationUtils;
import com.lcl.lclmeasurementtool.Utils.TimeUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class PostRequestTest {

    byte[] h_pkr;
    String sk_t;

    @Test
    public void testPost() {
//        {"sigma_t":"304502204550bfdc0c4fe829d4ee3b4ff2d58eab4d55d969cfdeddba2c6aec720e951eba022100f9728e692f60178af5c742951455ad230d10e9ec0784eb393a10f5e46fe9b1ce","sk_t":"308184020100301006072a8648ce3d020106052b8104000a046d306b02010104204969762f1a7345d4fcbb9126eae9a18da5e8b415815e773fb96321e065416d17a14403420004c19259265628f5c1f71391eb850373784f23cf76deb83a94f6cb459efae6fd36d963df5e220b24c5d035d80b27ee475a9f32d5ae888c6cdd468e45590b2de266","pk_a":"3056301006072a8648ce3d020106052b8104000a03420004b28b86b6e3a8b827c82f29e06c64588035151d7a3ade115e5a4b21bb00157650a5fe0b577b68b437785672c749657d534962bea87e104c80234d2c6e7f6b8eb8"}
        String sigma_t = "304502204550bfdc0c4fe829d4ee3b4ff2d58eab4d55d969cfdeddba2c6aec720e951eba022100f9728e692f60178af5c742951455ad230d10e9ec0784eb393a10f5e46fe9b1ce";
        String pk_a = "3056301006072a8648ce3d020106052b8104000a03420004b28b86b6e3a8b827c82f29e06c64588035151d7a3ade115e5a4b21bb00157650a5fe0b577b68b437785672c749657d534962bea87e104c80234d2c6e7f6b8eb8";
//        String pk_a = "3056301006072a8648ce3d020106052b8104000a03420004da754f3ede85eec8b7dec3fda5dbdc35662f807f29433e2810743c889de15e1f5d4338453fc13c45e856287cc7849554f92aca832c66a094c7f7f231c50afebf";
        String sk_t = "308184020100301006072a8648ce3d020106052b8104000a046d306b02010104204969762f1a7345d4fcbb9126eae9a18da5e8b415815e773fb96321e065416d17a14403420004c19259265628f5c1f71391eb850373784f23cf76deb83a94f6cb459efae6fd36d963df5e220b24c5d035d80b27ee475a9f32d5ae888c6cdd468e45590b2de266";
        validate(sigma_t, pk_a, sk_t);
    }

    private void validate(String sigma_t, String pk_a, String sk_t) {
        this.sk_t = sk_t;
        try {
            if (!ECDSA.Verify(Hex.decodeHex(sk_t),
                    Hex.decodeHex(sigma_t),
                    ECDSA.DeserializePublicKey(Hex.decodeHex(pk_a))
            )) {
                System.out.println("unable to verify keys");
                return;
            }
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | DecoderException e) {
            e.printStackTrace();
            return;
        }

        ECPublicKey pk_t;
        ECPrivateKey ecPrivateKey;
        try {
            ecPrivateKey = ECDSA.DeserializePrivateKey(Hex.decodeHex(sk_t));
            pk_t = ECDSA.DerivePublicKey(ecPrivateKey);
        } catch (DecoderException | NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchProviderException e) {
            e.printStackTrace();
            return;
        }

        SecureRandom secureRandom = new SecureRandom();
        byte[] R = new byte[16];
        secureRandom.nextBytes(R);
        System.out.println("R:" + Hex.encodeHexString(R));
//        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String RStringInHex = Hex.encodeHexString(R);
//        preferences.edit().putString("R", RStringInHex).apply();

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        byte[] h_sec;
        byte[] h_concat;
        byte[] sigma_r;
        try {
            byteArray.write(pk_t.getEncoded());
            byteArray.write(R);
            h_pkr = SecurityUtils.digest(byteArray.toByteArray(), SecurityUtils.SHA_256_HASH);
            System.out.println("h_pkr:" + Hex.encodeHexString(h_pkr));
//            preferences.edit().putString("h_pkr", Hex.encodeHexString(h_pkr)).apply();

            byteArray.reset();
            byteArray.write(Hex.decodeHex(sk_t));
            byteArray.write(pk_t.getEncoded());
            h_sec = SecurityUtils.digest(byteArray.toByteArray(), SecurityUtils.SHA_256_HASH);
            System.out.println("h_sec:" + Hex.encodeHexString(h_sec));

            byteArray.reset();
            byteArray.write(h_pkr);
            byteArray.write(h_sec);
            h_concat = byteArray.toByteArray();
            sigma_r = ECDSA.Sign(h_concat, ecPrivateKey);
        } catch (IOException | NoSuchAlgorithmException | DecoderException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            e.printStackTrace();
            return;
        }

        RegistrationMessageModel registrationMessageModel = new RegistrationMessageModel(sigma_r, h_concat, R);
        System.out.println("sigma_r:" + Hex.encodeHexString(sigma_r));
        System.out.println("h:" + Hex.encodeHexString(h_concat));
        System.out.println("R:" + Hex.encodeHexString(R));
    }

    @Test
    public void testSignalUpload() throws InvalidKeySpecException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, DecoderException {
        int dBm = -80;
        String ts = TimeUtils.getTimeStamp(ZoneId.of("America/Los_Angeles"));

        // TODO(sudheesh001) security check
        SignalStrengthMessageModel signalStrengthMessageModel =
                new SignalStrengthMessageModel(
                        80.123,
                        120.456,
                        ts,
                        dBm,
                        1,
                        "12345678",
                        "device_id");
        try {
            byte[] serialized = SerializationUtils.serializeToBytes(signalStrengthMessageModel);
            byte[] sig_m = ECDSA.Sign(serialized, ECDSA.DeserializePrivateKey(Hex.decodeHex(sk_t)));
            System.out.println("serialized:" + Hex.encodeHexString(serialized));
            System.out.println("sigma_m:" + Hex.encodeHexString(sig_m));
        } catch (JsonProcessingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
//        uploadData(signalStrengthMessageModel, sk_t, Hex.encodeHexString(h_pkr), UploadManager.SIGNAL_ENDPOINT);

    }

    @Test
    public void verify() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        ECPrivateKey ecPrivateKey = null;
        try {
            ecPrivateKey = ECDSA.DeserializePrivateKey(Hex.decodeHex(sk_t));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
//        ECPublicKey pk_t = ECDSA.DerivePublicKey(ecPrivateKey);
//        System.out.println(Hex.encodeHexString(pk_t.getEncoded()));
//        String message = "aced00057372003c636f6d2e6c636c2e6c636c6d6561737572656d656e74746f6f6c2e4d6f64656c732e5369676e616c537472656e6774684d6573736167654d6f64656c63002a79c40e560f02000249000364426d49000a6c6576656c5f636f646578720036636f6d2e6c636c2e6c636c6d6561737572656d656e74746f6f6c2e4d6f64656c732e4d6561737572656d656e74446174614d6f64656c4f56a48ed7478b880200054400086c617469747564654400096c6f6e6769747564654c000763656c6c5f69647400124c6a6176612f6c616e672f537472696e673b4c00096465766963655f696471007e00024c000974696d657374616d7071007e00027870405407df3b645a1d405e1d2f1a9fbe777400007400096465766963655f696474001a323032322d30312d32315431353a32343a30352e303634323636ffffffb000000001";
//        String signature = "3046022100d5a89f5a7089b49176b011ca5303e44dc554d3e41c93c241d56297532d35dc36022100fbab0b602c9e3897a157bc0ad11226d875a995fe397280cc9f29ae07e31b316a";
//        try {
//            boolean result = ECDSA.Verify(Hex.decodeHex(message), Hex.decodeHex(signature), pk_t);
//            Assert.assertTrue(result);
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        } catch (SignatureException e) {
//            e.printStackTrace();
//        } catch (DecoderException e) {
//            e.printStackTrace();
//        }
    }


    private void uploadData(MeasurementDataModel data, String sk_t, String h_pkr, String endpoint) throws NoSuchAlgorithmException,
            InvalidKeySpecException, DecoderException, SignatureException, InvalidKeyException, JsonProcessingException, NoSuchProviderException {

        byte[] serialized = SerializationUtils.serializeToBytes(data);

        byte[] sig_m = ECDSA.Sign(serialized, ECDSA.DeserializePrivateKey(Hex.decodeHex(sk_t)));
        Map<String, Object> uploadMap = new HashMap<>();
        uploadMap.put("M", Hex.encodeHexString(serialized));
        uploadMap.put("sigma_m", Hex.encodeHexString(sig_m));
        uploadMap.put("h_pkr", h_pkr);

        // upload data
//        UploadManager upload = UploadManager.Builder();
        String json = JsonStream.serialize(uploadMap);
        System.out.println(json);
//        upload.addPayload(;
//        upload.addEndpoint(endpoint);
//        try {
//            upload.post();
//        } catch (IOException e) {
//            Assert.fail();
//        }
    }
}
