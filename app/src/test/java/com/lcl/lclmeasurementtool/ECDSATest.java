package com.lcl.lclmeasurementtool;

import android.org.apache.commons.codec.DecoderException;
import android.org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.io.IOException;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;

import com.lcl.lclmeasurementtool.Utils.ECDSA;

public class ECDSATest {
    @Test
    public void Test_ECDSA_PublicKeyConversion() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, DecoderException, InvalidKeySpecException, NoSuchProviderException {
        String hexFromServer = "3056301006072a8648ce3d020106052b8104000a03420004da754f3ede85eec8b7dec3fda5dbdc35662f807f29433e2810743c889de15e1f5d4338453fc13c45e856287cc7849554f92aca832c66a094c7f7f231c50afebf";
        byte[] pkBytes = Hex.decodeHex(hexFromServer);

        // One line call to make the PK_A from server into PublicKey object
        PublicKey pk = ECDSA.DeserializePublicKey(pkBytes);
        System.out.println(pk);

        String pkToHex = Hex.encodeHexString(pk.getEncoded());
        assertEquals(pkToHex, hexFromServer);
    }

    @Test
    public void Test_ECDSA_PrivateKeyConversion() throws DecoderException, NoSuchAlgorithmException, InvalidKeySpecException, IOException, SignatureException, InvalidKeyException, NoSuchProviderException {
        String hexFromServer = "308184020100301006072a8648ce3d020106052b8104000a046d306b0201010420798116c5c26ccfd95e4e13fdf4df9e46cf3629223b190da6c891d48e4de5da57a144034200044552ed599a2d855f59286447d687fbd1ed05793025a7994268f29baef5ca1e3432f9b1d48301a85e4bd8ed77e2c6f3e834f947540b144dbc5a71a548c046c9e2";
        byte[] skBytes = Hex.decodeHex(hexFromServer);

        // One line calls to deserialize SK from server into Private Key and then use the PrivateKey to derive PublicKey
        PrivateKey sk = ECDSA.DeserializePrivateKey(skBytes);
        PublicKey pk = ECDSA.DerivePublicKey((ECPrivateKey) sk);

        byte[] message = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        byte[] invalidMessage = new byte[]{16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        // Example Testing of using the converted SK for signing (done by client) and verifying the signature (done by server)
        byte[] sign = ECDSA.Sign(message, (ECPrivateKey) sk);
        boolean verify = ECDSA.Verify(message, sign, (ECPublicKey) pk);
        assertTrue(verify);

        boolean verifyFalse = ECDSA.Verify(invalidMessage, sign, (ECPublicKey) pk);
        assertFalse(verifyFalse);
    }

    @Test
    public void Test_ECDSA_Signatures() throws NoSuchAlgorithmException, InvalidKeySpecException, DecoderException, InvalidKeyException, SignatureException, NoSuchProviderException {
        String sigHex = "304502201d3ea6680d007b751d4e3c1d928a270a1e5ce06cd9b77a46a95542766bb50cb90221008666a33c7e3362a18795d5b96cc36541f8ca79d9190c4341642145d41feb6605";
        String messageHex = "308184020100301006072a8648ce3d020106052b8104000a046d306b0201010420798116c5c26ccfd95e4e13fdf4df9e46cf3629223b190da6c891d48e4de5da57a144034200044552ed599a2d855f59286447d687fbd1ed05793025a7994268f29baef5ca1e3432f9b1d48301a85e4bd8ed77e2c6f3e834f947540b144dbc5a71a548c046c9e2";
        String pkHex = "3056301006072a8648ce3d020106052b8104000a03420004da754f3ede85eec8b7dec3fda5dbdc35662f807f29433e2810743c889de15e1f5d4338453fc13c45e856287cc7849554f92aca832c66a094c7f7f231c50afebf";

        // The messageHex is the SK_t encoded which is signed by the server
        byte[] skBytes = Hex.decodeHex(messageHex);

        // Obtain the PK_A from the PK sent, this is SPKI Encoded since its directly obtained from the server.
        byte[] pkABytes = Hex.decodeHex(pkHex);
        PublicKey pkA = ECDSA.DeserializePublicKey(pkABytes);

        // Convert the Signature to a byte array
        byte[] sigBytes = Hex.decodeHex(sigHex);
        boolean verifySignature = ECDSA.Verify(skBytes, sigBytes, (ECPublicKey) pkA);
        assertTrue(verifySignature);
    }
}