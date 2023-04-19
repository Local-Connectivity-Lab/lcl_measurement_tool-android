package com.lcl.lclmeasurementtool.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class SecurityUtil {
    companion object {
        // RSA algorithm
        const val RSA = "RSA"

        // SHA256 with RSA signature
        const val SHA_256_WITH_RSA_SIGNATURE = "SHA256withRSA"

        // SHA-256
        const val SHA_256_HASH = "SHA-256"

        /**
         * Digest the message with given algorithm
         *
         * @param data          the data to be hashed
         * @param algorithm     the algorithm used to digest the message
         * @return              the digested message in byte array
         * @throws NoSuchAlgorithmException if the provided algorithm doesn't exist
         */
        @Throws(NoSuchAlgorithmException::class)
        fun digest(data: ByteArray, algorithm: String): ByteArray {
            val messageDigest = MessageDigest.getInstance(algorithm)
            messageDigest.update(data)
            return messageDigest.digest()
        }
    }
}