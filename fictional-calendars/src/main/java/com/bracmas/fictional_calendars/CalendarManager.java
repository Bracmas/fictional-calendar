package com.bracmas.fictional_calendars;

import java.util.List;
import java.util.Optional;

public class CalendarManager {

    private int day;
    private int month; // 1-based index
    private int year;
    private int weekdayIndex; // 0-based index

    private List<String> monthNames;
    private List<String> weekdays;

    private boolean useMonthNames; // from config month-format: text/numeric
    private boolean useDayNames;   // optional, if you want to support named days

    public CalendarManager(List<String> monthNames, List<String> weekdays, boolean useMonthNames, boolean useDayNames) {
        this.monthNames = monthNames;
        this.weekdays = weekdays;
        this.useMonthNames = useMonthNames;
        this.useDayNames = useDayNames;
        // Initialize other fields as needed
    }

    public int getDay() {
        return day;
    }

    public Optional<String> getDayName() {
        if (useDayNames && day - 1 < weekdays.size() && day - 1 >= 0) {
            return Optional.of(weekdays.get((day - 1) % weekdays.size()));
        }
        return Optional.empty();
    }

    public int getMonth() {
        return month;
    }

    public String getMonthName() {
        if (useMonthNames && month - 1 < monthNames.size() && month - 1 >= 0) {
            return monthNames.get(month - 1);
        }
        return String.valueOf(month);
    }

    public int getYear() {
        return year;
    }

    public String getWeekdayName() {
        if (weekdayIndex >= 0 && weekdayIndex < weekdays.size()) {
            return weekdays.get(weekdayIndex);
        }
        return "";
    }

    public int getWeekdayIndex() {
        return weekdayIndex;
    }

    // Example setters and update methods for day, month, year, and weekdayIndex
    public void setDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
        // update weekdayIndex based on day or custom logic if needed
    }

    public void setWeekdayIndex(int index) {
        this.weekdayIndex = index;
    }

    // You should add logic to update the weekdayIndex properly when date changes.
}
