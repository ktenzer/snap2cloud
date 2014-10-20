package com.netapp.snap2cloud.services.aws;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.netapp.snap2cloud.model.S3BackupModel;

public class S3BackupList {
    private String bucketName;
    AWSCredentials credentials;
    Long timestamp;

    public S3BackupList(String bucketName, AWSCredentials credentials) {
        this.bucketName = bucketName;
        this.credentials = credentials;
        timestamp = System.currentTimeMillis();
    }

    public List<S3BackupModel> backupList() throws AmazonClientException {
        AwsConn aws = new AwsConn();
        AmazonS3Client s3 = aws.getS3Client(credentials);
        S3Util s3Util = new S3Util(credentials);

        List<S3BackupModel> backupModelList = new ArrayList<S3BackupModel>();
        ObjectListing objectList = null;
        do {
            String prefix = "snapshots/";
            String delimiter = "/";
            if (!prefix.endsWith(delimiter)) {
                prefix += delimiter;
            }

            ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(prefix).withDelimiter(delimiter);

            objectList = s3.listObjects(listObjectsRequest);

            for (S3ObjectSummary summary : objectList.getObjectSummaries()) {
                if (! summary.getKey().endsWith("snapshot")) {
                    continue;
                }
                
                S3BackupModel backupModel = new S3BackupModel();
                Map<String, String> metadataMap = s3Util.getMetadataForS3Object(bucketName, summary.getKey());

                backupModel.setBackupName(metadataMap.get("backup_name"));
                backupModel.setTimestamp(metadataMap.get("timestamp"));
                backupModel.setDate(metadataMap.get("date"));
                backupModel.setBackupPath(metadataMap.get("backup_path"));

                backupModelList.add(backupModel);

            }
            listObjectsRequest.setMarker(objectList.getNextMarker());
        } while (objectList.isTruncated());

        return backupModelList;
    }

}
