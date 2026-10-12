package org.springframework.samples.petclinic.auth;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Service;

@Service
public class PasswordService {

	private static final int ITERATIONS = 120_000;

	private static final int KEY_LENGTH = 256;

	private final SecureRandom secureRandom = new SecureRandom();

	public String hash(String password) {
		byte[] salt = new byte[16];
		secureRandom.nextBytes(salt);
		byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
		return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$"
			+ Base64.getEncoder().encodeToString(hash);
	}

	public boolean matches(String password, String storedHash) {
		try {
			String[] parts = storedHash.split("\\$");
			if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
				return false;
			}
			int iterations = Integer.parseInt(parts[1]);
			byte[] salt = Base64.getDecoder().decode(parts[2]);
			byte[] expected = Base64.getDecoder().decode(parts[3]);
			byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);
			return constantTimeEquals(expected, actual);
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
		try {
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
			return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			throw new IllegalStateException("Unable to hash password", ex);
		}
	}

	private boolean constantTimeEquals(byte[] left, byte[] right) {
		if (left.length != right.length) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < left.length; i++) {
			result |= left[i] ^ right[i];
		}
		return result == 0;
	}

}
