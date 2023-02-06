package com.lcl.lclmeasurementtool.constants

import android.os.Build
import android.util.Base64
import com.lcl.lclmeasurementtool.BuildConfig
import java.nio.charset.StandardCharsets

class IperfConstants {
    companion object {
        const val IC_isDebug = true

        // the SSL key
        // TODO: hide it
        var IC_SSL_PK: String = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwt8Pvsja7c6Co8nsyrCc
            qCYz3liIEUYS1QaoMgefQHRUoIVVi8Gh7/ZAzu6+Jfl/b0qhIb9vgbdKhSYM7lfB
            g2tkdH8tvdH7RzgfItp07+7j8YZd/XpDQKlOV4Ldv7rXhv/LrjlXGj4Zwq77CKdD
            UsAl/MDl83v6NhusqvndxZRBCviEJ38C2H8axVmkjc/rVL8sWuqJ3w8qPuGkuNls
            WElUA6cjjhG1NJMdzrMGGK9SkvtAwcUnNfgfyDvHml0Psaujwf8flhWI/cM42ZOU
            SJCXtDOI5zoS+iijAkDfo8yQ0jMT8O1vYZqnX2ZErw8rcyQ9oLKkY2mHGFOxoZ6M
            TQIDAQAB
            -----END PUBLIC KEY-----
            """.trimIndent()

        const val IC_test_username = BuildConfig.USERNAME
        const val IC_test_password = BuildConfig.PASSWORD

        const val IC_serverAddr = "othello-iperf.westus2.cloudapp.azure.com"
        const val IC_serverPort = 40404

        fun base64Encode(input: String): String {
            val buffer = input.toByteArray(StandardCharsets.US_ASCII)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // return the correct encoding for iperf
                java.util.Base64.getEncoder().encodeToString(buffer)
            } else {
                Base64.encodeToString(buffer, Base64.NO_WRAP)
            }
        }
    }
}