package com.netapp.snap2cloud.actions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.netapp.snap2cloud.os.ExecuteCommand;

public class CliCmdTest {
    private String cmdArg1 = "ls";
    private String cmdArg2 = "-l";
    
    @Test
    public void testExecute() {
        try {
            List<String> cmdArgs = new ArrayList<String>();
            cmdArgs.add(cmdArg1);
            cmdArgs.add(cmdArg2);
            ExecuteCommand cmd = new ExecuteCommand();
            cmd.executeCmd(cmdArgs);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
