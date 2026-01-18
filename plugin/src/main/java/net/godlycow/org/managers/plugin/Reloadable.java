package net.godlycow.org.managers.plugin;

import org.bukkit.command.CommandSender;

public interface Reloadable {
    String name();

    void reload() throws Exception;
}
