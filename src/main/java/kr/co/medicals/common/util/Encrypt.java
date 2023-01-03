package kr.co.medicals.common.util;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class Encrypt {
    public static String sha256(String param) {
        StringBuffer sha = new StringBuffer();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(param.getBytes());

            for (int i = 0; i < digest.length; i++) {
                byte temp = digest[i];
                String str = (Integer.toString((temp & 0xff) + 0x100, 16).substring(1));
                while (str.length() < 2) {
                    str = "0" + str;
                }
                str = str.substring(str.length() - 2);
                sha.append(str);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sha.toString();
    }

    public static String getHmac(String message, String apiKey) {
        String secretKey = apiKey;
        String checkHash = null;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            checkHash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return checkHash;
    }


}
