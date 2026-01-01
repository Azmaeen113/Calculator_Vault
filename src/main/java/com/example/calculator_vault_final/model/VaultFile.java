package com.example.calculator_vault_final.model;

import java.time.LocalDateTime;

public class VaultFile {
	private Integer id;
	private String fileName;
	private String originalExtension;
	private byte[] fileData;
	private long fileSize;
	private LocalDateTime uploadedAt;

	public VaultFile() {}

	public VaultFile(Integer id, String fileName, String originalExtension, byte[] fileData, long fileSize, LocalDateTime uploadedAt) {
		this.id = id;
		this.fileName = fileName;
		this.originalExtension = originalExtension;
		this.fileData = fileData;
		this.fileSize = fileSize;
		this.uploadedAt = uploadedAt;
	}

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }

	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName; }

	public String getOriginalExtension() { return originalExtension; }
	public void setOriginalExtension(String originalExtension) { this.originalExtension = originalExtension; }

	public byte[] getFileData() { return fileData; }
	public void setFileData(byte[] fileData) { this.fileData = fileData; }

	public long getFileSize() { return fileSize; }
	public void setFileSize(long fileSize) { this.fileSize = fileSize; }

	public LocalDateTime getUploadedAt() { return uploadedAt; }
	public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
