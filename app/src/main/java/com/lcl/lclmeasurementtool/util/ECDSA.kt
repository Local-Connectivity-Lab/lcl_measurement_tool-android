package com.lcl.lclmeasurementtool.util

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.IOException
import java.math.BigInteger
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.*
import java.util.*

/**
 * ECDSA helper class that provides convenient helper functions to assist cryptographic operations
 * related to EC algorithm
 */
class ECDSA {

    // This is a custom easy helper implementation for the EC prime256v1 or the secp256r1 curves.
    // The raw ASN.1 Encoding structures will contain the necessary metadata for interoperation, it would need corresponding
    // constant size changes below as necessary to support non 256 bit length curves.
    companion object {

        // statically load the BC provider
        init {
            Security.removeProvider("BC")
            Security.addProvider(BouncyCastleProvider())
        }

        // the EC algorithm
        private const val ALGORITHM = "EC"

        // SHA256 with ECDSA
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"

        // public key encoding size
        private const val PublicKeyEncodingSize = 65 // 64 + 1 byte 0x04

        private const val PROVIDER = "BC"

        // padding
        private const val PADDING: Byte = 0x04


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
        @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class, NoSuchProviderException::class)
        fun DerivePublicKey(sk: ECPrivateKey): ECPublicKey {
            val kf = getKeyFactory()
            val keyParams = sk.params
            val pkBytesEmbedded = ByteArray(PublicKeyEncodingSize)
            sk.encoded.copyInto(destination = pkBytesEmbedded, destinationOffset = 0, startIndex = sk.encoded.size - PublicKeyEncodingSize, endIndex = sk.encoded.size)
            val p = decodePoint(pkBytesEmbedded, keyParams.curve)
            val pkSpec = ECPublicKeySpec(p, keyParams)
            return kf.generatePublic(pkSpec) as ECPublicKey
        }

        /**
         * Deserialize the private key from raw bytes
         *
         * @param raw  the raw bytes to convert to private key
         * @return     an EC private key corresponds to the bytes
         * @throws InvalidKeySpecException    if the key gen process failed because of invalid key spec
         */
        @Throws(InvalidKeySpecException::class)
        fun DeserializePrivateKey(raw: ByteArray): ECPrivateKey {
           return decodePKCS8ECPrivateKey(raw)
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
        @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class,
            NoSuchProviderException::class)
        fun DeserializePublicKey(rawSPKIEncoded: ByteArray): ECPublicKey {
            return getKeyFactory().generatePublic(X509EncodedKeySpec(rawSPKIEncoded)) as ECPublicKey
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
        @Throws(
            NoSuchAlgorithmException::class,
            InvalidKeyException::class,
            SignatureException::class,
            NoSuchProviderException::class
        )
        fun Verify(message: ByteArray, signature: ByteArray, pk: ECPublicKey): Boolean {
            val s = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER)
            s.initVerify(pk)
            s.update(message)
            return s.verify(signature)
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
        @Throws(
            NoSuchAlgorithmException::class,
            InvalidKeyException::class,
            SignatureException::class,
            NoSuchProviderException::class
        )
        fun Sign(message: ByteArray, sk: ECPrivateKey): ByteArray {
            val s = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER)
            s.initSign(sk)
            s.update(message)
            return s.sign()
        }

        /////////////////////////////////////  ECUtils  //////////////////////////////////////////////

        /**
         * Decode the bytes data using the EC algorithm
         * @param data    the bytes data to be decoded
         * @param curve   the EllipticCurve used to decode the data
         * @return        an EC point corresponds to the raw bytes
         * @throws IOException  when the data is ill-formatted
         */
        @Throws(IOException::class)
        private fun decodePoint(data: ByteArray, curve: EllipticCurve): ECPoint {
            if (data.isEmpty() || data[0].compareTo(4) != 0) {
                throw IOException("Only uncompressed point format supported")
            }

            // Per ANSI X9.62, an encoded point is a 1 byte type followed by
            // ceiling(log base 2 field-size / 8) bytes of x and the same of y.
            val n = (data.size - 1) / 2
            if (n != ((curve.field.fieldSize + 7) shr 3)) {
                throw IOException("Point does not match field size")
            }

            val xb = data.copyOfRange(1, n + 1)
            val yb = data.copyOfRange(n + 1, n + 1 + n)
            return ECPoint(BigInteger(1, xb), BigInteger(1, yb))
        }

        /**
         * Decode the PKCS8 private key
         * @param encoded the encoded bytes of the private key in PKCS8 format
         * @return        the private key associated with the raw bytes
         * @throws InvalidKeySpecException  if the key spec is invalid
         */
        @Throws(InvalidKeySpecException::class)
        private fun decodePKCS8ECPrivateKey(encoded: ByteArray): ECPrivateKey {
            val kf = getKeyFactory()
            val keySpec = PKCS8EncodedKeySpec(encoded)
            return kf.generatePrivate(keySpec) as ECPrivateKey
        }

        /**
         * Return the key factory for the BC algorithm
         * @return  a key factor corresponds to the BC algorithm
         */
        private fun getKeyFactory(): KeyFactory {
            try {
                return KeyFactory.getInstance(ALGORITHM, PROVIDER)
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            } catch (e: NoSuchProviderException) {
                throw RuntimeException(e)
            }
        }
    }

}