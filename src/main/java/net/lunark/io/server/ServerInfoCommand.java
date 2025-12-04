package net.lunark.io.server;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import net.lunark.io.util.ServerMessages;

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

    private final ServerMessages messages;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public ServerInfoCommand(ServerMessages messages) {
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.get("ServerInfo.only-players"));
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

        int pluginCount = Bukkit.getPluginManager().getPlugins().length;
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        String minecraftVersion = Bukkit.getVersion();
        String paperVersion = Bukkit.getName() + " " + Bukkit.getBukkitVersion();

        String gpuInfo = "Not Available";

        player.sendMessage(messages.get("ServerInfo.header"));
        player.sendMessage(messages.get("ServerInfo.ram", "{used}", String.valueOf(usedMemory), "{max}", String.valueOf(maxMemory)));
        player.sendMessage(messages.get("ServerInfo.disk", "{free}", String.valueOf(freeDiskMB), "{total}", String.valueOf(totalDiskMB)));
        player.sendMessage(messages.get("ServerInfo.tps", "{tps}", df.format(tps)));
        player.sendMessage(messages.get("ServerInfo.cpu", "{cores}", String.valueOf(cpuCores), "{load}", cpuLoadStr));
        player.sendMessage(messages.get("ServerInfo.os", "{name}", osName, "{version}", osVersion, "{arch}", osArch));
        player.sendMessage(messages.get("ServerInfo.uptime", "{hours}", String.valueOf(uptimeHours), "{minutes}", String.valueOf(uptimeMinutes % 60), "{seconds}", String.valueOf(uptimeSeconds % 60)));
        player.sendMessage(messages.get("ServerInfo.java", "{version}", System.getProperty("java.version")));
        player.sendMessage(messages.get("ServerInfo.host", "{hostname}", hostname, "{address}", hostAddress));
        player.sendMessage(messages.get("ServerInfo.net", "{interfaces}", netInterfaces.length() > 0 ? netInterfaces.substring(0, netInterfaces.length() - 2) : "None"));
        player.sendMessage(messages.get("ServerInfo.plugins", "{count}", String.valueOf(pluginCount)));
        player.sendMessage(messages.get("ServerInfo.minecraft", "{version}", minecraftVersion));
        player.sendMessage(messages.get("ServerInfo.paper", "{version}", paperVersion));
        player.sendMessage(messages.get("ServerInfo.players", "{online}", String.valueOf(onlinePlayers), "{max}", String.valueOf(maxPlayers)));
        player.sendMessage(messages.get("ServerInfo.gpu", "{gpu}", gpuInfo));
        player.sendMessage(messages.get("ServerInfo.jvm", "{args}", String.join(", ", jvmArgs)));

        return true;
    }
}
