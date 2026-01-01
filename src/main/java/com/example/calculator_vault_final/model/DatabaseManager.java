package com.example.calculator_vault_final.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
	private static final String JDBC_URL;
	
	static {
		// Get the database path from resources folder
		String dbPath;
		try {
			URL resourceUrl = DatabaseManager.class.getResource("/vault.db");
			if (resourceUrl != null) {
				// Running from IDE or when resource is available
				dbPath = new File(resourceUrl.toURI()).getAbsolutePath();
			} else {
				// Fallback: use current working directory's resources
				File resourceFile = new File("src/main/resources/vault.db");
				if (resourceFile.exists()) {
					dbPath = resourceFile.getAbsolutePath();
				} else {
					// Last fallback: create in target/classes
					File targetFile = new File("target/classes/vault.db");
					targetFile.getParentFile().mkdirs();
					dbPath = targetFile.getAbsolutePath();
				}
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException("Failed to locate vault.db", e);
		}
		JDBC_URL = "jdbc:sqlite:" + dbPath;
		System.out.println("Using database at: " + dbPath);
	}

	public DatabaseManager() {
		initializeDatabase();
	}

	public void initializeDatabase() {
		try {
			try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
				try (Statement stmt = conn.createStatement()) {
					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS config (" +
							"id INTEGER PRIMARY KEY, " +
							"pin_hash TEXT, " +
							"is_first_time BOOLEAN, " +
							"created_at TIMESTAMP"
							+ ")");

					stmt.executeUpdate("CREATE TABLE IF NOT EXISTS vault_files (" +
							"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
							"file_name TEXT, " +
							"original_extension TEXT, " +
							"file_data BLOB, " +
							"file_size LONG, " +
							"uploaded_at TIMESTAMP"
							+ ")");
				}

				// Seed config row if empty
				try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM config")) {
					ResultSet rs = ps.executeQuery();
					if (rs.next() && rs.getInt(1) == 0) {
						try (PreparedStatement ins = conn.prepareStatement("INSERT INTO config (id, pin_hash, is_first_time, created_at) VALUES (1, NULL, 1, ?)")) {
							ins.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
							ins.executeUpdate();
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize database", e);
		}
	}

	public void setPinHash(String pinHash) throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL);
			 PreparedStatement ps = conn.prepareStatement("UPDATE config SET pin_hash = ?, is_first_time = 0 WHERE id = 1")) {
			ps.setString(1, pinHash);
			int rows = ps.executeUpdate();
			if (rows == 0) {
				throw new SQLException("Failed to set PIN hash");
			}
		}
	}

	public boolean updatePinHash(String oldPinHash, String newPinHash) throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL);
			 PreparedStatement ps = conn.prepareStatement("UPDATE config SET pin_hash = ? WHERE id = 1 AND pin_hash = ?")) {
			ps.setString(1, newPinHash);
			ps.setString(2, oldPinHash);
			return ps.executeUpdate() > 0;
		}
	}

	public String getPinHash() throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL);
			 PreparedStatement ps = conn.prepareStatement("SELECT pin_hash FROM config WHERE id = 1")) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
			return null;
		}
	}

	public boolean isFirstTime() throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL);
			 PreparedStatement ps = conn.prepareStatement("SELECT is_first_time FROM config WHERE id = 1")) {
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getBoolean(1);
			}
			return true;
		}
	}

	public void saveFile(VaultFile file) throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL);
			 PreparedStatement ps = conn.prepareStatement(
					 "INSERT INTO vault_files (file_name, original_extension, file_data, file_size, uploaded_at) VALUES (?,?,?,?,?)")) {
			ps.setString(1, file.getFileName());
			ps.setString(2, file.getOriginalExtension());
			ps.setBytes(3, file.getFileData());
			ps.setLong(4, file.getFileSize());
			ps.setTimestamp(5, Timestamp.valueOf(file.getUploadedAt() != null ? file.getUploadedAt() : LocalDateTime.now()));
			ps.executeUpdate();
		}
	}

	public List<VaultFile> getAllFiles() throws SQLException {
		List<VaultFile> files = new ArrayList<>();
		try (Connection conn = DriverManager.getConnection(JDBC_URL);
			 PreparedStatement ps = conn.prepareStatement(
					 "SELECT id, file_name, original_extension, file_size, uploaded_at FROM vault_files ORDER BY uploaded_at DESC")) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				VaultFile vf = new VaultFile();
				vf.setId(rs.getInt("id"));
				vf.setFileName(rs.getString("file_name"));
				vf.setOriginalExtension(rs.getString("original_extension"));
				vf.setFileSize(rs.getLong("file_size"));
				Timestamp ts = rs.getTimestamp("uploaded_at");
				vf.setUploadedAt(ts != null ? ts.toLocalDateTime() : null);
				files.add(vf);
			}
		}
		return files;
	}

	public byte[] getFileData(int fileId) throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL);
			 PreparedStatement ps = conn.prepareStatement("SELECT file_data FROM vault_files WHERE id = ?")) {
			ps.setInt(1, fileId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getBytes(1);
			}
			return null;
		}
	}

	public void deleteFile(int fileId) throws SQLException {
		try (Connection conn = DriverManager.getConnection(JDBC_URL);
			 PreparedStatement ps = conn.prepareStatement("DELETE FROM vault_files WHERE id = ?")) {
			ps.setInt(1, fileId);
			ps.executeUpdate();
		}
	}

	public void updateAllFilesEncryption(String oldPin, String newPin) throws SQLException {
		// Fetch all files' data, decrypt with oldPin, encrypt with newPin, and update
		try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
			conn.setAutoCommit(false);
			try (PreparedStatement psSel = conn.prepareStatement("SELECT id, file_data FROM vault_files");
				 PreparedStatement psUpd = conn.prepareStatement("UPDATE vault_files SET file_data = ? WHERE id = ?")) {
				ResultSet rs = psSel.executeQuery();
				while (rs.next()) {
					int id = rs.getInt("id");
					byte[] enc = rs.getBytes("file_data");
					byte[] dec = com.example.calculator_vault_final.service.FileVaultService.decryptData(enc, oldPin);
					byte[] re = com.example.calculator_vault_final.service.FileVaultService.encryptData(dec, newPin);
					psUpd.setBytes(1, re);
					psUpd.setInt(2, id);
					psUpd.addBatch();
				}
				psUpd.executeBatch();
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				throw e;
			} finally {
				conn.setAutoCommit(true);
			}
		}
	}
}
