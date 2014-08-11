package com.netapp.snap2cloud.services.aws;

import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;

public class AwsConn {
    private String accessKey;
    private String secretKey;

    public AwsConn(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public TransferManager getS3TransferManager() throws AmazonClientException {
        TransferManager tm = null;

        try {
            AWSCredentials credentials = getAwsCredentials();
            tm = new TransferManager(credentials);
        } catch (IOException e) {
            throw new AmazonClientException(e.getMessage(), e);
        }

        return tm;
    }

    public AmazonS3Client getS3Client() throws AmazonClientException {
        AmazonS3Client s3 = null;

        try {
            AWSCredentials credentials = getAwsCredentials();
            s3 = new AmazonS3Client(credentials);
        } catch (IOException e) {
            throw new AmazonClientException(e.getMessage(), e);
        }

        return s3;
    }
    
    private AWSCredentials getAwsCredentials() throws IOException {
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}
