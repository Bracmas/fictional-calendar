package com.bracmas.fictional_calendars;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FictionalCalendarCommand implements CommandExecutor, TabCompleter {

    private final FictionalCalendar plugin;

    public FictionalCalendarCommand(FictionalCalendar plugin) {
        this.plugin = plugin;
    }

    // Sends the help message to the command sender with improved formatting
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§b§l=== Fictional Calendar Help ==="); // Blue bold header
        sender.sendMessage("§9§lCommands:"); // Dark blue bold section header
        sender.sendMessage("  §e/fc help §7- Show this help message.");
        sender.sendMessage("  §e/fc date [short|long] §7- Show the current fictional date.");
        sender.sendMessage("    §bParameters:"); // Light blue for parameters
        sender.sendMessage("      §3short §7- Show date in short format (default)."); // Dark aqua for options
        sender.sendMessage("      §3long §7- Show date with weekday.");
        sender.sendMessage("  §e/fc setdate <day> <month> <year> §7- Set the current fictional date.");
        sender.sendMessage("    §bParameters:");
        sender.sendMessage("      §3day §7- Day number (1 to days per month).");
        sender.sendMessage("      §3month §7- Month number (1 to total months).");
        sender.sendMessage("      §3year §7- Year number (positive value).");
        sender.sendMessage("  §e/fc reload §7- Reload the plugin configuration.");
        sender.sendMessage("§b§l=============================="); // Blue bold footer
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no arguments or "help" argument, send help message
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        // Handle different subcommands
        switch (args[0].toLowerCase()) {
            case "reload":
                // Check for reload permission
                if (!sender.hasPermission("fictionalcalendar.reload")) {
                    sender.sendMessage("§cYou do not have permission to reload.");
                    return true;
                }
                plugin.reloadConfig(); // Reload the plugin configuration
                plugin.loadData(); // Reload plugin data
                sender.sendMessage("§aFictional calendar config reloaded.");
                return true;

            case "date":
                // Check for date view permission
                if (!sender.hasPermission("fictionalcalendar.date")) {
                    sender.sendMessage("§cYou do not have permission to view the date.");
                    return true;
                }
                // Determine the style (short/long) from arguments or config
                String style = (args.length > 1) ? args[1] : plugin.getConfig().getString("calendar.date-style", "short");
                String format = plugin.getConfig().getString("calendar.date-format", "dd-mm-yyyy");
                boolean useText = plugin.getConfig().getString("calendar.month-format", "text").equalsIgnoreCase("text");
                // Send the formatted current date
                sender.sendMessage("§6Current Date: §e" + plugin.getFormattedDate(style, format, useText));
                return true;

            case "setdate":
                // Check for setdate permission
                if (!sender.hasPermission("fictionalcalendar.setdate")) {
                    sender.sendMessage("§cYou do not have permission to set the date.");
                    return true;
                }
                // Validate argument count for setdate command
                if (args.length != 4) {
                    sender.sendMessage("§cUsage: /fc setdate <day> <month> <year>");
                    return true;
                }
                try {
                    // Parse day, month, year from arguments
                    int d = Integer.parseInt(args[1]);
                    int m = Integer.parseInt(args[2]);
                    int y = Integer.parseInt(args[3]);

                    int maxDay = plugin.getDaysPerMonth();
                    int maxMonth = plugin.getTotalMonths();

                    // Validate day, month, and year ranges
                    if (d < 1 || d > maxDay) {
                        sender.sendMessage("§cInvalid day. Must be 1–" + maxDay + ".");
                        return true;
                    }
                    if (m < 1 || m > maxMonth) {
                        sender.sendMessage("§cInvalid month. Must be 1–" + maxMonth + ".");
                        return true;
                    }
                    if (y < 1) {
                        sender.sendMessage("§cInvalid year. Must be positive.");
                        return true;
                    }

                    plugin.setDate(d, m, y); // Set the new date
                    sender.sendMessage("§aDate set to §e" + plugin.getFormattedDate("long", "dd-mm-yyyy", true));
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number format."); // Handle number parsing errors
                }
                return true;

            default:
                sender.sendMessage("§cUnknown subcommand. Use /fc help for help."); // Default for unknown subcommands
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Autocomplete for the first argument (subcommand)
            List<String> completions = new ArrayList<>();
            if ("help".startsWith(args[0].toLowerCase())) completions.add("help");
            if ("date".startsWith(args[0].toLowerCase())) completions.add("date");
            if ("setdate".startsWith(args[0].toLowerCase())) completions.add("setdate");
            if ("reload".startsWith(args[0].toLowerCase())) completions.add("reload");
            return completions;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("date")) {
                // Autocomplete for "date" subcommand's style argument
                List<String> completions = new ArrayList<>();
                if ("short".startsWith(args[1].toLowerCase())) completions.add("short");
                if ("long".startsWith(args[1].toLowerCase())) completions.add("long");
                return completions;
            } else if (args[0].equalsIgnoreCase("setdate")) {
                // Autocomplete for "setdate" subcommand's day argument
                // Provide a range of numbers for the day
                return IntStream.rangeClosed(1, plugin.getDaysPerMonth())
                        .mapToObj(String::valueOf)
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setdate")) {
                // Autocomplete for "setdate" subcommand's month argument
                // Provide a range of numbers for the month
                return IntStream.rangeClosed(1, plugin.getTotalMonths())
                        .mapToObj(String::valueOf)
                        .filter(s -> s.startsWith(args[2]))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("setdate")) {
                // Autocomplete for "setdate" subcommand's year argument
                // Suggest a reasonable range for years, e.g., current year +/- a few
                List<String> completions = new ArrayList<>();
                int currentYear = plugin.getYear();
                for (int i = -5; i <= 5; i++) { // Suggest years around the current fictional year
                    String yearStr = String.valueOf(currentYear + i);
                    if (yearStr.startsWith(args[3])) {
                        completions.add(yearStr);
                    }
                }
                return completions;
            }
        }
        return Collections.emptyList(); // No completions for other cases
    }
}