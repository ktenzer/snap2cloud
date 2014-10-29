package com.netapp.snap2cloud.cli;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class CliHelp {
    
    public static void printUsage(final Options options) {        
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -jar snap2cloud.jar -action <action> -config <config>", options );
        System.exit(0);
    }
}
