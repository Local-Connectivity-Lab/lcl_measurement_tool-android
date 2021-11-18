package com.lcl.lclmeasurementtool.Managers;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class KeyStoreManager {

    private static KeyStoreManager instance;

    private static final String TAG = "KeyStoreManager";

    private static final String provider = "AndroidKeyStore";
    private static final String alias = "KEYPAIR";

    private final KeyStore ks;

    @RequiresApi(api = Build.VERSION_CODES.P)
    private KeyStoreManager()
            throws NoSuchProviderException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException {
        ks = KeyStore.getInstance(provider);
        ks.load(null);
        generate();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static KeyStoreManager getInstance() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException {
        if (instance == null) {
            instance = new KeyStoreManager();
        }

        return instance;
    }

    private KeyStore.PrivateKeyEntry getEntry() throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(alias, null);
        if (entry == null) {
            Log.e(TAG, "key extraction failed");
            return null;
        }

        return (KeyStore.PrivateKeyEntry) entry;
    }

    public byte[] getPublicKey() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableEntryException {
        return getPublicKeyObject().getEncoded();
    }

    public PublicKey getPublicKeyObject() throws NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, KeyStoreException, IOException {
        KeyStore.PrivateKeyEntry entry = getEntry();
        if (entry == null) {
            return null;
        }

        return entry.getCertificate().getPublicKey();
    }

    // if the attestation has only one key, then it means the device doesn't support TEE
    public byte[][] getAttestation() throws NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, KeyStoreException, IOException {
        KeyStore.PrivateKeyEntry entry = getEntry();
        if (entry == null) return null;
        X509Certificate[] chains = (X509Certificate[]) entry.getCertificateChain();

        byte[][] buffer = new byte[chains.length][];
        int i = 0;
        for (X509Certificate chain : chains) {
            buffer[i++] = chain.getEncoded();
        }

        return buffer;
    }

    public PrivateKey getPrivateKey() throws NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, KeyStoreException, IOException {
        KeyStore.PrivateKeyEntry entry = getEntry();
        if (entry == null) return null;

        return entry.getPrivateKey();
    }

    public boolean contains() throws KeyStoreException {
        return ks.containsAlias(alias);
    }

    public void delete() throws KeyStoreException {
        ks.deleteEntry(alias);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void generate() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, provider);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
//                .setIsStrongBoxBacked(true)  => unable on LG phone and Pixel?
                .build();
        generator.initialize(spec);
        KeyPair keyPair = generator.generateKeyPair();
    }
}