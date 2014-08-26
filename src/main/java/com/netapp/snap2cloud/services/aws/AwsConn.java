package com.netapp.snap2cloud.services.aws;

import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;

public class AwsConn {

    public AwsConn() {
        
    }

    public TransferManager getS3TransferManager(AWSCredentials credentials) throws AmazonClientException {
        TransferManager tm = null;

        try {
            tm = new TransferManager(credentials);
        } catch (Exception e) {
            throw new AmazonClientException(e.getMessage(), e);
        }

        return tm;
    }

    public AmazonS3Client getS3Client(AWSCredentials credentials) throws AmazonClientException {
        AmazonS3Client s3 = null;

        try {
            s3 = new AmazonS3Client(credentials);
        } catch (Exception e) {
            throw new AmazonClientException(e.getMessage(), e);
        }

        return s3;
    }
    
    public AWSCredentials getAwsCredentials(String accessKey, String secretKey) throws IOException {
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}
