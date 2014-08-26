package com.netapp.snap2cloud.services.aws;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class S3Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Util.class);
    private final TransferManager tm;

    public S3Util(AWSCredentials credentials) {
        AwsConn aws = new AwsConn();
        tm = aws.getS3TransferManager(credentials);
    }

    public ObjectMetadata getMetadata(String backupName, Long timestamp, String mountPath)
            throws AmazonClientException {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("BACKUP_NAME", backupName);
        metadata.addUserMetadata("TIMESTAMP", String.valueOf(timestamp));
        metadata.addUserMetadata("DATE", this.getDateFormatFromEpoch(timestamp).toString());
        metadata.addUserMetadata("BACKUP_PATH", mountPath.toString());

        return metadata;
    }

    public void uploadBackupMetadata(ObjectMetadata metadata, String bucketName, String uploadPath) throws AmazonClientException {
        File file = new File(uploadPath + ".snapshot");
        String fileUploadPath = "snapshots/" + uploadPath + ".snapshot";
        try {
            file.createNewFile();
            this.uploadFile(file, metadata, bucketName, fileUploadPath);
            file.delete();
        } catch (Exception e) {
            throw new AmazonClientException(e.getMessage(), e);
        }
    }
    
    public void uploadFile(File file, ObjectMetadata metadata, String bucketName, String fileUploadPath) throws AmazonClientException {
        LOGGER.info("Uploading file " + file.getAbsolutePath() + " to Amazon S3 bucket " + bucketName);

        try {
            metadata.setContentLength(file.length());
            Upload upload = tm.upload(bucketName, fileUploadPath, new FileInputStream(file), metadata);
            upload.waitForCompletion();
        } catch (Exception e) {
            LOGGER.info("File upload for " + file.getAbsolutePath() + " to Amazon S3 bucket " + bucketName + " failed");
            throw new AmazonClientException(e.getMessage(), e);
        }

        LOGGER.info("File upload for " + file.getAbsolutePath() + " to Amazon S3 bucket " + bucketName + " completed successfully");
    }
    
    public String getDateFormatFromEpoch(Long epoch) {
        Date date = new Date(epoch * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(date);

        return formattedDate;
    }
}
