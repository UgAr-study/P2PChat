package com.example.p2pchat.ui.chat;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MessageItem {

    private final String name;
    private final Calendar date;
    private final String message;

    public MessageItem(String name, String msg, Calendar time) {
        this.name = name;
        this.message = msg;
        this.date = time;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public Calendar getTime() {
        return date;
    }

    //*Return time in format hh:mm
    public String getTimeHoursMinutes() {
        String hours = Integer.toString(date.get(Calendar.HOUR_OF_DAY));
        String minutes = Integer.toString(date.get(Calendar.MINUTE));
        if (minutes.length() == 1) {
            minutes = "0" + minutes;
        }
        return hours + ":" + minutes;
    }

    //*Return time in format dd/mm/yyyy
    public String getTimeDate(String separator) {
        return Integer.toString(date.get(Calendar.DAY_OF_MONTH)) + separator +
                date.get(Calendar.MONTH) + separator
                + date.get(Calendar.YEAR);
    }

//    //*Return date in format "dd MMMMMMMM"
//    public String getDateDayMonth() {
//        DateFormat dateFormat = new SimpleDateFormat("dd MMMMMMMM");
//        String strDate = dateFormat.format(date.getTime());
//        if (strDate.charAt(0) == '0') {
//            return strDate.substring(1);
//        }
//        return dateFormat.format(date.getTime());
//    }

    public int getDay() {
        return date.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        return date.get(Calendar.MONTH);
    }

    public int getYear() {
        return date.get(Calendar.YEAR);
    }

    public int getHours() { return date.get(Calendar.HOUR_OF_DAY);}
}