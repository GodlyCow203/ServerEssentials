package serveressentials.serveressentials.pw;

import serveressentials.serveressentials.config.GUIConfig;

import java.util.List;

public class WarpCategories {

    private final GUIConfig guiConfig;

    public WarpCategories(GUIConfig guiConfig) {
        this.guiConfig = guiConfig;
    }

    public boolean isValidCategory(String category) {
        List<String> categories = guiConfig.getCategories();
        return categories.stream().anyMatch(c -> c.equalsIgnoreCase(category));
    }
}
