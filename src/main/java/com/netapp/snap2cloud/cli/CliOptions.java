package com.netapp.snap2cloud.cli;

import org.apache.commons.cli.Options;

public class CliOptions {

    public Options getOptions() {
        // create the Options
        Options options = new Options();

        options.addOption("config", true, "Path to configuration file");
        options.addOption("action", true, "Provide an action: backup, list, deleteBackup, deleteOnRetention, or cleanup");
        
        return options;
    }
}
