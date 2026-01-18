package net.godlycow.org.kit.gui;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KitGUIManager {
    private final Set<UUID> playersInMainGUI = ConcurrentHashMap.newKeySet();
    private final Set<UUID> playersInPreviewGUI = ConcurrentHashMap.newKeySet();

    public void setPlayerInMainGUI(UUID playerId, boolean isOpen) {
        if (isOpen) {
            playersInMainGUI.add(playerId);
        } else {
            playersInMainGUI.remove(playerId);
        }
    }

    public void setPlayerInPreviewGUI(UUID playerId, boolean isOpen) {
        if (isOpen) {
            playersInPreviewGUI.add(playerId);
        } else {
            playersInPreviewGUI.remove(playerId);
        }
    }

    public boolean isPlayerInAnyKitGUI(UUID playerId) {
        return playersInMainGUI.contains(playerId) || playersInPreviewGUI.contains(playerId);
    }

    public void cleanupPlayer(UUID playerId) {
        playersInMainGUI.remove(playerId);
        playersInPreviewGUI.remove(playerId);
    }
}