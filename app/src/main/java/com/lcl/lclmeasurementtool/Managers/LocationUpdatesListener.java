package com.lcl.lclmeasurementtool.Managers;

import android.location.Location;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public interface LocationUpdatesListener {
    void onUpdate(Location location) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableEntryException, SignatureException, InvalidKeyException, DecoderException, InvalidKeySpecException;
}
