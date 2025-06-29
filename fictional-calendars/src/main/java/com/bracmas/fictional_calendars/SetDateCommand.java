package com.bracmas.fictional_calendars;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetDateCommand implements CommandExecutor {
    private final FictionalCalendar plugin;

    public SetDateCommand(FictionalCalendar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("§cUsage: /setdate <day> <month> <year>");
            return true;
        }
        try {
            int d = Integer.parseInt(args[0]);
            int m = Integer.parseInt(args[1]);
            int y = Integer.parseInt(args[2]);

            int maxDay = plugin.getDaysPerMonth();
            int maxMonth = plugin.getTotalMonths();

            if (d < 1 || d > maxDay) {
                sender.sendMessage("§cInvalid day. It must be between 1 and " + maxDay + ".");
                return true;
            }
            if (m < 1 || m > maxMonth) {
                sender.sendMessage("§cInvalid month. It must be between 1 and " + maxMonth + ".");
                return true;
            }
            if (y < 1) {
                sender.sendMessage("§cInvalid year. It must be positive.");
                return true;
            }

            plugin.setDate(d, m, y);
            sender.sendMessage("§aDate set to §e" + plugin.getFormattedDate("long", "dd-mm-yyyy", true));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number format.");
        }
        return true;
    }
}
