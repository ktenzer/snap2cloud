package com.netapp.snap2cloud.model;

public class S3BackupModel {
    private String backupName;
    private String timestamp;
    private String date;
    private String backupPath;
    
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

	public String getBackupName() {
		return backupName;
	}

	public void setBackupName(String backupName) {
		this.backupName = backupName;
	}
}
