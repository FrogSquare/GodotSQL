
package org.godotengine.godot.data;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESObfuscator {
	private static final String SECRET = "njkcndwiu63fjnp9";

	private static final String UTF8 = "UTF-8";
	private static final String KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC";
	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final byte[] IV =
		{ 16, 74, 71, -80, 32, 101, -47, 72, 117, -14, 0, -29, 70, 65, -12, 74 };

	private static final String header = "AESObfuscator-1|";

	private Cipher mEncryptor;
	private Cipher mDecryptor;

	public AESObfuscator(byte[] salt, String applicationId, String deviceId) {
		byte[] passwordData = null;

		String sec = SECRET;
		try {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
		KeySpec keySpec =
		new PBEKeySpec((applicationId + deviceId + sec).toCharArray(), salt, 1024, 256);

		passwordData = factory.generateSecret(keySpec).getEncoded();
		}
		catch (GeneralSecurityException e) {
		// MJackUtils.LogDebug("Probably an incompatible device. Trying different approach.");

		MessageDigest digester = null;
			try {
			digester = MessageDigest.getInstance("MD5");
			char[] password = (applicationId + deviceId + sec).toCharArray();

			for (int i = 0; i < password.length; i++) {
				digester.update((byte) password[i]);
			}

			passwordData = digester.digest();
			}
			catch (NoSuchAlgorithmException e1) {
			throw new RuntimeException("Invalid environment", e1);
			}
		}

		SecretKey secret = new SecretKeySpec(passwordData, "AES");
		try {
		mEncryptor = Cipher.getInstance(CIPHER_ALGORITHM);
		mEncryptor.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(IV));

		mDecryptor = Cipher.getInstance(CIPHER_ALGORITHM);
		mDecryptor.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(IV));
		}
		catch (GeneralSecurityException e) {
		throw new RuntimeException("Invalid environment 2", e);
		}

	}

	public String obfuscateInt(int original) { 
		return obfuscateString("" + original);
	}

	public synchronized String obfuscateString(String original) {
		if (TextUtils.isEmpty(original)) { return original; }

		try {
		return Base64.encode(mEncryptor.doFinal((header + original).getBytes(UTF8)));
		}
		catch (UnsupportedEncodingException e) {
		throw new RuntimeException("Invalid environment", e);
		}
		catch (GeneralSecurityException e) {
		throw new RuntimeException("Invalid environment", e);
		}
	}

	public int unobfuscateToInt(String obfuscated) throws ValidationException {
		return Integer.parseInt(unobfuscateToString(obfuscated));
	}

	public synchronized String unobfuscateToString(String obfuscated) throws ValidationException {
		if (TextUtils.isEmpty(obfuscated)) { return null; }

		try {
		String result = new String(mDecryptor.doFinal(Base64.decode(obfuscated)), UTF8);

		int headerIndex = result.indexOf(header);
		if (headerIndex != 0) {
		throw new ValidationException("Header not found (invalid data or key)" + ":" +
											obfuscated);
		}

		return result.substring(header.length(), result.length());
		}
		catch (Base64DecoderException e) {
		throw new ValidationException(e.getMessage() + ":" + obfuscated);
		}
		catch (IllegalBlockSizeException e) {
		throw new ValidationException(e.getMessage() + ":" + obfuscated);
		}
		catch (BadPaddingException e) {
		throw new ValidationException(e.getMessage() + ":" + obfuscated);
		}
		catch (UnsupportedEncodingException e) {
		throw new RuntimeException("Invalid environment", e);
		}
	}

	public class ValidationException extends Exception {
		public ValidationException() {
			super();
		}

		public ValidationException(String s) {
			super(s);
		}

		 private static final long serialVersionUID = 1L;
	}
}

