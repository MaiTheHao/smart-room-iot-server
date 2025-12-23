package com.iviet.ivshs.security;

import java.security.PublicKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class VerifyMsgManagement {
	public static boolean verifyMSg(String sSource, PublicKey publicKey, String sSign) {
		byte[] bSign = null;
		byte[] bSource = null;
		byte[] bSourceSha256 = null;
		boolean bVerify = false;
		try {
			bSource = sSource.getBytes("UTF-8");
			bSourceSha256 = DigestUtils.sha256(bSource);
			bSign = Base64.decodeBase64(sSign);
			bVerify = verifing(bSourceSha256, publicKey, bSign);
		} catch (Exception ex) {
			System.out.println("[" + getCurrentDateTimeNow() + "][verifyMSg][" + ex.getMessage() + "]");
		}
		return bVerify;
	}

	private static boolean verifing(byte[] bSourceSha256, PublicKey publicKey, byte[] bSign) {
		Boolean bverify = null;
		try {
			Signature sha256_rsa = Signature.getInstance("SHA256WithRSA");
			sha256_rsa.initVerify(publicKey);
			sha256_rsa.update(bSourceSha256);
			bverify = sha256_rsa.verify(bSign);
		} catch (Exception ex) {
			System.out.println("[" + getCurrentDateTimeNow() + "][verifing][" + ex.getMessage() + "]");
		}
		return bverify;
	}

	private static String getCurrentDateTimeNow() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(cal.getTime());
	}

}
