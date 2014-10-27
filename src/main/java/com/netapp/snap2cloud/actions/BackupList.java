package com.netapp.snap2cloud.actions;

import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.netapp.snap2cloud.model.Hyperscaler;
import com.netapp.snap2cloud.model.S3BackupModel;
import com.netapp.snap2cloud.services.aws.AwsConn;
import com.netapp.snap2cloud.services.aws.S3BackupList;

public class BackupList {
    Hyperscaler hyperscaler;
    
    public BackupList(Hyperscaler hyperscaler) {
        this.hyperscaler = hyperscaler;
    }
    
    public void list() throws Exception {
        try {                
            AwsConn aws = new AwsConn();
            AWSCredentials credentials = aws.getAwsCredentialsFromEnvironment();
            S3BackupList s3BackupList = new S3BackupList(hyperscaler.getS3BucketName(), credentials);        
            List<S3BackupModel> backups = s3BackupList.backupList();
            System.out.println("##### S3 Backup List #####");
            System.out.printf("%-45s%-45s%-23s%n", "### Snapshot ###", "### Path ###", "### Date ###");
            for (S3BackupModel backup : backups) {
                System.out.printf("%-45s%-45s%-23s%n", backup.getBackupName(), backup.getBackupPath(), backup.getDate());
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
}
