package com.netapp.snap2cloud.cli;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class CliHelp {
    public static void printUsage(final String applicationName, final Options options, final OutputStream out) {
        
        final PrintWriter writer = new PrintWriter(out);
        final HelpFormatter usageFormatter = new HelpFormatter();
        usageFormatter.printUsage(writer, 80, applicationName, options);
        writer.close();
    }

    public static void printHelp(final Options options, final int printedRowWidth, final String header, final String footer, final int spacesBeforeOption,
            final int spacesBeforeOptionDescription, final boolean displayUsage, final OutputStream out) {
        
        final String commandLineSyntax = "java -cp ApacheCommonsCLI.jar";
        final PrintWriter writer = new PrintWriter(out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, printedRowWidth, commandLineSyntax, header, options, spacesBeforeOption, spacesBeforeOptionDescription, footer,
                displayUsage);
        writer.close();
    }
}
