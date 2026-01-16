package net.godlycow.org.commands.impl;

import net.godlycow.org.commands.config.ServerInfoConfig;
import net.godlycow.org.language.PlayerLanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;

import static net.godlycow.org.language.LanguageManager.ComponentPlaceholder;

public final class ServerInfoCommand implements CommandExecutor {
    private static final String PERMISSION = "essc.command.serverinfo";

    private final Plugin plugin;
    private final PlayerLanguageManager langManager;
    private final ServerInfoConfig config;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public ServerInfoCommand(Plugin plugin, PlayerLanguageManager langManager, ServerInfoConfig config) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(langManager.getMessageFor(null, "commands.serverinfo.only-player",
                    "<red>Only players can use this command."));
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.no-permission",
                    "<red>You do not have permission to use this command!"));
            return true;
        }

        long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        File root = new File(".");
        long freeDiskMB = root.getFreeSpace() / 1024 / 1024;
        long totalDiskMB = root.getTotalSpace() / 1024 / 1024;

        double tps = Bukkit.getTPS()[0];

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        int cpuCores = osBean.getAvailableProcessors();

        String cpuLoadStr = "N/A";
        try {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            double load = sunOsBean.getSystemCpuLoad();
            if (load >= 0) cpuLoadStr = df.format(load * 100) + "%";
        } catch (ClassCastException ignored) {}

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeMXBean.getUptime();
        long uptimeSeconds = uptimeMillis / 1000;
        long uptimeMinutes = uptimeSeconds / 60;
        long uptimeHours = uptimeMinutes / 60;
        List<String> jvmArgs = runtimeMXBean.getInputArguments();

        String hostname = "Unknown";
        String hostAddress = "Unknown";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostname = localHost.getHostName();
            hostAddress = localHost.getHostAddress();
        } catch (UnknownHostException ignored) {}

        StringBuilder netInterfaces = new StringBuilder();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface netIf = nets.nextElement();
                if (!netIf.isUp() || netIf.isLoopback()) continue;
                Enumeration<InetAddress> addresses = netIf.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.isLoopbackAddress()) continue;
                    netInterfaces.append(netIf.getDisplayName()).append(": ").append(addr.getHostAddress()).append(", ");
                }
            }
        } catch (SocketException ignored) {}
        String netInterfacesStr = netInterfaces.length() > 0 ? netInterfaces.substring(0, netInterfaces.length() - 2) : "None";

        int pluginCount = Bukkit.getPluginManager().getPlugins().length;
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        String minecraftVersion = Bukkit.getVersion();
        String paperVersion = Bukkit.getName() + " " + Bukkit.getBukkitVersion();

        String gpuInfo = "Not Available";

        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.header",
                "<gold>========== Server Information =========="));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.ram",
                "<green>RAM: <white>{used}MB / {max}MB",
                ComponentPlaceholder.of("{used}", String.valueOf(usedMemory)),
                ComponentPlaceholder.of("{max}", String.valueOf(maxMemory))));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.disk",
                "<green>Disk: <white>{free}MB free / {total}MB total",
                ComponentPlaceholder.of("{free}", String.valueOf(freeDiskMB)),
                ComponentPlaceholder.of("{total}", String.valueOf(totalDiskMB))));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.tps",
                "<green>TPS: <white>{tps}",
                ComponentPlaceholder.of("{tps}", df.format(tps))));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.cpu",
                "<green>CPU: <white>{cores} cores, {load} load",
                ComponentPlaceholder.of("{cores}", String.valueOf(cpuCores)),
                ComponentPlaceholder.of("{load}", cpuLoadStr)));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.os",
                "<green>OS: <white>{name} {version} ({arch})",
                ComponentPlaceholder.of("{name}", osName),
                ComponentPlaceholder.of("{version}", osVersion),
                ComponentPlaceholder.of("{arch}", osArch)));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.uptime",
                "<green>Uptime: <white>{hours}h {minutes}m {seconds}s",
                ComponentPlaceholder.of("{hours}", String.valueOf(uptimeHours)),
                ComponentPlaceholder.of("{minutes}", String.valueOf(uptimeMinutes % 60)),
                ComponentPlaceholder.of("{seconds}", String.valueOf(uptimeSeconds % 60))));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.java",
                "<green>Java: <white>{version}",
                ComponentPlaceholder.of("{version}", System.getProperty("java.version"))));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.host",
                "<green>Host: <white>{hostname} ({address})",
                ComponentPlaceholder.of("{hostname}", hostname),
                ComponentPlaceholder.of("{address}", hostAddress)));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.net",
                "<green>Network: <white>{interfaces}",
                ComponentPlaceholder.of("{interfaces}", netInterfacesStr)));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.plugins",
                "<green>Plugins: <white>{count}",
                ComponentPlaceholder.of("{count}", String.valueOf(pluginCount))));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.minecraft",
                "<green>Minecraft: <white>{version}",
                ComponentPlaceholder.of("{version}", minecraftVersion)));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.paper",
                "<green>Server: <white>{version}",
                ComponentPlaceholder.of("{version}", paperVersion)));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.players",
                "<green>Players: <white>{online}/{max}",
                ComponentPlaceholder.of("{online}", String.valueOf(onlinePlayers)),
                ComponentPlaceholder.of("{max}", String.valueOf(maxPlayers))));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.gpu",
                "<green>GPU: <white>{gpu}",
                ComponentPlaceholder.of("{gpu}", gpuInfo)));
        player.sendMessage(langManager.getMessageFor(player, "commands.serverinfo.jvm",
                "<green>JVM Args: <white>{args}",
                ComponentPlaceholder.of("{args}", String.join(", ", jvmArgs))));

        return true;
    }
}