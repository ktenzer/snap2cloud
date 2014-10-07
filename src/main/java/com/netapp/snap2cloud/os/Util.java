package com.netapp.snap2cloud.os;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Util {

    public String getDateFormatFromEpoch(Long epoch) {
        Date date = new Date(epoch);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = sdf.format(date);

        return formattedDate;
    }
}
