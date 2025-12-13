package com.serveressentials.api;

import org.jetbrains.annotations.NotNull;

/**
 * Public API entry point for ServerEssentials.
 * Thread-safe singleton. Always check {@link #isAvailable()} before use.
 */
public interface ServerEssentialsAPI {

    /** API version for compatibility checks */
    String API_VERSION = "2.0.7.1";

    /**
     * Gets the singleton API instance.
     * @return API instance (never null when plugin is loaded)
     * @throws IllegalStateException if plugin is not loaded
     */
    @NotNull
    static ServerEssentialsAPI getInstance() {
        return net.lunark.io.api.APIImpl.getInstance();
    }

    /**
     * Checks if the API is ready for use.
     * @return true if plugin is enabled and API is initialized
     */
    boolean isAvailable();

    /**
     * Gets the Vault API for vault operations.
     * @return VaultAPI instance (never null when isAvailable() is true)
     */
    @NotNull
    VaultAPI getVaults();

    /**
     * Gets the API version string.
     * @return semantic version string
     */
    @NotNull
    default String getAPIVersion() {
        return API_VERSION;
    }
}