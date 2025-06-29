package com.bracmas.fictional_calendars;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PAPIExpansion extends PlaceholderExpansion {

    private final FictionalCalendar plugin;

    public PAPIExpansion(FictionalCalendar plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "fictionalcalendar";
    }

    @Override
    public String getAuthor() {
        return "bracmas";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        // Get formatting preferences from the plugin's configuration
        String format = plugin.getConfig().getString("calendar.date-format", "dd-mm-yyyy");
        boolean useText = plugin.getConfig().getString("calendar.month-format", "text").equalsIgnoreCase("text");
        String style = plugin.getConfig().getString("calendar.date-style", "short");


        // Handle different placeholder identifiers
        switch (identifier.toLowerCase()) {
            case "date":
                // Returns the full formatted date based on plugin's config style and format
                return plugin.getFormattedDate(style, format, useText);
            case "weekday":
                // Returns the name of the current weekday
                return plugin.getWeekdayName();
            case "date_long":
                // Returns the long formatted date, overriding the config style to "long"
                return plugin.getFormattedDate("long", format, useText);
            case "day_number":
                // Returns the current day number
                return String.valueOf(plugin.getDay());
            case "weekday_name":
                // Returns the current weekday name
                return plugin.getWeekdayName();
            case "month_number":
                // Returns the current month number
                return String.valueOf(plugin.getMonth());
            case "month_name":
                // Returns the current month name
                return plugin.getMonthName();
            case "year":
                // Returns the current year number
                return String.valueOf(plugin.getYear());
        }
        return null; // Return null for unknown placeholders
    }
}
