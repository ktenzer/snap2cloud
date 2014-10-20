package com.netapp.snap2cloud.services.aws;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.netapp.snap2cloud.model.S3BackupModel;

public class S3DeleteBackup {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3DeleteBackup.class);

    private String bucketName;
    AWSCredentials credentials;
    Long timestamp;

    public S3DeleteBackup(String bucketName, AWSCredentials credentials) {
        this.bucketName = bucketName;
        this.credentials = credentials;
        timestamp = System.currentTimeMillis();
    }

    public void delete(String backupName) throws AmazonClientException {
        S3Util s3Util = new S3Util(credentials);

        try {
            s3Util.deleteS3Backup(backupName, bucketName);
        } catch (Exception e) {
            throw new AmazonClientException(e.getMessage());
        }
    }
    
    public void deleteOnRetention(int retention) throws AmazonClientException {
        S3Util s3Util = new S3Util(credentials);
        S3BackupList s3BackupList = new S3BackupList(bucketName, credentials);
        List<S3BackupModel> backupList = s3BackupList.backupList();
        HashMap<String, S3BackupModel> backupMap = new HashMap<String, S3BackupModel>();
        for (S3BackupModel backup : backupList) {
            backupMap.put(backup.getTimestamp(), backup);
        }

        Map<String, S3BackupModel> reverseSortedBackupMap = new TreeMap<String, S3BackupModel>(Collections.reverseOrder());
        reverseSortedBackupMap.putAll(backupMap);
        
        if (retention >= backupList.size()) {
            LOGGER.info(String.format("S3 backup older than retention " + retention + " not found, skipping delete"));
        } else {

            int count = 1;

            for (Entry<String, S3BackupModel> reverseSortedEntrySet : reverseSortedBackupMap.entrySet()) {
                if (count > retention) {
                    final String backupName = reverseSortedEntrySet.getValue().getBackupName();
                    LOGGER.info(String.format("S3 backup delete of " + backupName + " using retention " + retention));

                    try {
                        s3Util.deleteS3Backup(backupName, bucketName);
                    } catch (AmazonClientException e) {
                        throw new AmazonClientException(e.getMessage(), e);
                    }
                }
                count++;
            }
        }
    }
}
