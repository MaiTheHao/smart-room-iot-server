package com.iviet.ivshs.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class KeyManagement {
	
	/**
	 * Find public key and private key into keystore
	 * @param pathFile
	 * @param keyStorePass
	 * @param alias
	 * @return
	 * KeyPair
	 */
	public KeyPair findKeyPair(String pathFile, String keyStorePass , String alias) {
		try {
			File keystoreFile = new File(pathFile);
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(new FileInputStream(keystoreFile), keyStorePass.toCharArray());
			Key key = keystore.getKey(alias, keyStorePass.toCharArray());
			if (key instanceof PrivateKey) {
				// Get certificate of public key
				Certificate cert = keystore.getCertificate(alias);
				// Get public key
				PublicKey publicKey = cert.getPublicKey();
				return new KeyPair(publicKey, (PrivateKey) key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public PublicKey findPublicKeyFromCer(String pathCerFile) {
		PublicKey publicKey = null;
		File cerFile = new File(pathCerFile);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(cerFile);
			CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
			Certificate  certificate = cerFactory.generateCertificate(fis);
			publicKey = certificate.getPublicKey();
			fis.close();
		} catch (CertificateException | IOException e) {
			e.printStackTrace();
		}
		return publicKey;
	}
}
