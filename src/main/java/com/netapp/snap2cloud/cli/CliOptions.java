package com.netapp.snap2cloud.cli;

import org.apache.commons.cli.Options;

public class CliOptions {

    public Options getOptions() {
        // create the Options
        Options options = new Options();

        options.addOption("config", true, "Configuration file");
        options.addOption("action", true, "Provide an action");
        options.addOption("mountPath", true, "Mount path");
        options.addOption("snapshotName", true, "NetApp snapshot name");
        options.addOption("volumeName", true, "NetApp volume name");
        options.addOption("bucketName", true, "S3 bucket name");
        options.addOption("cluster", true, "NetApp storage cluster");
        options.addOption("svm", true, "NetApp storage virtual machine");
        options.addOption("username", true, "NetApp ONTAPI username");
        options.addOption("password", true, "NetApp ONTAPI password");
        options.addOption("useSsl", false, "SSL communications to NetApp storage");
        options.addOption("port", true, "NetApp HTTP or HTTPS port");
        options.addOption("mountCmd", true, "Mount command");
        options.addOption("umountCmd", true, "Umount command");
        options.addOption("exportHost", true, "Hostname or ip for export");
        options.addOption("exportPolicy", true, "Export policy");
        options.addOption("exportPermission", true, "Export permission: root, read-only, or read-write");
        
        return options;
    }
}
