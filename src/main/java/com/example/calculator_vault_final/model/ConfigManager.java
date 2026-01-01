package com.example.calculator_vault_final.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConfigManager {
	// Hash a 5-digit PIN using SHA-256 and return hex string
	public static String hashPin(String pin) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(pin.getBytes());
			StringBuilder hex = new StringBuilder();
			for (byte b : hash) {
				String h = Integer.toHexString(0xff & b);
				if (h.length() == 1) hex.append('0');
				hex.append(h);
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 not available", e);
		}
	}

	// Verify input PIN against stored hash
	public static boolean verifyPin(String inputPin, String storedHash) {
		return hashPin(inputPin).equals(storedHash);
	}
}
