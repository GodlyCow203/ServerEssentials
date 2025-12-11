package net.lunark.io.commands;

import java.util.UUID;


public record CommandState(UUID playerId, String command, String key, String value) {


    public boolean asBoolean() {
        return Boolean.parseBoolean(value);
    }


    public int asInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double asDouble() {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}