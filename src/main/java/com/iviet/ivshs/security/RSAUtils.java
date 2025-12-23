package com.iviet.ivshs.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

/**
 * @author JavaDigest
 * 
 */
public class RSAUtils {
	private static final String ALGORITHM = "RSA";
	private static final String PRIVATE_KEY_FILE = "/private.key";
	private static final String PUBLIC_KEY_FILE = "/public.key";

	/**
	 * Generate key which contains a pair of private and public key using 1024
	 * bytes. Store the set of keys in Prvate.key and Public.key files.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private static void generateKey() {
		try {
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
			keyGen.initialize(1024);
			final KeyPair key = keyGen.generateKeyPair();

			File privateKeyFile = new File(PRIVATE_KEY_FILE);
			File publicKeyFile = new File(PUBLIC_KEY_FILE);

			// Create files to store public and private key
			if (privateKeyFile.getParentFile() != null) {
				privateKeyFile.getParentFile().mkdirs();
			}
			privateKeyFile.createNewFile();

			if (publicKeyFile.getParentFile() != null) {
				publicKeyFile.getParentFile().mkdirs();
			}
			publicKeyFile.createNewFile();

			// Saving the Public key in a file
			ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publicKeyFile));
			publicKeyOS.writeObject(key.getPublic());
			publicKeyOS.close();

			// Saving the Private key in a file
			ObjectOutputStream privateKeyOS = new ObjectOutputStream(new FileOutputStream(privateKeyFile));
			privateKeyOS.writeObject(key.getPrivate());
			privateKeyOS.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * The method checks if the pair of public and private key has been
	 * generated.
	 * 
	 * @return flag indicating if the pair of keys were generated.
	 */
	private static boolean areKeysPresent() {
		File privateKey = new File(PRIVATE_KEY_FILE);
		File publicKey = new File(PUBLIC_KEY_FILE);
		if (privateKey.exists() && publicKey.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * Encrypt the plain text using public key.
	 * 
	 * @param text : original plain text
	 * @param key  :The public key
	 * @return Encrypted text
	 * @throws java.lang.Exception
	 */
	public static byte[] encrypt(String text, PublicKey key) {
		byte[] cipherText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			// encrypt the plain text using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText;
	}

	/**
	 * Decrypt text using private key.
	 * 
	 * @param text
	 *            :encrypted text
	 * @param key
	 *            :The private key
	 * @return plain text
	 * @throws java.lang.Exception
	 */
	public static String decrypt(byte[] text, PrivateKey key) {
		byte[] dectyptedText = null;
		try {
			// get an RSA cipher object and print the provider
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

			// decrypt the text using the private key
			cipher.init(Cipher.DECRYPT_MODE, key);
			dectyptedText = cipher.doFinal(text);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return new String(dectyptedText);
	}
	
	public static byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf) {
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}

	private final static String HEX = "0123456789ABCDEF";

	private static void appendHex(StringBuffer sb, byte b) {
		sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
	}

	/**
	 * Test the EncryptionUtil
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			 //Check if the pair of keys are present else generate those.
			 if (!areKeysPresent()) {
				 //Method generates a pair of keys using the RSA algorithm and
				 //stores it
				 //in their respective files
				 generateKey();
			 }

			final String originalText = "Text to be encrypted ";
			ObjectInputStream inputStream = null;

			// Encrypt the string using the public key
			inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
			final PublicKey publicKey = (PublicKey) inputStream.readObject();
			final byte[] cipherText = encrypt(originalText, publicKey);

			// Decrypt the cipher text using the private key.
			inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
			final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
			final String plainText = decrypt(cipherText, privateKey);

			// Printing the Original, Encrypted and Decrypted Text
			System.out.println("Original: " + originalText);
			System.out.println("Encrypted: " + cipherText.toString());
			System.out.println("Decrypted: " + plainText);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
