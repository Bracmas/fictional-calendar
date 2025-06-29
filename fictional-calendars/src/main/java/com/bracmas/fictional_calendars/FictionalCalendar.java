package com.bracmas.fictional_calendars;


import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FictionalCalendar extends JavaPlugin {
    private long lastUpdate;
    private int day, month, year;
    private List<String> monthNames;
    private List<String> weekdays;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadData();

        // Register the FictionalCalendar command with its executor
        getCommand("fictionalcalendar").setExecutor(new FictionalCalendarCommand(this));
        // Set the TabCompleter for the fictionalcalendar command to handle auto-completion
        getCommand("fictionalcalendar").setTabCompleter(new FictionalCalendarCommand(this));


        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            // Register the PAPI expansion if PlaceholderAPI is enabled
            new PAPIExpansion(this).register();
        }

        // Schedule an asynchronous task to update the fictional date
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long hoursPassed = TimeUnit.MILLISECONDS.toHours(now - lastUpdate);
                // Get the fictional days per real day from config, default to 3.0
                double fictionalDaysPerRealDay = getConfig().getDouble("calendar.fictional-days-per-real-day", 3.0);
                // Calculate hours per fictional day
                long hoursPerFictionalDay = (long)(24 / fictionalDaysPerRealDay);

                int daysPassed = (int)(hoursPassed / hoursPerFictionalDay);
                if (daysPassed > 0) {
                    // Increment days and update lastUpdate time
                    incrementDays(daysPassed);
                    lastUpdate += TimeUnit.HOURS.toMillis(daysPassed * hoursPerFictionalDay);
                    saveData(); // Save data after incrementing
                }
            }
            // Run the task asynchronously, starting immediately and repeating every 30 minutes (20L ticks * 60 seconds/minute * 30 minutes)
        }.runTaskTimerAsynchronously(this, 0L, 20L * 60 * 30);
    }

    @Override
    public void onDisable() {
        saveData(); // Save data when the plugin is disabled
    }
    
    // Load plugin data from config
    public void loadData() {
        // Parse start date from config, default to "01-01-1000"
        String[] startParts = getConfig().getString("calendar.start-date", "01-01-1000").split("-");
        // Load day, month, year, and lastUpdate from config
        day = getConfig().getInt("calendar.day", Integer.parseInt(startParts[0]));
        month = getConfig().getInt("calendar.month", Integer.parseInt(startParts[1]));
        year = getConfig().getInt("calendar.year", Integer.parseInt(startParts[2]));
        lastUpdate = getConfig().getLong("calendar.lastUpdate", System.currentTimeMillis());
        monthNames = getConfig().getStringList("calendar.month-names");
        weekdays = getConfig().getStringList("calendar.weekdays");
    }

    public void saveData() {
        getConfig().set("calendar.day", day);
        getConfig().set("calendar.month", month);
        getConfig().set("calendar.year", year);
        getConfig().set("calendar.lastUpdate", lastUpdate);
        saveConfig(); // Save the config file
    }

    // Increment the fictional date by a given number of days
    private void incrementDays(int daysToAdd) {
        int daysPerMonth = getDaysPerMonth();
        int totalMonths = getTotalMonths();

        for (int i = 0; i < daysToAdd; i++) {
            day++;
            if (day > daysPerMonth) {
                day = 1;
                month++;
                if (month > totalMonths) {
                    month = 1;
                    year++;
                }
            }
        }
    }

    public void setDate(int d, int m, int y) {
        this.day = d;
        this.month = m;
        this.year = y;
        this.lastUpdate = System.currentTimeMillis(); // Reset lastUpdate on manual set
        saveData(); // Save data after setting the date
    }

    // Getters for day, month, and year
    public int getDay() { return day; }
    public int getMonth() { return month; }
    public int getYear() { return year; }

    // Get days per month from config, default to 56
    public int getDaysPerMonth() {
        return getConfig().getInt("calendar.days-per-month", 56);
    }

    // Get total number of months based on month names list size
    public int getTotalMonths() {
        return monthNames.size();
    }

    // Get the name of the current month
    public String getMonthName() {
        if (monthNames.size() >= month && month > 0) {
            return monthNames.get(month - 1);
        }
        return "Month-" + month; // Fallback if month name not found
    }

    // Get the name of the current weekday
    public String getWeekdayName() {
        if (weekdays.isEmpty()) return "Day"; // Fallback if no weekdays are defined
        // Calculate total days from year 1 to determine current weekday
        int totalDays = ((year - 1) * getTotalMonths() * getDaysPerMonth())
                + ((month - 1) * getDaysPerMonth())
                + (day - 1);
        return weekdays.get(totalDays % weekdays.size());
    }

    /**
     * Formats the fictional date based on the provided style, format, and month display preference.
     *
     * @param style "short" for just date, "long" for date with weekday.
     * @param format The format string (e.g., "dd-mm-yyyy", "mm/dd/yyyy", "yyyy MM dd").
     * Placeholders: 'dd' for day, 'mm' for month number, 'MM' for month name, 'yyyy' for year, 'ww' for weekday name.
     * @param useTextMonth True to use month names, false to use month numbers.
     * @return The formatted date string.
     */
    public String getFormattedDate(String style, String format, boolean useTextMonth) {
        String dateString = format;

        // Replace placeholders in the format string
        dateString = dateString.replace("dd", String.format("%02d", day));
        dateString = dateString.replace("mm", String.format("%02d", month));
        dateString = dateString.replace("MM", getMonthName());
        dateString = dateString.replace("yyyy", String.format("%04d", year));
        dateString = dateString.replace("ww", getWeekdayName()); // Added placeholder for weekday name

        // Apply "long" style if requested to include weekday name
        if ("long".equalsIgnoreCase(style)) {
            // Check if weekday is already part of the format string to avoid duplication
            if (!format.contains("ww")) {
                dateString = String.format("%s (%s)", dateString, getWeekdayName());
            }
        }
        return dateString;
    }

    /**
     * Overloaded method to format a specific date.
     * This is useful for PAPI placeholders that need to display specific parts of the date.
     *
     * @param d The day.
     * @param m The month.
     * @param y The year.
     * @param format The format string.
     * @param useTextMonth True to use month names, false to use month numbers.
     * @param includeWeekday True to include weekday in the formatted string, false otherwise.
     * @return The formatted date string.
     */
    public String getFormattedDate(int d, int m, int y, String format, boolean useTextMonth, boolean includeWeekday) {
        // Temporarily set the date to format specific values without changing the plugin's current date
        int originalDay = this.day;
        int originalMonth = this.month;
        int originalYear = this.year;

        this.day = d;
        this.month = m;
        this.year = y;

        String formatted = getFormattedDate(includeWeekday ? "long" : "short", format, useTextMonth);

        // Restore the original date
        this.day = originalDay;
        this.month = originalMonth;
        this.year = originalYear;

        return formatted;
    }
}