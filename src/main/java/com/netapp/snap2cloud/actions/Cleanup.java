package com.netapp.snap2cloud.actions;

import java.util.Arrays;
import java.util.List;

import com.netapp.snap2cloud.model.Host;
import com.netapp.snap2cloud.model.Hyperscaler;
import com.netapp.snap2cloud.model.Storage;
import com.netapp.snap2cloud.os.ExecuteCommand;
import com.netapp.snap2cloud.services.netapp.cdot.CdotApi;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class Cleanup {

    NtapConnModel connection;
    Storage storage;
    Host host;
    Hyperscaler hyperscaler;
    CdotApi cdotApi;
    
    public Cleanup(NtapConnModel connection, Storage storage, Host host, Hyperscaler hyperscaler) {
        this.connection = connection;
        this.storage = storage;
        this.host = host;
        this.hyperscaler = hyperscaler;
        this.cdotApi = new CdotApi(storage.getSvm(), connection);
    }
    
    public void all() throws Exception {
        try {
            NtapConnModel connection = new NtapConnModel();
            connection.setHost(storage.getCluster());
            connection.setPort(Integer.valueOf(storage.getPort()));
            connection.setSecure(Boolean.valueOf(storage.isSecure()));
            connection.setUsername(storage.getUserName());
            connection.setPassword(storage.getPassword());

            CdotApi cdotApi = new CdotApi(storage.getSvm(), connection);
            
            
            try {
                ExecuteCommand cmd = new ExecuteCommand();
                String[] umountCmdArray = host.getUmountCmd().split(" ");
                List<String> cmd2Args = Arrays.asList(umountCmdArray);
                cmd.executeCmd(cmd2Args);
            } catch (Exception e) {
                System.err.println("Couldn't unmount the file system - " + e.getMessage());
            }
            
            cdotApi.deleteVolume(storage.getVolumeCloneName());
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }
}
