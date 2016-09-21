package com.cloudkibo.library;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by sojharo on 20/09/2016.
 */
public class Utility {

    public static String convertDateToLocalTimeZoneAndReadable(String dStr) throws ParseException {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        Date date = format.parse(dStr);

        TimeZone tz = TimeZone.getDefault();
        String gmt1= TimeZone.getTimeZone(tz.getID()).getDisplayName(false, TimeZone.SHORT);
        String gmt2= TimeZone.getTimeZone(tz.getID()).getDisplayName(false, TimeZone.LONG);
        Log.d("Tag","TimeZone : "+gmt1+"\t"+gmt2);

        boolean additionRequired = (gmt1.contains("+"));
        String hourOffset = gmt1.substring(4, 6);
        String minuteOffset = gmt1.substring(7, 9);

        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(date); // sets calendar time/date
        if(additionRequired) {
            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hourOffset)); // adds hours
            cal.add(Calendar.MINUTE, Integer.parseInt(minuteOffset)); // adds minutes
        } else {
            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hourOffset) * (-1)); // adds hours
            cal.add(Calendar.MINUTE, Integer.parseInt(minuteOffset) * (-1)); // adds minutes
        }
        Date date2 = cal.getTime();

        cal.setTime(date2);

        return  String.format("%02d", cal.get(Calendar.DATE)) +"-"+
                String.format("%02d", (cal.get(Calendar.MONTH)+1)) +"-"+
                String.format("%02d", cal.get(Calendar.YEAR)) +" "+
                String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) +":"+
                String.format("%02d", cal.get(Calendar.MINUTE));
    }

    public static String getCurrentTimeInISO(){
        TimeZone tZ = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tZ);
        return df.format(new Date());
    }

}
