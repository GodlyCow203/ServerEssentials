package com.serveressentials.api;

public interface ServerEssentialsAPI {

    /**
     * Internal holder for the API instance
     */
    final class Holder {
        static ServerEssentialsAPI INSTANCE = null;
    }

    /**
     * Called by ServerEssentials on enable to provide the implementation
     * @param impl The API implementation
     */
    static void provide(ServerEssentialsAPI impl) {
        Holder.INSTANCE = impl;
    }

    /**
     * Get the singleton instance of the API.
     * @return API instance
     * @throws IllegalStateException if the plugin is not loaded
     */
    static ServerEssentialsAPI getInstance() {
        if (Holder.INSTANCE == null) {
            // Try to get from Bukkit's service manager as fallback
            org.bukkit.plugin.ServicesManager sm = org.bukkit.Bukkit.getServicesManager();
            ServerEssentialsAPI api = sm.load(ServerEssentialsAPI.class);
            if (api != null) {
                Holder.INSTANCE = api;
                return api;
            }
            throw new IllegalStateException("API not initialized. Is ServerEssentials installed?");
        }
        return Holder.INSTANCE;
    }

    /**
     * Get the vault manager for accessing player vaults.
     * @return Vault manager instance
     */
    VaultAPI getVaults();

    /**
     * Check if the plugin is enabled and ready.
     * @return true if API is available
     */
    boolean isAvailable();
}