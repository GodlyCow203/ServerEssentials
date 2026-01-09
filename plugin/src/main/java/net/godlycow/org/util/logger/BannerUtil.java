package net.godlycow.org.util.logger;

import org.bukkit.Bukkit;

public class BannerUtil {

    public static void printBanner(long startupTimeMillis) {
        String serverSoftware = Bukkit.getName() + " " + Bukkit.getVersion();
        String cores = String.valueOf(Runtime.getRuntime().availableProcessors());
        String threads = String.valueOf(Thread.activeCount());

        String[] infoLines = new String[]{
                AnsiColorUtil.primary("Server Software: ") + AnsiColorUtil.secondary(serverSoftware),
                AnsiColorUtil.primary("Startup Time:   ") + AnsiColorUtil.secondary(startupTimeMillis + " ms"),
                AnsiColorUtil.primary("CPU Cores:      ") + AnsiColorUtil.secondary(cores),
                AnsiColorUtil.primary("Threads:        ") + AnsiColorUtil.secondary(threads),
                "",
                AnsiColorUtil.success("ServerEssentials successfully enabled!")
        };

        String[] asciiLogo = new String[]{
                "             ",
                "  ┏━━━┳━━━┓  ",
                "  ┃┏━┓┃┏━━┛  ",
                "  ┃┗━━┫┗━━┓  ",
                "  ┗━━┓┃┏━━┛  ",
                "  ┃┗━┛┃┗━━┓  ",
                "  ┗━━━┻━━━┛  "
        };

        System.out.println();

        for (int i = 0; i < asciiLogo.length; i++) {
            StringBuilder line = new StringBuilder(AnsiColorUtil.colorize(AnsiColorUtil.GRAY, asciiLogo[i]));

            if (i > 0 && i - 1 < infoLines.length) {
                line.append("   ").append(infoLines[i - 1]);
            }

            System.out.println(line);
        }

        System.out.println();


    }
}