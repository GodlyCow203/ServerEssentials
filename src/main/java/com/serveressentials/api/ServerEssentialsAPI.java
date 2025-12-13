package com.serveressentials.api;

public interface ServerEssentialsAPI {

    /**
     * Get the singleton instance of the API.
     * @return API instance, or null if plugin is not loaded
     */
    static ServerEssentialsAPI getInstance() {
        throw new IllegalStateException("API not initialized. Is ServerEssentials installed?");
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