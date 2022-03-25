package com.lcl.lclmeasurementtool.Managers;

import android.location.Location;

import com.lcl.lclmeasurementtool.Utils.DecoderException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

/**
 * A callback handling the location update
 */
public interface LocationUpdatesListener {
    // called when a new location is fetched
    void onUpdate(Location location) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableEntryException, SignatureException, InvalidKeyException, DecoderException, InvalidKeySpecException, NoSuchProviderException;
}
