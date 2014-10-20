package com.netapp.snap2cloud.actions;

import com.amazonaws.auth.AWSCredentials;
import com.netapp.snap2cloud.model.Hyperscaler;
import com.netapp.snap2cloud.model.Storage;
import com.netapp.snap2cloud.services.aws.AwsConn;
import com.netapp.snap2cloud.services.aws.S3DeleteBackup;

public class DeleteBackup {
    
    Storage storage;
    Hyperscaler hyperscaler;
    
    public DeleteBackup(Storage storage, Hyperscaler hyperscaler) {
        this.hyperscaler = hyperscaler;
        this.storage = storage;
    }
    
    public void deleteBackup() throws Exception {
        try {                   
            AwsConn aws = new AwsConn();
            AWSCredentials credentials = aws.getAwsCredentialsFromEnvironment();
            S3DeleteBackup s3Delete = new S3DeleteBackup(hyperscaler.getS3BucketName(), credentials);        
            s3Delete.delete(storage.getSnapshotName());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
    
    public void deleteOnRetention() throws Exception {
        try {                   
            AwsConn aws = new AwsConn();
            AWSCredentials credentials = aws.getAwsCredentialsFromEnvironment();
            S3DeleteBackup s3Delete = new S3DeleteBackup(hyperscaler.getS3BucketName(), credentials);        
            s3Delete.deleteOnRetention(hyperscaler.getRetention());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
}
