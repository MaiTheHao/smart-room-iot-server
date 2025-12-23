package com.iviet.ivshs.security;

import java.security.PrivateKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class SignMsgUtil {
	public static String signMsg (String sSource, PrivateKey privateKey) {
		byte[] bSource = null;
        byte[] bSourceSha256 = null;
        byte[] bSign = null;
        String bSignBase64 = null;
        try {
            bSource = sSource.getBytes("UTF-8");
            bSourceSha256 = DigestUtils.sha256(bSource);
            bSign = signing(bSourceSha256, privateKey);
            bSignBase64 = Base64.encodeBase64String(bSign);
        } catch (Exception ex) {
            System.out.println("[" + getCurrentDateTimeNow() + "][signMSg][" + ex.getMessage() + "]");
        }
        return bSignBase64;
	}
	
	private static byte[] signing(byte[] bSourceSha256, PrivateKey privateKey) {
        byte[] bSign = null;
        try {
            Signature sha256_rsa = Signature.getInstance("SHA256WithRSA");
            sha256_rsa.initSign(privateKey);         
            sha256_rsa.update(bSourceSha256);
            bSign = sha256_rsa.sign();
        } catch (Exception ex) {
            System.out.println("[" + getCurrentDateTimeNow() + "][signing][" + ex.getMessage() + "]");
        }
        return bSign;
    }
    
    private static String getCurrentDateTimeNow() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }

}
