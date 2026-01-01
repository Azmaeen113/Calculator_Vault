package com.example.calculator_vault_final.service;

import com.example.calculator_vault_final.model.DatabaseManager;
import com.example.calculator_vault_final.model.VaultFile;
import org.apache.commons.io.FileUtils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileVaultService {
	private static final List<File> TEMP_FILES = new ArrayList<>();
	private final DatabaseManager db;

	public FileVaultService(DatabaseManager db) {
		this.db = db;
		Runtime.getRuntime().addShutdownHook(new Thread(FileVaultService::deleteTempFiles));
	}

	// XOR encryption/decryption helpers
	public static byte[] encryptData(byte[] data, String pin) {
		return xorWithKey(data, pin.getBytes());
	}

	public static byte[] decryptData(byte[] data, String pin) {
		return xorWithKey(data, pin.getBytes());
	}

	private static byte[] xorWithKey(byte[] data, byte[] key) {
		byte[] out = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			out[i] = (byte) (data[i] ^ key[i % key.length]);
		}
		return out;
	}

	public void saveFileToVault(File file, String pin) throws Exception {
		byte[] bytes = FileUtils.readFileToByteArray(file);
		byte[] enc = encryptData(bytes, pin);
		VaultFile vf = new VaultFile(null,
				file.getName(),
				getExtension(file.getName()),
				enc,
				bytes.length,
				LocalDateTime.now());
		db.saveFile(vf);
	}

	public byte[] retrieveFileFromVault(int fileId, String pin) throws Exception {
		byte[] enc = db.getFileData(fileId);
		if (enc == null) return null;
		return decryptData(enc, pin);
	}

	public File createTempFile(VaultFile vaultFile, byte[] decryptedData) throws IOException {
		String base = vaultFile.getFileName();
		Path temp = Files.createTempFile("vault_", "_" + base);
		Files.write(temp, decryptedData);
		File f = temp.toFile();
		f.deleteOnExit();
		TEMP_FILES.add(f);
		return f;
	}

	public static void deleteTempFiles() {
		for (File f : new ArrayList<>(TEMP_FILES)) {
			try {
				Files.deleteIfExists(f.toPath());
			} catch (IOException ignored) {}
		}
		TEMP_FILES.clear();
	}

	public static String formatFileSize(long bytes) {
		double kb = bytes / 1024.0;
		if (kb < 1024) return String.format("%.1f KB", kb);
		double mb = kb / 1024.0;
		return String.format("%.1f MB", mb);
	}

	public void reEncryptFile(VaultFile vaultFile, String oldPin, String newPin) throws Exception {
		byte[] enc = vaultFile.getFileData();
		if (enc == null) enc = db.getFileData(vaultFile.getId());
		byte[] dec = decryptData(enc, oldPin);
		byte[] re = encryptData(dec, newPin);
		// Re-encryption is now handled through DatabaseManager's updateAllFilesEncryption
		db.updateAllFilesEncryption(oldPin, newPin);
	}

	public static void openFileWithDesktop(File file) throws IOException {
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().open(file);
		} else {
			throw new IOException("Desktop integration not supported");
		}
	}

	private static String getExtension(String name) {
		int idx = name.lastIndexOf('.');
		return idx >= 0 ? name.substring(idx + 1) : "";
	}
}
