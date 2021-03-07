package com.example.p2pchat;

import com.example.p2pchat.ui.chat.MessageItem;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
}
