package com.netapp.snap2cloud.model;

public class Hyperscaler {

    private String s3BucketName;
    private boolean backupTimestamp;
    private int retention;
    
    public String getS3BucketName() {
        return s3BucketName;
    }
    
    public void setS3BucketName(String s3BucketName) {
        this.s3BucketName = s3BucketName;
    }

    public boolean isBackupTimestamp() {
        return backupTimestamp;
    }

    public void setBackupTimestamp(boolean backupTimestamp) {
        this.backupTimestamp = backupTimestamp;
    }

    public int getRetention() {
        return retention;
    }

    public void setRetention(int retention) {
        this.retention = retention;
    }
}
