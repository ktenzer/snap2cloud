package com.netapp.snap2cloud.actions;

import org.junit.Assert;
import org.junit.Test;

import com.netapp.snap2cloud.services.netapp.cdot.CdotApi;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class NetAppCloneDeleteTest {
    private String clone = "test_clone";
    
    @Test
    public void testExecute() {
        try {
            //NtapCdotTestSuite cdotTestSuite = new NtapCdotTestSuite();
            //NtapConnModel connection = cdotTestSuite.getConnectionSpec();
            //CdotApi cdotApi = new CdotApi(cdotTestSuite.SVM, connection);
            //cdotApi.deleteVolume(clone);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
