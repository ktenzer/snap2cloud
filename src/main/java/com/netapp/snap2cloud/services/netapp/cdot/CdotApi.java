package com.netapp.snap2cloud.services.netapp.cdot;

import java.util.List;

import com.netapp.nmsdk.client.ApiRunner;
import com.netapp.nmsdk.ontap.api.snapshot.SnapshotGetIterRequest;
import com.netapp.nmsdk.ontap.api.snapshot.SnapshotInfo;

public class CdotApi {
    
    private String svm;
    private NtapConnModel connection;
    private ApiRunner runner;
    
    public CdotApi (String svm, NtapConnModel connection) {
        this.svm = svm;
        this.connection = connection;
        
        this.runner = NtapConn.getRunner(svm, connection);
    }

    public void getSnapshots(String volume) throws Exception {
        try {
            SnapshotInfo snapshotInfo = new SnapshotInfo();
            snapshotInfo.setVserver(svm);

            SnapshotGetIterRequest snapshotGetIterRequest = new SnapshotGetIterRequest();
            snapshotGetIterRequest.withQuery(snapshotInfo);

            List<SnapshotInfo> snapshotList = runner.run(snapshotGetIterRequest).getAttributesList();
            
            for (SnapshotInfo snapshot : snapshotList) {
                
            }
       
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
}
