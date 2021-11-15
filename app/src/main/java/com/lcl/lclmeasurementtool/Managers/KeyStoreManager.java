package com.lcl.lclmeasurementtool.Managers;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

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

public class KeyStoreManager {

    private static KeyStoreManager instance;

    private static final String TAG = "KeyStoreManager";

    private static final String provider = "AndroidKeyStore";
    private static final String alias = "KEYPAIR";

    private final KeyStore ks;

    private KeyStoreManager()
            throws NoSuchProviderException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException {
        ks = KeyStore.getInstance(provider);
        ks.load(null);
        generate();
    }

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

        KeyStore.PrivateKeyEntry entry = getEntry();
        if (entry == null) {
            Log.e(TAG, "key extraction failed");
            return null;
        }

        return entry.getCertificate().getPublicKey().getEncoded();
    }

    public PrivateKey getPrivateKey() throws NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, KeyStoreException, IOException {
        KeyStore.PrivateKeyEntry entry = getEntry();

        return entry.getPrivateKey();
    }

    public boolean contains() throws KeyStoreException {
        return ks.containsAlias(alias);
    }

    public void delete() throws KeyStoreException {
        ks.deleteEntry(alias);
    }

    public void generate() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, provider);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .build();
        generator.initialize(spec);
        KeyPair keyPair = generator.generateKeyPair();
    }
}
