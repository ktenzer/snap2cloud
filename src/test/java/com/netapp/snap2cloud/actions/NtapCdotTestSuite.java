package com.netapp.snap2cloud.actions;

import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class NtapCdotTestSuite {
    public String SVM = "svm-keith";
    private String HOSTNAME = "steve.muccbc.hq.netapp.com";
    private Integer PORT = 80;
    private String USERNAME = "admin";
    private String PASSWORD = "netapp01";
    private boolean secure = false;
    
    public NtapCdotTestSuite() {
        
    }
    
    public NtapConnModel getConnectionSpec() {
        NtapConnModel connection = new NtapConnModel();
        connection.setHost(this.HOSTNAME);
        connection.setPort(this.PORT);
        connection.setUsername(this.USERNAME);
        connection.setPassword(this.PASSWORD);
        connection.setSecure(secure);
        
        return connection;
    }
}
