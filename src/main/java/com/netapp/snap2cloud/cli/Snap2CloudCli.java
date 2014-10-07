package com.netapp.snap2cloud.cli;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.netapp.snap2cloud.actions.Backup;
import com.netapp.snap2cloud.actions.BackupList;
import com.netapp.snap2cloud.actions.Cleanup;
import com.netapp.snap2cloud.model.Host;
import com.netapp.snap2cloud.model.Hyperscaler;
import com.netapp.snap2cloud.model.Storage;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class Snap2CloudCli {
    
    public static void main(String [] args) {
        try {
            usePosixParser(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void usePosixParser(final String[] commandLineArguments) {
        
        final CliOptions cliOptions = new CliOptions();
        final CommandLineParser cmdLinePosixParser = new PosixParser();
        final Options posixOptions = cliOptions.getOptions();
        
        CommandLine commandLine;
        boolean isParameters = true;
        try {
            commandLine = cmdLinePosixParser.parse(posixOptions, commandLineArguments);
            
            if (! commandLine.hasOption("config")) {
                System.out.println("Missing argument: config");
                isParameters = false;
            }
            
            if (! commandLine.hasOption("action")) {
                System.out.println("Missing argument: action");
                isParameters = false;
            }
            
            Storage storage =null;
            Host host = null;
            Hyperscaler hyperscaler= null;
            NtapConnModel ntapConn =null;
            try {
                Config config = new Config();
                Map<String, String> configMap = config.getConfig(commandLine.getOptionValue("config"));
                storage = config.getStorageDetails(configMap);
                host = config.getHostDetails(configMap);
                hyperscaler = config.getHyperscalerDetails(configMap);
                ntapConn = getConnection(storage);
            } catch (Exception e) {
                System.err.println("Could not read configuration file - " + e.getMessage());
            }
            
            if (commandLine.getOptionValue("action").equals("backup")) {
                try {
             
                    Backup backup = new Backup(ntapConn, storage, host, hyperscaler);
                    backup.backupExistingSnapshot();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                
            } else if(commandLine.getOptionValue("action").equals("cleanup")) {
                try {
                    Cleanup cleanup = new Cleanup(ntapConn, storage, host, hyperscaler);
                    cleanup.all();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            } else if(commandLine.getOptionValue("action").equals("list")) {
                try {
                    BackupList backupList = new BackupList(ntapConn, storage, host, hyperscaler);
                    backupList.list();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }                
            } else {
                System.err.println("Invalid action");
            }
        } catch (ParseException parseException) {
            System.err.println("Encountered exception while parsing using PosixParser:\n" + parseException.getMessage());
        }
        
        if (!isParameters) {
            printHelp();
        }
    }

    private static void printHelp() {
        String applicationName = "snap2cloud";
        final CliOptions cliOptions = new CliOptions();
        
        System.out.println(System.lineSeparator());
        CliHelp.printUsage(applicationName, cliOptions.getOptions(), System.out);
    }

    private static NtapConnModel getConnection(Storage storage) {
        
        NtapConnModel connection = new NtapConnModel();
        connection.setHost(storage.getCluster());
        connection.setPort(Integer.valueOf(storage.getPort()));
        connection.setUsername(storage.getUserName());
        connection.setPassword(storage.getPassword());
        connection.setSecure(storage.isSecure());
        
        return connection;
    }
}
