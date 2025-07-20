package serveressentials.serveressentials;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
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

public class ServerInfoCommand implements CommandExecutor {

    private final Plugin plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public ServerInfoCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    private String getPrefix() {
        String rawPrefix = ServerEssentials.getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l]&r ");
        return ChatColor.translateAlternateColorCodes('&', rawPrefix);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = getPrefix();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + ChatColor.RED + "Only players can use this command.");
            return true;
        }

        // RAM info
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;

        // Disk space info (free and total on server root)
        File root = new File(".");
        long freeDiskMB = root.getFreeSpace() / 1024 / 1024;
        long totalDiskMB = root.getTotalSpace() / 1024 / 1024;

        // TPS (using Paper API)
        double tps = Bukkit.getTPS()[0]; // 1-minute average TPS

        // CPU info (from OS bean)
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        int cpuCores = osBean.getAvailableProcessors();

        // CPU Load (if supported)
        String cpuLoadStr = "N/A";
        double systemLoadAvg = osBean.getSystemLoadAverage();

        try {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            double load = sunOsBean.getSystemCpuLoad();
            if (load >= 0) {
                cpuLoadStr = df.format(load * 100) + "%";
            }
        } catch (ClassCastException ignored) {}

        // JVM uptime
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeMXBean.getUptime();
        long uptimeSeconds = uptimeMillis / 1000;
        long uptimeMinutes = uptimeSeconds / 60;
        long uptimeHours = uptimeMinutes / 60;

        // JVM args
        List<String> jvmArgs = runtimeMXBean.getInputArguments();

        // Host info
        String hostname = "Unknown";
        String hostAddress = "Unknown";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostname = localHost.getHostName();
            hostAddress = localHost.getHostAddress();
        } catch (UnknownHostException ignored) {}

        // Network interfaces
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

        // Plugin info
        int pluginCount = Bukkit.getPluginManager().getPlugins().length;

        // Minecraft & Paper version
        String minecraftVersion = Bukkit.getVersion();
        String paperVersion = Bukkit.getName() + " " + Bukkit.getBukkitVersion();

        // Player counts
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();

        // GPU info (Java limitation)
        String gpuInfo = "Not Available";

        // Send info to player
        player.sendMessage(prefix + ChatColor.GOLD + "---- Server Info ----");
        player.sendMessage(prefix + ChatColor.YELLOW + "RAM Usage: " + ChatColor.WHITE + usedMemory + "MB / " + maxMemory + "MB");
        player.sendMessage(prefix + ChatColor.YELLOW + "Disk Space: " + ChatColor.WHITE + freeDiskMB + "MB free / " + totalDiskMB + "MB total");
        player.sendMessage(prefix + ChatColor.YELLOW + "TPS: " + ChatColor.WHITE + df.format(tps));
        player.sendMessage(prefix + ChatColor.YELLOW + "CPU Cores: " + ChatColor.WHITE + cpuCores);
        player.sendMessage(prefix + ChatColor.YELLOW + "CPU Load: " + ChatColor.WHITE + cpuLoadStr);
        player.sendMessage(prefix + ChatColor.YELLOW + "OS: " + ChatColor.WHITE + osName + " " + osVersion + " (" + osArch + ")");
        player.sendMessage(prefix + ChatColor.YELLOW + "System Load Average (1m): " + ChatColor.WHITE + (systemLoadAvg >= 0 ? df.format(systemLoadAvg) : "N/A"));
        player.sendMessage(prefix + ChatColor.YELLOW + "JVM Uptime: " + ChatColor.WHITE + uptimeHours + "h " + (uptimeMinutes % 60) + "m " + (uptimeSeconds % 60) + "s");
        player.sendMessage(prefix + ChatColor.YELLOW + "Java Version: " + ChatColor.WHITE + System.getProperty("java.version"));
        player.sendMessage(prefix + ChatColor.YELLOW + "Host: " + ChatColor.WHITE + hostname + " (" + hostAddress + ")");
        player.sendMessage(prefix + ChatColor.YELLOW + "Network Interfaces: " + ChatColor.WHITE + (netInterfaces.length() > 0 ? netInterfaces.substring(0, netInterfaces.length() - 2) : "None"));
        player.sendMessage(prefix + ChatColor.YELLOW + "Plugins Loaded: " + ChatColor.WHITE + pluginCount);
        player.sendMessage(prefix + ChatColor.YELLOW + "Minecraft Version: " + ChatColor.WHITE + minecraftVersion);
        player.sendMessage(prefix + ChatColor.YELLOW + "Server Version: " + ChatColor.WHITE + paperVersion);
        player.sendMessage(prefix + ChatColor.YELLOW + "Players Online: " + ChatColor.WHITE + onlinePlayers + "/" + maxPlayers);
        player.sendMessage(prefix + ChatColor.YELLOW + "GPU: " + ChatColor.WHITE + gpuInfo);
        player.sendMessage(prefix + ChatColor.YELLOW + "JVM Arguments: " + ChatColor.WHITE + String.join(", ", jvmArgs));

        return true;
    }
}
