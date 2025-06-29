# **Fictional Calendar Plugin**

The Fictional Calendar Plugin for Paper/Spigot servers allows you to implement a custom, in-game calendar system that runs parallel to real-world time. This is perfect for role-playing servers, lore-heavy communities, or any server that desires a unique temporal progression.

## **Features**

* **Customizable Calendar:** Define your own month names, number of days per month, and weekday names.  
* **Time Progression:** Fictional days advance automatically based on a configurable real-world time ratio.  
* **Date Persistence:** The current fictional date is saved and loaded automatically, ensuring continuity across server restarts.  
* **In-Game Commands:** Easy-to-use commands to view and manage the current date.  
* **PlaceholderAPI Integration:** Seamlessly display the fictional date and time elements in various plugins (e.g., chat, scoreboards, holographic displays).  
* **Configurable Formatting:** Full control over how the date is displayed, including order, separators, and text/numeric month display.  
* **Command Auto-completion:** User-friendly tab completion for all /fc commands.

## **How it Works**

The plugin operates by tracking real-world time elapsed since its last update. Based on the days-per-irl-day setting in your config.yml, it calculates how many fictional days have passed and increments the calendar accordingly. This process runs asynchronously in the background, ensuring minimal impact on server performance.

The current date (day, month, year, and lastUpdate timestamp) is persistently stored in config.yml and reloaded upon server startup, maintaining the calendar's state.

## **Failsafe Abilities**

The Fictional Calendar plugin incorporates several mechanisms to ensure data integrity and consistent time progression, even across server restarts or unexpected shutdowns.

* **Automatic Date Saving (Persistence):**  
  * The plugin utilizes Bukkit's configuration API (JavaPlugin.getConfig().set(...) and JavaPlugin.saveConfig()) to persist the day, month, year, and critically, the lastUpdate timestamp to the config.yml file.  
  * Data is saved in two primary scenarios:  
    1. **Periodically:** The asynchronous BukkitRunnable responsible for time progression (running every 30 minutes by default) calls saveData() after each increment, ensuring that the current state is frequently written to disk.  
    2. **On Plugin Disable:** The onDisable() method is overridden to explicitly call saveData(), guaranteeing that the latest calendar state is saved just before the server shuts down or the plugin is unloaded. This minimizes data loss during planned restarts.  
* **lastUpdate Mechanism (Time Synchronization):**  
  * The lastUpdate field stores the System.currentTimeMillis() value corresponding to the exact real-world timestamp when the fictional calendar was last successfully incremented and saved.  
  * Upon server startup (onEnable()), this lastUpdate value is loaded.  
  * The core time progression logic then calculates hoursPassed \= TimeUnit.MILLISECONDS.toHours(now \- lastUpdate). This now is the current System.currentTimeMillis().  
  * This calculation precisely determines how much real-world time has elapsed since the *last recorded update*, regardless of server downtime.  
  * The plugin then "catches up" by applying the appropriate number of fictional day increments (daysPassed) based on hoursPassed and fictional-days-per-real-day. The lastUpdate is then advanced accordingly (lastUpdate \+= TimeUnit.HOURS.toMillis(daysPassed \* hoursPerFictionalDay)), ensuring continuity.  
  * This prevents time inconsistencies or skips if the server is offline for a short period (e.g., 2 minutes) or a long one, as the calendar state always synchronizes to the correct fictional time relative to real-world elapsed time.  
* **Configurable Start Date (Initialization & Recovery):**  
  * The calendar.start-date property in config.yml serves as a baseline.  
  * During loadData(), if the day, month, or year properties are missing or malformed in config.yml (e.g., after a fresh installation or manual corruption), the plugin defaults to parsing the start-date string to initialize the calendar's position.  
  * Similarly, if lastUpdate is missing or set to 0, the plugin uses the current System.currentTimeMillis() as the initial lastUpdate, effectively starting the time tracking from the plugin's current enable time. This provides a robust initialization and recovery point for the calendar's state.  
* **Input Validation (Command Security & Integrity):**  
  * The /fc setdate command implements strict input validation within FictionalCalendarCommand.java.  
  * Before applying any date changes, the parsed day, month, and year integers are checked against:  
    * **Day Range:** 1 to plugin.getDaysPerMonth() (dynamically determined by the configured days-per-month which typically defaults to 56 based on the internal logic if not explicitly set in config, or based on a direct config value if days-per-month is added to config).  
    * **Month Range:** 1 to plugin.getTotalMonths() (dynamically determined by the number of month-names defined in config.yml).  
    * **Year Validity:** year \< 1 (ensures the year is positive).  
  * NumberFormatException is caught to handle non-integer inputs gracefully.  
  * Invalid inputs result in informative error messages sent back to the command sender, preventing the calendar from being set to an illogical or out-of-bounds date and maintaining the system's integrity.

## **Configuration (config.yml)**

The config.yml file is the heart of your Fictional Calendar's customization.
```
\# Fictional Calendar Configuration  
calendar:  
  \# The fictional start date used when the server starts for the first time.  
  \# This date is used to initialize the calendar if 'day', 'month', or 'year' are not explicitly set below, or if 'lastUpdate' is 0\.  
  \# Format: "dd-mm-yyyy" (day-month-year).  
  start-date: "01-01-1000"

  \# The last real-world time (in milliseconds) when the calendar was updated by the plugin's internal tick.  
  \# This value is crucial for calculating how many fictional days have passed since the last update.  
  \# Do NOT modify this value manually unless you know what you're doing, as it can cause date desynchronization.  
  \# If you are manually setting the 'day', 'month', or 'year' below, you should set this to 0 or remove it,  
  \# so the plugin recalculates the 'lastUpdate' on startup.  
  lastUpdate: 0

  \# The current fictional date settings. These values represent the plugin's active date.  
  \# The plugin will automatically update these values based on the 'days-per-irl-day' setting.  
  day: 1        \# Day of the current fictional month (must be 1 to the number of days specified in 'days-per-month' in plugin logic).  
  month: 1      \# Month number (must be 1 to the total number of months defined in 'month-names' list below).  
  year: 1000    \# Fictional year (must be a positive integer).

  \# Number of fictional days that pass for every one real-life day.  
  \# For example, a value of 3.0 means 3 fictional days will pass for every 24 real-life hours.  
  \# Fractional values are allowed (e.g., 0.5 for half a fictional day per real day).  
  \# I strongly recommend to leave it as is if you are satisfied with the current progression speed.  
  days-per-irl-day: 3.0

  \# The primary format for displaying the date in chat commands (e.g., /fc date) and some PAPI placeholders.  
  \# You can customize this string using placeholders:  
  \#   \- dd: Day (e.g., 01, 15, 30\)  
  \#   \- mm: Month number (e.g., 01, 06, 12\)  
  \#   \- MM: Month name (e.g., Love, Chaos, Evil)  
  \#   \- yyyy: Year (e.g., 1000, 1999, 2025\)  
  \#   \- ww: Weekday name (e.g., Anth, Sil, Theyim)  
  \# Examples:  
  \#   "dd-mm-yyyy" \-\> 01-01-1000  
  \#   "mm/dd/yyyy" \-\> 01/01/1000  
  \#   "MM dd, yyyy" \-\> Love 01, 1000  
  \#   "dd of MM, yyyy (ww)" \-\> 01 of Love, 1000 (Anth)  
  date-format: "dd-mm-yyyy"

  \# The default style for date output when using the /fc date command without specifying 'short' or 'long'.  
  \#   \- "short": Displays only the date according to 'date-format'.  
  \#   \- "long": Displays the date according to 'date-format' and appends the weekday name (if not already included with 'ww').  
  date-style: "short"

  \# Whether months should be displayed as their text names or as numbers.  
  \# This affects how 'mm' and 'MM' are interpreted in 'date-format' and also influences PAPI output for month.  
  \#   \- "text": Displays month names from the 'month-names' list (e.g., Love, Chaos).  
  \#   \- "numeric": Displays month numbers (e.g., 1, 6, 12).  
  month-format: "text"

  \# List of month names used in your fictional calendar.  
  \# The total number of months in your calendar is determined by the number of items in this list.  
  \# Months are indexed starting from 1 (e.g., the first name in the list is Month 1, the second is Month 2, etc.).  
  month-names:  
    \- Love  
    \- Power  
    \- Order  
    \- Knowledge  
    \- Oblivion  
    \- Chaos  
    \- Life  
    \- War  
    \- Righteousness  
    \- Science  
    \- Death  
    \- Evil

  \# List of weekday names used in your fictional calendar.  
  \# The total number of weekdays in your cycle is determined by the number of items in this list.  
  \# The plugin calculates the current weekday by taking the total fictional days passed modulo the size of this list.  
  weekdays:  
    \- Anth  
    \- Asmo  
    \- Roth  
    \- Lull  
    \- Sil  
    \- Varth  
    \- Theyim
```
## **Commands**

All commands start with /fictionalcalendar or the alias /fc.

* /fc help: Displays the in-game help message with available commands and their usage.  
* /fc date \[short|long\]: Shows the current fictional date.  
  * short: Displays the date using the format defined in config.yml's date-format (default).  
  * long: Displays the date using the format defined in date-format, appending the current weekday name (e.g., "01 of Love, 1000 (Anth)").  
* /fc setdate \<day\> \<month\> \<year\>: Manually sets the current fictional date.  
  * \<day\>: The day number (1 to days-per-month configured).  
  * \<month\>: The month number (1 to the total number of month-names).  
  * \<year\>: The year number (must be positive).  
  * **Tab Completion:** This command supports tab completion, guiding you through entering the day, month, and year with hints.  
* /fc reload: Reloads the plugin's config.yml from disk. Useful for applying configuration changes without a server restart.

### **Permissions**

* fictionalcalendar.use: Allows usage of the base /fc command and its aliases. (Default: true)  
* fictionalcalendar.date: Allows a player to use the /fc date subcommand. (Default: true)  
* fictionalcalendar.setdate: Allows a player to use the /fc setdate subcommand. (Default: op)  
* fictionalcalendar.reload: Allows a player to use the /fc reload subcommand. (Default: op)

## **PlaceholderAPI Integration**

This plugin integrates with PlaceholderAPI, allowing you to display various elements of your fictional calendar in other plugins (e.g., chat, scoreboards, holographic displays, custom GUIs).

**To use PlaceholderAPI placeholders:**

1. Ensure PlaceholderAPI is installed and enabled on your server.  
2. Restart your server or use /papi reload to register the new placeholders.  
3. Use the placeholders in any plugin that supports PlaceholderAPI.

Here's a list of available placeholders:

* %fictionalcalendar\_date%: Displays the full formatted date based on the calendar.date-style, calendar.date-format, and calendar.month-format in your config.yml.  
* %fictionalcalendar\_date\_long%: Displays the full formatted date in "long" style, regardless of the date-style config, including the weekday name.  
* %fictionalcalendar\_weekday% (or %fictionalcalendar\_weekday\_name%): Displays the current weekday's name (e.g., "Anth", "Sil").  
* %fictionalcalendar\_day\_number%: Displays the current day of the month as a number (e.g., "1", "15").  
* %fictionalcalendar\_month\_number%: Displays the current month as a number (e.g., "1", "6").  
* %fictionalcalendar\_month\_name%: Displays the current month's name (e.g., "Love", "Chaos").  
* %fictionalcalendar\_year%: Displays the current fictional year (e.g., "1000", "2025").

**Example Usage in Chat Plugin (e.g., EssentialsX):**

format: '&7\[%fictionalcalendar\_date% &8| \&f%fictionalcalendar\_weekday%\] \&r{DISPLAY\_NAME}: {MESSAGE}'

This would display something like: \[01-Love-1000 | Anth\] PlayerName: Hello\!

## **Installation**

1. Place the FictionalCalendar.jar file into your server's plugins/ folder.  
2. (Optional but Recommended) Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for full functionality.  
3. Start or restart your server.  
4. Edit the generated config.yml in plugins/FictionalCalendar/ to customize your calendar.  
5. Use /fc reload or restart your server to apply config changes.

## **Troubleshooting**

* **Plugin not loading:**  
  * Ensure your server version is compatible with api-version: 1.20 specified in plugin.yml.  
  * Check the server console for any error messages related to FictionalCalendar during startup.  
* **Date not updating:**  
  * Verify calendar.days-per-irl-day is set to a positive value in config.yml.  
  * Check for any errors in the server console that might indicate issues with the plugin's background task.  
  * Ensure lastUpdate in config.yml is not a future timestamp. If it is, consider setting it to 0 and reloading the plugin.  
* **PlaceholderAPI placeholders not working:**  
  * Confirm PlaceholderAPI is installed and enabled (/plugins command).  
  * Run /papi reload after installing or updating the Fictional Calendar plugin.  
  * Double-check the placeholder syntax (e.g., %fictionalcalendar\_date%).  
* **Commands not found or permissions issues:**  
  * Ensure the plugin is loaded (/plugins command).  
  * Check the plugin.yml for correct command and permission definitions.  
  * Verify your permissions plugin is configured correctly and that players have the necessary permissions (e.g., fictionalcalendar.use).  
* **Invalid Date Errors (/fc setdate):**  
  * Ensure the day is within the configured days-per-month (determined by the plugin's internal logic, typically 56 based on the default config).  
  * Ensure the month number is between 1 and the total number of month-names defined in config.yml.  
  * Ensure the year is a positive integer.

Developed by: bracmas  
Version: 1.0
