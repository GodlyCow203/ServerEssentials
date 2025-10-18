package serveressentials.serveressentials.util;

import org.bukkit.Bukkit;

public class BannerUtil {

    public static void printBanner(long startupTimeMillis) {
        String serverSoftware = Bukkit.getName() + " " + Bukkit.getVersion();
        String cores = String.valueOf(Runtime.getRuntime().availableProcessors());
        String threads = String.valueOf(Thread.activeCount());

        // ASCII lines
        String[] asciiLogo = new String[]{
                "             ",
                "  ┏━━━┳━━━┓  ",
                "  ┃┏━┓┃┏━━┛  ",
                "  ┃┗━━┫┗━━┓  ",
                "  ┗━━┓┃┏━━┛  ",
                "  ┃┗━┛┃┗━━┓  ",
                "  ┗━━━┻━━━┛  "
        };

        // Info lines (aligned to appear on the right side of ASCII logo)
        String[] infoLines = new String[]{
                "§7Server Software: §f" + serverSoftware,
                "§7Startup Time:   §f" + startupTimeMillis + " ms",
                "§7CPU Cores:      §f" + cores,
                "§7Threads:        §f" + threads,
                "§aServerEssentials successfully enabled!"
        };

        Bukkit.getConsoleSender().sendMessage(" ");

        // Print ASCII with appended info
        for (int i = 0; i < asciiLogo.length; i++) {
            String line = "§7" + asciiLogo[i];
            if (i > 0 && i - 1 < infoLines.length) {
                line += " " + infoLines[i - 1]; // append info next to ASCII
            }
            Bukkit.getConsoleSender().sendMessage(line);
        }

        Bukkit.getConsoleSender().sendMessage(" ");
    }
}
