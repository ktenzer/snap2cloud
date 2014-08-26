package com.netapp.snap2cloud.services.aws;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.ObjectMetadataProvider;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

public class S3Backup {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Backup.class);

    private String mountPath;
    private String bucketName;
    private String backupName;
    AWSCredentials credentials;
    Long timestamp;

    public S3Backup(String mountPath, String bucketName, String backupName, AWSCredentials credentials) {
        this.mountPath = mountPath;
        this.bucketName = bucketName;
        this.backupName = backupName;
        this.credentials = credentials;
        timestamp = System.currentTimeMillis();
    }

    public void backup() throws AmazonClientException {
        AwsConn aws = new AwsConn();
        TransferManager tm = aws.getS3TransferManager(credentials);
        S3Util s3Util = new S3Util(credentials);
        ObjectMetadata metadata = s3Util.getMetadata(backupName, timestamp, mountPath);

        try {
            createBucket(tm, bucketName);
        } catch (Exception e) {
            throw new AmazonClientException(e.getMessage());
        }

        File file = new File(mountPath);
        String backupPath = backupName + file.getAbsolutePath();

        LOGGER.info("Uploading directory " + file.getAbsolutePath() +" to Amazon S3 bucket " + bucketName +" in " + mountPath);
        try {
            ObjectMetadataProvider metadataProvider = new ObjectMetadataProvider() {
                public void provideObjectMetadata(File file, ObjectMetadata metadata) {
                    metadata.addUserMetadata("TIMESTAMP", String.valueOf(timestamp));
                    metadata.addUserMetadata("MOUNT_PATH", mountPath.toString());
                    metadata.addUserMetadata("BACKUP_NAME", backupName);
                }
            };

            MultipleFileUpload uploadDirectory = tm.uploadDirectory(bucketName, backupPath, file, true, metadataProvider);

            while (uploadDirectory.isDone() == false) {
                Double progress = uploadDirectory.getProgress().getPercentTransferred();
                String percent = progress.intValue() + "%";
                long xferProgress = uploadDirectory.getProgress().getBytesTransferred();
                String xferState = uploadDirectory.getState().toString();

                LOGGER.info(String.format("Upload status for " + file.getAbsolutePath() + " in S3 Bucket " + bucketName + " state: " + xferState + "percent: " + percent + "progress: " + xferProgress)); 
            }

            this.verifyMultiPartUpload(uploadDirectory);
            
            try {
                s3Util.uploadBackupMetadata(metadata, bucketName, backupName);
            } catch (Exception e) {
                throw new AmazonClientException(e.getMessage());
            }

            LOGGER.info("Directory upload for " + file.getAbsolutePath() + " to Amazon S3 bucket " + bucketName + " completed successfully");
        } catch (Exception e) {
            LOGGER.error("Directory upload for " + file.getAbsolutePath() + " to Amazon S3 bucket " + bucketName + " failed");
            throw new AmazonClientException(e.getMessage());
        }
    }

    private static void createBucket(TransferManager tm, String bucketName) throws AmazonClientException {

        boolean existsBucket = false;
        for (Bucket bucket : tm.getAmazonS3Client().listBuckets()) {
            if (bucket.getName().equals(bucketName)) {
                existsBucket = true;
            }
        }

        if (!existsBucket) {
            LOGGER.info("Creating Amazon S3 bucket " + bucketName);
            try {
                tm.getAmazonS3Client().createBucket(bucketName);
            } catch (AmazonClientException e) {
                LOGGER.error("Amazon S3 bucket creation for bucket " + bucketName + " failed");
                throw new AmazonClientException(e.getMessage());
            }

            LOGGER.info("Amazon S3 bucket creation for bucket " + bucketName + " completed successfully");
        }
    }

    private Map<String, String> verifyMultiPartUpload(MultipleFileUpload uploadDirectory) throws AmazonClientException {
            Collection<? extends Upload> uploadResults = uploadDirectory.getSubTransfers();
            Iterator<? extends Upload> iterator = uploadResults.iterator();

            Map<String, String> fileModifyMap = new HashMap<String, String>();
            while (iterator.hasNext()) {
                UploadResult uploadResult = null;

                try {
                    uploadResult = iterator.next().waitForUploadResult();
                } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                        throw new AmazonClientException(e.getMessage());
                }

                if (uploadResult != null) {
                    LOGGER.debug(String.format("Multipart upload success for file " + uploadResult.getKey() + " to Amazon S3 bucket " + uploadResult.getBucketName()));
                }
            }
            
            return fileModifyMap;
        }
}
