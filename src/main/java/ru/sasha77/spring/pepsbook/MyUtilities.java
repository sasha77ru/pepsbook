package ru.sasha77.spring.pepsbook;

import java.util.Date;
import java.text.SimpleDateFormat;

public class MyUtilities {
    public static String myDate (Date date) {
        return new SimpleDateFormat("dd.MM.yy HH:mm").format(date);
    }
}
