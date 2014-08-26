package com.netapp.snap2cloud.actions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.netapp.snap2cloud.os.ExecuteCommand;
import com.netapp.snap2cloud.services.netapp.cdot.CdotApi;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class NetAppCloneMountDeleteTest {
    private String svm = "svm-keith";
    private String policy = "keith";
    private String host = "0.0.0.0/0";
    private String permission = "read_write";
    private String volume = "test";
    private String snapshot = "test123";
    private String clone = "test_clone";
    private String cliCmd1Arg1 = "mount";
    private String cliCmd1Arg2 = "10.65.58.160:/test_clone";
    private String cliCmd1Arg3 = "/Users/ktenzer/mnt";
    private String cliCmd2Arg1 = "umount";
    private String cliCmd2Arg2 = "/Users/ktenzer/mnt";
    
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
            
            cmd.sleep(20);
            
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
