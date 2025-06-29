package com.bracmas.fictional_calendars;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DateCommand implements CommandExecutor {
    private final FictionalCalendar plugin;

    public DateCommand(FictionalCalendar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String style = plugin.getConfig().getString("calendar.date-style", "short");
        String format = plugin.getConfig().getString("calendar.date-format", "dd-mm-yyyy");
        String monthFormat = plugin.getConfig().getString("calendar.month-format", "text");
        boolean useText = monthFormat.equalsIgnoreCase("text");

        String dateStr = plugin.getFormattedDate(style, format, useText);
        sender.sendMessage("§6Current Date: §e" + dateStr);
        return true;
    }
}

