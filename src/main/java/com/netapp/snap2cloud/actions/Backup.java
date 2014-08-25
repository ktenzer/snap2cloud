package com.netapp.snap2cloud.actions;

import com.netapp.snap2cloud.services.netapp.cdot.CdotApi;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class Backup {

    private String smv;
    NtapConnModel connection;
    CdotApi cdotApi;
    public Backup(String svm, NtapConnModel connection) {
        this.smv = svm;
        this.connection = connection;
        this.cdotApi = new CdotApi(svm, connection);
    }
    
    public void backupToCloud() throws Exception {
        cdotApi.cloneVolumeFromSnapshot("test", "test", "test_clone");
    }
}
