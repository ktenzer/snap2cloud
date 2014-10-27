package com.netapp.snap2cloud.actions;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.netapp.snap2cloud.model.Host;
import com.netapp.snap2cloud.model.Hyperscaler;
import com.netapp.snap2cloud.model.Storage;
import com.netapp.snap2cloud.os.ExecuteCommand;
import com.netapp.snap2cloud.services.aws.AwsConn;
import com.netapp.snap2cloud.services.aws.S3Backup;
import com.netapp.snap2cloud.services.netapp.cdot.CdotApi;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class Backup {

    NtapConnModel connection;
    Storage storage;
    Host host;
    Hyperscaler hyperscaler;
    CdotApi cdotApi;
    
    public Backup(NtapConnModel connection, Storage storage, Host host, Hyperscaler hyperscaler) {
        this.connection = connection;
        this.storage = storage;
        this.host = host;
        this.hyperscaler = hyperscaler;
        this.cdotApi = new CdotApi(storage.getSvm(), connection);
    }
    
    public void backupExistingSnapshot() throws Exception {
        try {         
            CdotApi cdotApi = new CdotApi(storage.getSvm(), connection);
            
            //use latest snapshot name if set
            if (storage.isLatestSnapshot()) {
                storage.setSnapshotName(cdotApi.getLatestSnapshot(storage.getVolumeName()).getName());
            }
            
            cdotApi.cloneVolumeFromSnapshot(storage.getVolumeName(), storage.getSnapshotName(), storage.getVolumeCloneName());
            cdotApi.exportVolume(storage.getSvm(), storage.getVolumeCloneName(), host.getExportPolicyName(), host.getExportHostorNetwork(), host.getExportPermission());
            
            String[] mountCmdArray = host.getMountCmd().split(" ");
            List<String> cmd1Args = Arrays.asList(mountCmdArray);

            ExecuteCommand cmd = new ExecuteCommand();
            cmd.executeCmd(cmd1Args);
            
            AwsConn aws = new AwsConn();
            AWSCredentials credentials = aws.getAwsCredentialsFromEnvironment();
            S3Backup s3Backup = new S3Backup(host.getMountPath(), hyperscaler.getS3BucketName(), storage.getSnapshotName(), hyperscaler.isBackupTimestamp(), credentials);           
            s3Backup.backup();
            
            String[] umountCmdArray = host.getUmountCmd().split(" ");
            List<String> cmd2Args = Arrays.asList(umountCmdArray);
            
            cmd.executeCmd(cmd2Args);
            
            cdotApi.deleteVolume(storage.getVolumeCloneName());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
}
