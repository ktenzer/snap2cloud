package com.netapp.snap2cloud.services.netapp.cdot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netapp.nmsdk.client.ApiRunner;
import com.netapp.nmsdk.client.ApiTarget;

public class NtapConn {
    private static final Logger LOGGER = LoggerFactory.getLogger(NtapConn.class);
    public static final ApiRunner getRunner(String svm, NtapConnModel connection) {
        LOGGER.info("Creating NetApp connection to controller " + connection.getHost() + " using SVM " + svm + " " + connection.getPort() + " " + connection.getUsername() + " " + connection.getPassword() + " " + connection.isSecure());
        ApiTarget.Builder builder = ApiTarget.builder().withHost(connection.getHost()).withUserName(connection.getUsername())
                .withPassword(connection.getPassword()).withTargetType(ApiTarget.TargetType.FILER).withPort(connection.getPort());

        if (connection.isSecure()) {
            builder = builder.useHttps();
        } else {
            builder = builder.useHttp();
        }

        ApiRunner runner = new ApiRunner(builder.build());

        if (svm != null) {
            runner = runner.withVServer(svm);
        }

        return runner;
    }
    
    public NtapConnModel getConnectionDetails(String host, String port, String username, String password, String secure) throws Exception {
        
        NtapConnModel connection = new NtapConnModel();
        
        connection.setHost(host);
        connection.setPort(Integer.valueOf(port));
        connection.setUsername(username);
        connection.setPassword(password);
        connection.setSecure(Boolean.valueOf(secure));

        return connection;
    }
}
