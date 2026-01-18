package net.godlycow.org.modules;

import java.util.Collections;
import java.util.Map;

public class Module {

    private boolean enabled;
    private final Map<String, Boolean> commands;

    public Module(boolean enabled, Map<String, Boolean> commands) {
        this.enabled = enabled;
        this.commands = commands;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isCommandEnabled(String command) {
        return commands.getOrDefault(command.toLowerCase(), true);
    }

    public void setCommandEnabled(String command, boolean enabled) {
        commands.put(command.toLowerCase(), enabled);
    }

    public Map<String, Boolean> getCommands() {return Collections.unmodifiableMap(commands);
    }

}
