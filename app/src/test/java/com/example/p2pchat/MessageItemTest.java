package com.example.p2pchat;

import com.example.p2pchat.ui.chat.MessageItem;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class MessageItemTest {
    @Test
    public void Test_MessageItemTest() {
        Calendar calendar = new GregorianCalendar();
        MessageItem messageItem = new MessageItem("Artem", "I am LOH!", calendar);
        assertEquals(messageItem.getName(), "Artem");
        assertEquals(messageItem.getMessage(), "I am LOH!");
        assertEquals(messageItem.getTime(), calendar);
    }

    @Test
    public void Test_CalendarTest() {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        long timeInMilli = calendar.getTimeInMillis();
        Calendar calendar1 = new GregorianCalendar(TimeZone.getDefault());
        calendar1.setTimeInMillis(timeInMilli);
        assertEquals(calendar, calendar1);
    }


}
