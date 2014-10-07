package com.netapp.snap2cloud.actions;

import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.netapp.snap2cloud.model.Host;
import com.netapp.snap2cloud.model.Hyperscaler;
import com.netapp.snap2cloud.model.S3BackupModel;
import com.netapp.snap2cloud.model.Storage;
import com.netapp.snap2cloud.services.aws.AwsConn;
import com.netapp.snap2cloud.services.aws.S3BackupList;
import com.netapp.snap2cloud.services.netapp.cdot.CdotApi;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class BackupList {

    NtapConnModel connection;
    Storage storage;
    Host host;
    Hyperscaler hyperscaler;
    CdotApi cdotApi;
    
    public BackupList(NtapConnModel connection, Storage storage, Host host, Hyperscaler hyperscaler) {
        this.connection = connection;
        this.storage = storage;
        this.host = host;
        this.hyperscaler = hyperscaler;
        this.cdotApi = new CdotApi(storage.getSvm(), connection);
    }
    
    public void list() throws Exception {
        try {                
            AwsConn aws = new AwsConn();
            AWSCredentials credentials = aws.getAwsCredentialsFromEnvironment();
            S3BackupList s3BackupList = new S3BackupList(hyperscaler.getS3BucketName(), credentials);        
            List<S3BackupModel> backups = s3BackupList.backupList();
            System.out.println("S3 Backup List");
            for (S3BackupModel backup : backups) {
                System.out.println(backup.getBackupName() + " " + backup.getBackupPath() + " " + backup.getDate());
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
}
