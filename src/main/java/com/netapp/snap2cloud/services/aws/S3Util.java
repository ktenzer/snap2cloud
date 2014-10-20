package com.netapp.snap2cloud.services.aws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class S3Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Util.class);
    private final TransferManager tm;
    private final AmazonS3Client s3;

    public S3Util(AWSCredentials credentials) {
        AwsConn aws = new AwsConn();
        tm = aws.getS3TransferManager(credentials);
        s3 = aws.getS3Client(credentials);
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
    
    public Map<String, String> getMetadataForS3Object(String bucketName, String s3Path) {

        Map<String, String> metadataMap = new HashMap<String, String>();
        try {
            S3Object metadataObject = s3.getObject(new GetObjectRequest(bucketName, s3Path));
            metadataMap = metadataObject.getObjectMetadata().getUserMetadata();

            try {
                metadataObject.close();
            } catch (IOException e) {
                throw new AmazonClientException(e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new AmazonClientException(e.getMessage(), e);
        }

        return metadataMap;
    }
    
    public String getDateFormatFromEpoch(Long epoch) {
        Date date = new Date(epoch);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(date);

        return formattedDate;
    }
    
    public boolean existsS3Object(String bucket, String key) {
        ObjectListing list = s3.listObjects(bucket, key);
        return list.getObjectSummaries().size() > 0;
    }
    
    public void deleteS3Backup(String backupName, String bucketName) throws AmazonClientException {
        String backupMetadataFileName = "snapshots/" + backupName + ".snapshot";

        ObjectListing objectList = null;
        do {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName).withPrefix(backupName);

            objectList = s3.listObjects(listObjectsRequest);
            ;

            if (this.existsS3Object(bucketName, backupMetadataFileName)) {
                s3.deleteObject(bucketName, backupMetadataFileName);
            }

            if (objectList.getObjectSummaries().isEmpty()) {
                LOGGER.warn(String.format("S3 backup " + backupName + " for bucket " + bucketName + " does not exist"));
            }

            for (S3ObjectSummary summary : objectList.getObjectSummaries()) {
                try {
                    LOGGER.debug(String.format("Deleting object " + summary.getKey()));
                    s3.deleteObject(bucketName, summary.getKey());
                } catch (AmazonClientException e) {
                    LOGGER.error(String.format("Delete of object " + summary.getKey() + " failed"));
                    LOGGER.error(String.format("S3 backup delete of " + backupName + " failed"));
                    throw new AmazonClientException(e.getMessage(), e);
                }
                LOGGER.debug(String.format("S3 object delete for " + summary.getKey() + " completed successfully"));
            }
            listObjectsRequest.setMarker(objectList.getNextMarker());
        } while (objectList.isTruncated());

        LOGGER.info(String.format("S3 backup delete for " + backupName + " completed successfully"));
    }
}
