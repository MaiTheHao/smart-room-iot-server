package com.iviet.ivshs.security;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class RsaAesHelper {
	
	/**
	 * Encrypt data
	 * @param objSource
	 * @param keyPassword
	 * @param publicKey
	 * @return
	 * String
	 */
	public synchronized static String encryptAESRSA(Object objSource, String keyPassword, PublicKey publicKey) {
		byte[] bSource = null;
		String base64Return = null;

		try {
			bSource = objSource.toString().getBytes();
			// 1.Generating Initialization vector (IV)
			int ivSize = 16;
			byte[] iv = new byte[ivSize];
			SecureRandom random = new SecureRandom();
			random.nextBytes(iv);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// 2. Get SecretKey
			SecretKeySpec secretKeySpec = getSecretKeySpec(keyPassword);

			// 3.Encrypt bSource
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] enSource = cipher.doFinal(bSource);

			// 4.Combine ivParameterSpec and enSource encrypted
			byte[] comIVandEnSource = new byte[ivSize + enSource.length];
			System.arraycopy(iv, 0, comIVandEnSource, 0, ivSize);
			System.arraycopy(enSource, 0, comIVandEnSource, ivSize, enSource.length);

			// 5.Encrypt SecretKey with PublicKey
			byte[] enSecretKey = RSAHelper.encrypt(secretKeySpec.getEncoded(), publicKey);

			// 6. Combine enSecretKey and comIVandEnSource
			byte[] comReturn = new byte[enSecretKey.length + comIVandEnSource.length];
			System.arraycopy(enSecretKey, 0, comReturn, 0, enSecretKey.length);
			System.arraycopy(comIVandEnSource, 0, comReturn, enSecretKey.length, comIVandEnSource.length);

			// 7. Convert Byte Array to Base64
			base64Return = Base64.encodeBase64String(comReturn);
		} catch (Exception ex) {
			System.out.println("[" + System.currentTimeMillis() + "][encrypt_Msg][" + ex.getMessage() + "]");
		}
		return base64Return;
	}

	/**
	 * Decrypt data
	 * @param objSource
	 * @param privateKey
	 * @param publicKey
	 * @return
	 * String
	 */
	public synchronized static String decryptAESRSA(Object objSource, PrivateKey privateKey, PublicKey publicKey) {
		int ivSize = 16;
		int keyLength = 0;
		byte[] bSource = null;
		String deReturn = null;
		try {
			// 1. Get KeyLength of PublicKey
			RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
			keyLength = rsaPublicKey.getModulus().bitLength() / 8;

			// 2. Decode to Base64 to Byte Array
			bSource = Base64.decodeBase64(objSource.toString());

			// 3. Extract Encrypt SecretKey
			byte[] enSecretKey = new byte[keyLength];
			System.arraycopy(bSource, 0, enSecretKey, 0, keyLength);

			// 4. Extract IV
			byte[] iv = new byte[ivSize];
			System.arraycopy(bSource, keyLength, iv, 0, iv.length);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// 5. Extract encrypted part
			int encryptedSize = bSource.length - ivSize - keyLength;
			byte[] encryptedBytes = new byte[encryptedSize];
			System.arraycopy(bSource, keyLength + ivSize, encryptedBytes, 0, encryptedSize);

			// 6. Get SecretKey
			byte[] decryptedKey = RSAHelper.decrypt(enSecretKey, privateKey);
			SecretKeySpec originalKey = new SecretKeySpec(decryptedKey, 0, decryptedKey.length, "AES");

			// 7. Decrypt
			Cipher cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipherDecrypt.init(Cipher.DECRYPT_MODE, originalKey, ivParameterSpec);
			byte[] decrypted = cipherDecrypt.doFinal(encryptedBytes);
			deReturn = new String(decrypted, "UTF-8");
		} catch (Exception ex) {
			System.out.println("[" + System.currentTimeMillis() + "][decrypt_Msg][" + ex.getMessage() + "]");
		}
		return deReturn;
	}

	/**
	 * Get secret key of AES
	 * @param password
	 * @return
	 * @throws Exception
	 * SecretKeySpec
	 */
	private static SecretKeySpec getSecretKeySpec(String password) throws Exception {
		byte[] salt = new byte[16];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(salt);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 128, 256);
		SecretKey tmp = factory.generateSecret(keySpec);
		SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");
		return secretKeySpec;
	}

}
