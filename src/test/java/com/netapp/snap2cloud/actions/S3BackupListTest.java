package com.netapp.snap2cloud.actions;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.netapp.snap2cloud.services.aws.AwsConn;
import com.netapp.snap2cloud.services.aws.S3BackupList;


public class S3BackupListTest {
    private String bucketName = "snap2cloud12345";

    @Test
    public void testExecute() {
        try {
            AwsConn aws = new AwsConn();
            AWSCredentials credentials = aws.getAwsCredentialsFromEnvironment();
            S3BackupList s3BackupList = new S3BackupList(bucketName, credentials);
            s3BackupList.backupList();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
