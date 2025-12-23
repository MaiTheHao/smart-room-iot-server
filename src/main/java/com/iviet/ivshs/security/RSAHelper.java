package com.iviet.ivshs.security;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class RSAHelper {
	/**
	 * Encrypt the plain text using public key.
	 * 
	 * @param data : original plain text
	 * @param key  :The public key
	 * @return Encrypted text
	 * @throws java.lang.Exception
	 */
	public static byte[] encrypt(byte[] data, PublicKey key) {
		byte[] cipherText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText;
	}
	
	/**
	 * Decrypt text using private key.
	 * 
	 * @param data : encrypted text
	 * @param key :The private key
	 * @return plain text
	 * @throws java.lang.Exception
	 */
	public static byte[] decrypt(byte[] data, PrivateKey key) {
		byte[] dectyptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, key);
			dectyptedText = cipher.doFinal(data);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dectyptedText;
		//return new String(dectyptedText);
	}
}
