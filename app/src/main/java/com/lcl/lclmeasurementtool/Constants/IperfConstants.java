package com.lcl.lclmeasurementtool.Constants;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class IperfConstants {

    public static boolean IC_isDebug = true;

    public static String IC_SSL_PK = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwt8Pvsja7c6Co8nsyrCc\n" +
            "qCYz3liIEUYS1QaoMgefQHRUoIVVi8Gh7/ZAzu6+Jfl/b0qhIb9vgbdKhSYM7lfB\n" +
            "g2tkdH8tvdH7RzgfItp07+7j8YZd/XpDQKlOV4Ldv7rXhv/LrjlXGj4Zwq77CKdD\n" +
            "UsAl/MDl83v6NhusqvndxZRBCviEJ38C2H8axVmkjc/rVL8sWuqJ3w8qPuGkuNls\n" +
            "WElUA6cjjhG1NJMdzrMGGK9SkvtAwcUnNfgfyDvHml0Psaujwf8flhWI/cM42ZOU\n" +
            "SJCXtDOI5zoS+iijAkDfo8yQ0jMT8O1vYZqnX2ZErw8rcyQ9oLKkY2mHGFOxoZ6M\n" +
            "TQIDAQAB\n" +
            "-----END PUBLIC KEY-----";

    public static String IC_test_username = "secrettestuser";
    public static String IC_test_password = "secrettestuser";

    public static String IC_serverAddr = "http://othello-iperf.westus2.cloudapp.azure.com";
    public static int IC_serverPort = 40404;


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String Base64Encode(String input) {
        byte[] buffer = input.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(buffer);
    }
}
