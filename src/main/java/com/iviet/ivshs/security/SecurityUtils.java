package com.iviet.ivshs.security;

import java.security.KeyPair;
import java.security.PublicKey;

import org.apache.commons.lang3.RandomStringUtils;

import com.iviet.ivshs.constant.ConstantUtil;

public class SecurityUtils {
	
	
	private static KeyPair sourceKeyPair = new KeyManagement().findKeyPair(ConstantUtil.C_SOURCE_KEYSTORE_FILE,
			ConstantUtil.C_SOURCE_KEYSTORE_PASSWORD, ConstantUtil.C_SOURCE_KEYSTORE_ALIAS);
	private static PublicKey destinationPublicKey = new KeyManagement().findPublicKeyFromCer(ConstantUtil.C_DES_CER_FILE);
	
	
	/**
	 * Encrypt text by RAS and AES
	 * @param cleartext
	 * @return encrypted text
	 */
	public static String encryptAll(String cleartext) {
		String aesPassword = RandomStringUtils.randomNumeric(6);
		return RsaAesHelper.encryptAESRSA(cleartext, aesPassword, destinationPublicKey);
	}
	
	/**
	 * Decrypt text by RAS and AES
	 * @param encryptText
	 * @return
	 */
	public static String decryptAll(String encryptText) {
		return RsaAesHelper.decryptAESRSA(encryptText, sourceKeyPair.getPrivate(), sourceKeyPair.getPublic());
	}
}
