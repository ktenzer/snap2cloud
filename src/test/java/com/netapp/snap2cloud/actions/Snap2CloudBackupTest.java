package com.netapp.snap2cloud.actions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.netapp.snap2cloud.os.ExecuteCommand;
import com.netapp.snap2cloud.services.aws.AwsConn;
import com.netapp.snap2cloud.services.aws.S3Backup;
import com.netapp.snap2cloud.services.netapp.cdot.CdotApi;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class Snap2CloudBackupTest {
    private String svm = "svm-keith";
    private String policy = "keith";
    private String host = "0.0.0.0/0";
    private String permission = "read_write";
    private String volume = "test";
    private String snapshot = "test123";
    private String clone = "test_clone";
    
    private String bucketName = "snap2cloud12345";
    private String mountPath = "/tmp/aws";
    private String backupName = "foobar";
    private String accessKey = "";
    private String secretKey = "";
    
    private String cliCmd1Arg1 = "mount";
    private String cliCmd1Arg2 = "10.65.58.160:/test_clone";
    private String cliCmd1Arg3 = mountPath;
    private String cliCmd2Arg1 = "umount";
    private String cliCmd2Arg2 = mountPath;
    
    @Test
    public void testExecute() {
        try {
            NtapCdotTestSuite cdotTestSuite = new NtapCdotTestSuite();
            NtapConnModel connection = cdotTestSuite.getConnectionSpec();
            CdotApi cdotApi = new CdotApi(cdotTestSuite.SVM, connection);
            cdotApi.cloneVolumeFromSnapshot(volume, snapshot, clone);
            cdotApi.exportVolume(svm, clone, policy, host, permission);
            
            List<String> cmd1Args = new ArrayList<String>();
            cmd1Args.add(cliCmd1Arg1);
            cmd1Args.add(cliCmd1Arg2);
            cmd1Args.add(cliCmd1Arg3);

            ExecuteCommand cmd = new ExecuteCommand();
            cmd.executeCmd(cmd1Args);
            
            AwsConn aws = new AwsConn();
            AWSCredentials credentials = aws.getAwsCredentialsFromEnvironment();
            S3Backup s3Backup = new S3Backup(mountPath, bucketName, backupName, false, credentials);           
            s3Backup.backup();
            
            List<String> cmd2Args = new ArrayList<String>();
            cmd2Args.add(cliCmd2Arg1);
            cmd2Args.add(cliCmd2Arg2);
            
            cmd.executeCmd(cmd2Args);
            
            cdotApi.deleteVolume(clone);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
