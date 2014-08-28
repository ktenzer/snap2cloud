package com.netapp.snap2cloud.actions;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.netapp.snap2cloud.services.aws.AwsConn;
import com.netapp.snap2cloud.services.aws.S3Backup;


public class S3BackupTest {
    private String bucketName = "snap2cloud12345";
    private String mountPath = "/tmp/aws";
    private String backupName = "foobar";
    private String accessKey = "";
    private String secretKey = "";
    @Test
    public void testExecute() {
        try {
            AwsConn aws = new AwsConn();
            AWSCredentials credentials = aws.getAwsCredentials(accessKey, secretKey);
            S3Backup s3Backup = new S3Backup(mountPath, bucketName, backupName, credentials);
            
            s3Backup.backup();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
