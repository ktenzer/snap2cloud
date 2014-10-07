package com.netapp.snap2cloud.actions;

import org.junit.Test;

import com.netapp.snap2cloud.os.Util;

public class UtilTest {

    @Test
    public void execute() {
        Util util = new Util();
        System.out.println("Date is " + util.getDateFormatFromEpoch(System.currentTimeMillis()));
    }
}
