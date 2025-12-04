package net.lunark.io.commands;

import java.util.UUID;

/**
 * Immutable record representing a command state
 */
public record CommandState(UUID playerId, String command, String key, String value) {

    /**
     * Convenience method to parse boolean state
     */
    public boolean asBoolean() {
        return Boolean.parseBoolean(value);
    }

    /**
     * Convenience method to parse integer state
     */
    public int asInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Convenience method to parse double state
     */
    public double asDouble() {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}