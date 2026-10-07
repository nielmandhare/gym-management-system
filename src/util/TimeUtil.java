/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author mgssr
 */
public class TimeUtil {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("d");

    public static String getCurrentTime() {
        return LocalTime.now().format(TIME_FORMAT);
    }

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    public static String getCurrentDay() {
        return LocalDate.now().format(DAY_FORMAT);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMAT);
    }

    public static String getFormattedTime(String pattern) {
        return LocalTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getFormattedDate(String pattern) {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getFormattedDateTime(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getMonthStartDate() {
        return YearMonth.from(LocalDate.now()).atDay(1).format(DATE_FORMAT) + " 00:00:00";
    }

    public static String getMonthEndDate() {
        return YearMonth.from(LocalDate.now()).atEndOfMonth().format(DATE_FORMAT) + " 23:59:59";
    }

    public static String getDayStart() {
        return getCurrentDate() + " 00:00:00";
    }

    public static String getDayEnd() {
        return getCurrentDate() + " 23:59:59";
    }
}
