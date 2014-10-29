package com.netapp.snap2cloud.cli;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netapp.snap2cloud.actions.Backup;
import com.netapp.snap2cloud.actions.BackupList;
import com.netapp.snap2cloud.actions.Cleanup;
import com.netapp.snap2cloud.actions.DeleteBackup;
import com.netapp.snap2cloud.model.Host;
import com.netapp.snap2cloud.model.Hyperscaler;
import com.netapp.snap2cloud.model.Storage;
import com.netapp.snap2cloud.services.netapp.cdot.NtapConnModel;

public class Snap2CloudCli {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Snap2CloudCli.class);
    
    public static void main(String [] args) {
        try {
            usePosixParser(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void usePosixParser(final String[] commandLineArguments) throws Exception {
        
        final CliOptions cliOptions = new CliOptions();
        final CommandLineParser cmdLinePosixParser = new PosixParser();
        final Options posixOptions = cliOptions.getOptions();
        
        CommandLine commandLine;
        try {
            commandLine = cmdLinePosixParser.parse(posixOptions, commandLineArguments);
            
            if (! commandLine.hasOption("config")) {
                LOGGER.error("Missing argument: config");
                printHelp();
            }
            
            if (! commandLine.hasOption("action")) {
                LOGGER.error("Missing argument: action");
                printHelp();
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
                throw new Exception("ERROR: Could not read configuration file - " + e.getMessage(), e);
            }
            
            if (commandLine.getOptionValue("action").equals("backup")) {
                try {           
                    Backup backup = new Backup(ntapConn, storage, host, hyperscaler);
                    backup.backupExistingSnapshot();
                } catch (Exception e) {
                    throw new Exception(e.getMessage(), e);
                }
                
            } else if(commandLine.getOptionValue("action").equals("cleanup")) {
                try {
                    Cleanup cleanup = new Cleanup(ntapConn, storage, host, hyperscaler);
                    cleanup.all();
                } catch (Exception e) {
                    throw new Exception(e.getMessage(), e);
                }
            } else if(commandLine.getOptionValue("action").equals("list")) {
                try {
                    BackupList backupList = new BackupList(hyperscaler);
                    backupList.list();
                } catch (Exception e) {
                    throw new Exception(e.getMessage(), e);
                }       
            } else if(commandLine.getOptionValue("action").equals("deleteBackup")) {
                try {
                    DeleteBackup deleteBackup = new DeleteBackup(storage, hyperscaler);
                    deleteBackup.deleteBackup();
                } catch (Exception e) {
                    throw new Exception(e.getMessage(), e);
                }
            } else if(commandLine.getOptionValue("action").equals("deleteOnRetention")) {
                try {
                    DeleteBackup deleteBackup = new DeleteBackup(storage, hyperscaler);
                    deleteBackup.deleteOnRetention();
                } catch (Exception e) {
                    throw new Exception(e.getMessage(), e);
                }                   
            } else {
                throw new Exception("Invalid argument");
            }
        } catch (ParseException parseException) {
            throw new Exception("ERROR: Encountered exception while parsing using PosixParser:\n" + parseException.getMessage(), parseException);
        }
        
        LOGGER.info("Snap2cloud completed successfully");
        System.exit(0);
    }

    private static void printHelp() {
        final CliOptions cliOptions = new CliOptions();

        CliHelp.printUsage(cliOptions.getOptions());
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
