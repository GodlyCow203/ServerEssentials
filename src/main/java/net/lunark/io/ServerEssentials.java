

package net.lunark.io;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.lunark.io.Fun.*;
import net.lunark.io.Managers.*;
import net.lunark.io.PlaceholderAPI.ServerEssentialsPlaceholder;
import net.lunark.io.PlaceholderAPI.TopPlaytimePlaceholder;
import net.lunark.io.Player.*;
import net.lunark.io.Rtp.RtpCommand;
import net.lunark.io.Rtp.RtpConfig;
import net.lunark.io.Rtp.RtpListener;
import net.lunark.io.Rtp.RtpLocationStorage;
import net.lunark.io.TPA.TPACommand;
import net.lunark.io.TPA.TPAConfig;
import net.lunark.io.TPA.TPAListener;
import net.lunark.io.TPA.TPAStorage;
import net.lunark.io.Vault.VaultCommand;
import net.lunark.io.Vault.VaultManager;
import net.lunark.io.auction.*;
import net.lunark.io.commands.CommandDataStorage;
import net.lunark.io.commands.DatabaseCommand;
import net.lunark.io.commands.LanguageCommand;
import net.lunark.io.config.GUIConfig;
import net.lunark.io.database.DatabaseConfig;
import net.lunark.io.database.DatabaseManager;
import net.lunark.io.database.DatabaseType;
import net.lunark.io.economy.*;
import net.lunark.io.homes.HomeListener;
import net.lunark.io.homes.HomeManager;
import net.lunark.io.homes.HomesGUI;
import net.lunark.io.interaction_blocks.*;
import net.lunark.io.kit.*;
import net.lunark.io.kit.storage.KitStorage;
import net.lunark.io.commands.*;
import net.lunark.io.commands.impl.*;
import net.lunark.io.commands.config.*;
import net.lunark.io.language.LanguageManager;
import net.lunark.io.language.PlayerLanguageManager;
import net.lunark.io.language.storage.PlayerLanguageStorage;
import net.lunark.io.listeners.*;
import net.lunark.io.lobby.*;
import net.lunark.io.mail.MailCommand;
import net.lunark.io.mail.MailConfig;
import net.lunark.io.mail.MailListener;
import net.lunark.io.mail.MailStorage;
import net.lunark.io.nick.NickManager;
import net.lunark.io.pw.WarpStorage;
import net.lunark.io.reports.*;
import net.lunark.io.rules.RulesGUI;
import net.lunark.io.rules.RulesListener;
import net.lunark.io.rules.RulesStorage;
import net.lunark.io.scoreboard.CustomScoreboardManager;
import net.lunark.io.scoreboard.util.ScoreboardJoinListener;
import net.lunark.io.server.*;
import net.lunark.io.serverEssentials.ServerEssentialsCommand;
import net.lunark.io.serverEssentials.VersionChecker;
import net.lunark.io.daily.*;
import net.lunark.io.staff.*;
import net.lunark.io.util.*;
import net.lunark.io.utility.EditSignCommand;
import net.lunark.io.utility.FuckCommand;
import net.lunark.io.utility.ToggleFlyCommand;
import net.lunark.io.warp.WarpCommand;
import net.lunark.io.warp.WarpManager;
import net.lunark.io.warp.WarpsCommand;
import net.milkbowl.vault.economy.Economy;
import net.lunark.io.sellgui.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;


public class ServerEssentials extends JavaPlugin implements Listener {

    private static ServerEssentials instance;
    private static final Map<UUID, Double> balances = new HashMap<>();
    private Listener DailyRewardGUI;
    BanManager banManager = new BanManager(this);
    private long startTime;
    private LockdownCommand lockdownCommand;
    private String originalMotd;
    private final HashMap<UUID, UUID> lastMessageMap = new HashMap<>();
    private final HashMap<UUID, BukkitRunnable> jailReleaseTasks = new HashMap<>();
    private Location releaseLocation;
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private File prefixFile;
    private FileConfiguration prefixConfig;
    private File starterMoneyFile;
    private FileConfiguration starterMoneyConfig;
    private PlaytimeManager PlaytimeManager;
    private GUIConfig guiConfig;
    private WarpStorage warpStorage;
    private File pwFile;
    private FileConfiguration pwConfig;
    private BukkitAudiences adventure;
    private CustomScoreboardManager scoreboard;
    private static Economy economy;
    private FileConfiguration offlineConfig;
    private File offlineFile;
    private AuctionManager auctionManager;
    private GUIManager guiManager;
    private Economy vaultEconomy;
    private AuctionMessagesManager auctionMessagesManager;
    private MessagesManager messagesManager;
    private PlayerMessages playerMessages;
    private static final int BSTATS_PLUGIN_ID = 27221;
    private ServerMessages serverMessages;
    private KillTracker killTracker;
    private NotesCommand notesCommand;
    private List<String> reloadedItems = new ArrayList<>();
    private VaultMessages vaultMessages;
    private VaultManager vaultManager;
    private HomeManager homeManager;
    private HomeMessages messages;
    private HomesGUI homesGUI;
    private HomeListener homeListener;
    private WarpManager warpManager;
    private WarpMessages warpMessages;
    private FileConfiguration placeholdersConfig;
    private File placeholdersFile;
    private File nickConfigFile;
    private final Map<String, YamlConfiguration> configs = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private File messagesFile;
    private FirstJoinManager firstJoinManager;
    private YamlConfiguration cfg;
    private NickManager nickManager;
    private File nicksFile;
    private boolean vaultInstalled;
    private TPAConfig tpaConfig;
    private TPAStorage tpaStorage;
    private TPACommand tpaCommand;
    private TPAListener tpaListener;
    private boolean lpInstalled;
    private boolean papiInstalled;

    private DatabaseManager databaseManager;
    private LanguageManager languageManager;
    private PlayerLanguageManager playerLanguageManager;
    private RtpLocationStorage rtpLocationStorage;
    private KitConfig kitConfig;
    private KitStorage kitStorage;
    private KitCommand kitCommand;
    private KitGUIListener kitGuiListener;
    private CommandDataStorage commandDataStorage;
    private FlyCommand flyCommand;
    private FlyListener flyListener;
    private MailConfig mailConfig;
    private MailStorage mailStorage;
    private MailCommand mailCommand;
    private MailListener mailListener;
    private ReportConfig reportConfig;
    private ReportStorage reportStorage;
    private ReportCommand reportCommand;
    private ReportsListCommand reportsListCommand;
    private ReportListener reportListener;
    private EconomyService economyService;
    private AFKManager afkManager;

    private ShopConfig shopConfig;
    private ShopCommand shopCommand;
    private ShopGUIListener shopGuiListener;
    private ShopGUIManager shopGuiManager;
    private ShopStorage shopStorage;

    private SellConfig sellConfig;
    private SellStorage sellStorage;
    private SellGUIManager sellGuiManager;
    private SellCommand sellCommand;
    private SellGUIListener sellGuiListener;

    private DailyConfig dailyConfig;
    private DailyStorage dailyStorage;
    private DailyCommand dailyCommand;
    private DailyListener dailyListener;

    private NearConfig nearConfig;
    private NearCommand nearCommand;
    private RenameConfig renameConfig;
    private RenameItemCommand renameItemCommand;
    private WorldConfig worldConfig;
    private WorldCommand worldCommand;
    private ExtConfig extConfig;
    private ExtCommand extCommand;
    private TeleportWorldConfig tppConfig;
    private TeleportWorldCommand tppCommand;
    private MagnetConfig magnetConfig;
    private MagnetStorage magnetStorage;
    private MagnetCommand magnetCommand;
    private MagnetListener magnetListener;
    private DayConfig dayConfig;
    private DayCommand dayCommand;
    private InventorySortConfig inventorySortConfig;
    private InventorySortCommand inventorySortCommand;
    private RulesConfig rulesConfig;
    private RulesStorage rulesStorage;
    private RulesCommand rulesCommand;
    private RulesListener rulesListener;
    private BookConfig bookConfig;
    private BookCommand bookCommand;
    private BreakConfig breakConfig;
    private BreakCommand breakCommand;
    private SetLoreLineConfig setLoreLineConfig;
    private SetLoreLineCommand setLoreLineCommand;
    private TrackCommand trackCommand;
    private TrackConfig trackConfig;
    private SuicideConfig suicideConfig;
    private SuicideCommand suicideCommand;
    private KillConfig killConfig;
    private KillCommand killCommand;
    private BottomConfig bottomConfig;
    private BottomCommand bottomCommand;
    private CondenseConfig condenseConfig;
    private CondenseCommand condenseCommand;
    private NukeConfig nukeConfig;
    private NukeCommand nukeCommand;
    private WeatherConfig weatherConfig;
    private WeatherCommand weatherCommand;
    private RulesGUI rulesGUI;
    private PlaytimeConfig playtimeConfig;
    private PlaytimeCommand playtimeCommand;
    private RecipeConfig recipeConfig;
    private RecipeCommand recipeCommand;





    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        saveDefaultConfig();

        instance = this; // Set instance early

        // 1. Language Manager
        languageManager = new LanguageManager(this);
        languageManager.loadLanguages();
        languageManager.setDefaultLanguage(getConfig().getString("default_language", "en"));

        // 2. Database Manager (initialize ALL pools here)
        databaseManager = new DatabaseManager(this);
        initializeDatabases(); // MUST include "shop" pool

        // 3. Check for Vault and load economy BEFORE features that need it
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("=== VAULT NOT FOUND ===");
            getLogger().severe("ServerEssentials requires Vault for economy features!");
            getLogger().severe("Please install Vault and an economy plugin (or use ServerEssentials' built-in economy).");
            getLogger().severe("Plugin will continue without economy support.");
            vaultEconomy = null; // Set to null to indicate no economy
        } else {
            getLogger().info("Vault detected. Setting up economy provider...");

            // Register ServerEssentials' economy provider FIRST
            economyService = new EconomyService(this, databaseManager);
            economyService.register();

            // NOW ask Vault for the economy provider (could be ours or another plugin's)
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                getLogger().warning("No economy provider registered! Economy features will be disabled.");
                vaultEconomy = null;
            } else {
                vaultEconomy = rsp.getProvider();
                getLogger().info("Economy provider loaded: " + vaultEconomy.getName());
            }
        }

        // 4. Player Language Manager (depends on databaseManager + languageManager)
        PlayerLanguageStorage langStorage = new PlayerLanguageStorage.YamlStorage(this);
        playerLanguageManager = new PlayerLanguageManager(langStorage, languageManager);

        // 5. Feature Modules (depends on playerLanguageManager + databaseManager + vaultEconomy)
        initializeKitSystem();
        initializeTPASystem();
        initializeCommandModules();
        initializeMailSystem();
        initializeReportSystem();
        initializeShopSystem();
        initializeRtpSystem();
        initializeSellSystem();
        initializeDailySystem();
        initializeRulesSystem();// Only initialize if economy is available




        nearConfig = new NearConfig(this);
        nearCommand = new NearCommand(playerLanguageManager, nearConfig);

        renameConfig = new RenameConfig(this);
        renameItemCommand = new RenameItemCommand(playerLanguageManager, renameConfig);

        worldConfig = new WorldConfig(this);
        worldCommand = new WorldCommand(playerLanguageManager, worldConfig);

        extConfig = new ExtConfig(this);
        extCommand = new ExtCommand(playerLanguageManager, extConfig);

        tppConfig = new TeleportWorldConfig(this);
        tppCommand = new TeleportWorldCommand(playerLanguageManager, tppConfig);

        magnetConfig = new MagnetConfig(this);
        magnetStorage = new MagnetStorage(this, commandDataStorage);
        magnetCommand = new MagnetCommand(playerLanguageManager, magnetConfig, magnetStorage);
        magnetListener = new MagnetListener(this, magnetConfig, magnetStorage);

        dayConfig = new DayConfig(this);
        dayCommand = new DayCommand(playerLanguageManager, dayConfig);

        inventorySortConfig = new InventorySortConfig(this);
        inventorySortCommand = new InventorySortCommand(playerLanguageManager, inventorySortConfig);

        bookConfig = new BookConfig(this);
        bookCommand = new BookCommand(this, playerLanguageManager, bookConfig);

        breakConfig = new BreakConfig(this);
        breakCommand = new BreakCommand(playerLanguageManager, breakConfig, commandDataStorage);

        setLoreLineConfig = new SetLoreLineConfig(this);
        setLoreLineCommand = new SetLoreLineCommand(playerLanguageManager, setLoreLineConfig, commandDataStorage);

        trackConfig = new TrackConfig(this);
        trackCommand = new TrackCommand(playerLanguageManager, trackConfig, commandDataStorage);

        this.suicideConfig = new SuicideConfig(this);
        this.suicideCommand = new SuicideCommand(playerLanguageManager, suicideConfig, commandDataStorage);

        this.killConfig = new KillConfig(this);
        this.killCommand = new KillCommand(playerLanguageManager, killConfig, killTracker, commandDataStorage);

        this.bottomConfig = new BottomConfig(this);
        this.bottomCommand = new BottomCommand(playerLanguageManager, bottomConfig, commandDataStorage);

        this.condenseConfig = new CondenseConfig(this);
        this.condenseCommand = new CondenseCommand(playerLanguageManager, condenseConfig, commandDataStorage);

        this.nukeConfig = new NukeConfig(this);
        this.nukeCommand = new NukeCommand(playerLanguageManager, nukeConfig, commandDataStorage);

        this.weatherConfig = new WeatherConfig(this);
        this.weatherCommand = new WeatherCommand(playerLanguageManager, weatherConfig, commandDataStorage);

        this.playtimeConfig = new PlaytimeConfig(this);
        this.playtimeCommand = new PlaytimeCommand(playerLanguageManager, playtimeConfig, commandDataStorage);

        this.recipeConfig = new RecipeConfig(this);
        this.recipeCommand = new RecipeCommand(playerLanguageManager, recipeConfig, commandDataStorage);






        // Commands
        getCommand("fly").setExecutor(flyCommand);
        getCommand("tpa").setExecutor(tpaCommand);
        getCommand("tpahere").setExecutor(tpaCommand);
        getCommand("tpaccept").setExecutor(tpaCommand);
        getCommand("tpdeny").setExecutor(tpaCommand);
        getCommand("tpacancel").setExecutor(tpaCommand);
        getCommand("tpall").setExecutor(tpaCommand);
        getCommand("tpatoggle").setExecutor(tpaCommand);
        getCommand("tpainfo").setExecutor(tpaCommand);
        getCommand("mail").setExecutor(mailCommand);
        getCommand("report").setExecutor(reportCommand);
        getCommand("report").setTabCompleter(reportCommand);
        getCommand("reportclear").setExecutor(reportCommand);
        getCommand("reportslist").setExecutor(reportsListCommand);
        getCommand("shop").setExecutor(shopCommand);
        getCommand("daily").setExecutor(dailyCommand);
        getCommand("near").setExecutor(nearCommand);
        getCommand("rename").setExecutor(renameItemCommand);
        getCommand("world").setExecutor(worldCommand);
        getCommand("ext").setExecutor(extCommand);
        getCommand("tpp").setExecutor(tppCommand);
        getCommand("magnet").setExecutor(magnetCommand);
        getCommand("day").setExecutor(dayCommand);
        getCommand("inventorysort").setExecutor(inventorySortCommand);
        getCommand("rules").setExecutor(rulesCommand);
        getCommand("break").setExecutor(breakCommand);
        getCommand("setloreline").setExecutor(setLoreLineCommand);
        getCommand("track").setExecutor(trackCommand);
        getCommand("suicide").setExecutor(suicideCommand);
        getCommand("kills").setExecutor(killCommand);
        getCommand("bottom").setExecutor(bottomCommand);
        getCommand("condense").setExecutor(condenseCommand);
        getCommand("nuke").setExecutor(nukeCommand);
        getCommand("weather").setExecutor(weatherCommand);
        getCommand("playtime").setExecutor(playtimeCommand);
        getCommand("recipe").setExecutor(recipeCommand);








        // Tab Completer
        getCommand("tpp").setTabCompleter(tppCommand);
        getCommand("weather").setTabCompleter(weatherCommand);
        getCommand("recipe").setTabCompleter(recipeCommand);







        // 7. Register Listeners
        getServer().getPluginManager().registerEvents(reportListener, this);
        getServer().getPluginManager().registerEvents(tpaListener, this);
        getServer().getPluginManager().registerEvents(mailListener, this);
        getServer().getPluginManager().registerEvents(shopGuiListener, this);
        getServer().getPluginManager().registerEvents(dailyListener, this);
        getServer().getPluginManager().registerEvents(rulesListener, this);


        // CONTINUE WITH REST OF INITIALIZATION (simplified)
        RtpConfig rtpConfig = new RtpConfig(this);
        rtpLocationStorage = new RtpLocationStorage(this, databaseManager, "rtp");
        CooldownManager cooldownManager = new CooldownManager();
        BackManager backManager = new BackManager();

        getCommand("language").setExecutor(new LanguageCommand(languageManager, playerLanguageManager));
        getCommand("database").setExecutor(new DatabaseCommand(databaseManager, languageManager));
        getCommand("rtp").setExecutor(new RtpCommand(this, playerLanguageManager, rtpConfig));
        getCommand("kit").setExecutor(kitCommand);

        getServer().getPluginManager().registerEvents(
                new RtpListener(this, playerLanguageManager, rtpLocationStorage, cooldownManager, backManager, rtpConfig), this);

        // ... rest of your initialization code (simplified for brevity)
        homeManager = new HomeManager(this);
        warpManager = new WarpManager(this);

        // ... all other commands and listeners ...

        long elapsed = System.currentTimeMillis() - start;
        BannerUtil.printBanner(elapsed);

        saveDefaultPlaceholders();
        afkManager = new AFKManager(this);

        nickConfigFile = new File(getDataFolder(), "config/nick/nick.yml");
        messagesFile = new File(getDataFolder(), "messages/player.yml");
        nicksFile = new File(getDataFolder(), "storage/nicks.yml");
        saveDefaultFile(nickConfigFile, "/config/nick/nick.yml");
        saveDefaultFile(messagesFile, "/messages/player.yml");
        saveDefaultFile(nicksFile, null);

        loadPlaceholders();


        HashMap<UUID, UUID> lastMessageMap = new HashMap<>();

        if (!isCommandDisabled("warp")) getCommand("warp").setExecutor(new WarpCommand(this));
        if (!isCommandDisabled("setwarp")) getCommand("setwarp").setExecutor(new WarpCommand(this));
        if (!isCommandDisabled("delwarp")) getCommand("delwarp").setExecutor(new WarpCommand(this));

        try {
            Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.adventure = BukkitAudiences.create(this);
        scoreboard = new CustomScoreboardManager(this);

        File messagesDir = new File(getDataFolder(), "messages");
        if (!messagesDir.exists()) messagesDir.mkdirs();

        this.playerMessages = new PlayerMessages(this);
        this.messagesManager = new MessagesManager(this);
        serverMessages = new ServerMessages(this, "messages/server.yml");
        messagesManager.load("fun.yml");

        FunMessages funMessages = new FunMessages(this, "messages/fun.yml");

        createPrefixFile();
        String prefix = ChatColor.translateAlternateColorCodes('&',
                prefixConfig.getString("prefix", "&9&l[&bSE&9&l]&r ")
        );

        LobbyManager.setup();
        loadOfflineConfig();
        LobbyManager.setup();

        messagesManager = new MessagesManager(this);
        MessagesManager messagesManager = new MessagesManager(this);
        messagesManager.load("fun.yml");

        auctionManager = new AuctionManager(this);
        guiManager = new GUIManager(this);

        File messagesFile = new File(getDataFolder(), "messages/scoreboard_system.yml");
        if (!messagesFile.exists()) saveResource("messages/scoreboard_system.yml", false);

        reloadPluginConfig();



        saveDefaultConfig();
        saveResource("config.yml", false);

        JoinLeaveManager.load();
        saveResource("messages/lobby.yml", false);
        LobbyMessages.setup();
        LobbyStorage.setup();
        LobbyConfig.setup();

        messagesManager.load("player.yml");

        this.startTime = System.currentTimeMillis();
        originalMotd = Bukkit.getMotd();

        CustomScoreboardManager manager = new CustomScoreboardManager(this);
        String originalMotd = Bukkit.getMotd();

        LockdownCommand lockdownCommand = new LockdownCommand(this, messagesManager, originalMotd);
        PluginCommand cmd = getCommand("lockdown");

        new TopPlaytimePlaceholder(this).register();
        this.PlaytimeManager = new PlaytimeManager(this);

        this.serverMessages = new ServerMessages(this, "messages/server.yml");



        this.guiConfig = new GUIConfig(getConfig());
        this.warpStorage = new WarpStorage(this, guiConfig);
        warpStorage.loadAll();

        auctionMessagesManager = new AuctionMessagesManager(this);
        auctionMessagesManager.loadMessages(); // load auction.yml

        playerMessages = new PlayerMessages(this);
        KillTracker killTracker = new KillTracker(this);
        ConsoleCommandManager commandManager = new ConsoleCommandManager(this);

        releaseLocation = new Location(Bukkit.getWorld("world"), 0, 65, 0);
        getServer().getPluginManager().registerEvents(new JoinCommandListener(commandManager), this);



        FileConfiguration pwConfig = getConfig();
        if (!pwConfig.contains("gui")) saveConfig();

        if (!isCommandDisabled("se")) this.getCommand("se").setExecutor(new ServerEssentialsCommand(this));

        VersionChecker.checkLatestVersion(this);
        Bukkit.getPluginManager().registerEvents(new VersionNotifyJoinListener(), this);




        nickManager = new NickManager(getDataFolder());
        if (!isCommandDisabled("nick")) getCommand("nick").setExecutor(nickManager);
        if (!isCommandDisabled("nicks")) getCommand("nicks").setExecutor(nickManager);


        getServer().getPluginManager().registerEvents(new AdminChatListener(), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);
        getLogger().info("ServerEssentials enabled (dashboard command registered).");

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(this, this); // onJoin listener
        getServer().getPluginManager().registerEvents(new BanListener(banManager), this);
        Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

        ServerEssentials plugin = ServerEssentials.getInstance();
        AdminUtilitiesCommand adminUtils = new AdminUtilitiesCommand();
        getServer().getPluginManager().registerEvents(new AdminUtilitiesListener(adminUtils), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ServerEssentialsPlaceholder(this).register();
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders won't work.");
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent e) {}
        }, this);





        if (!isCommandDisabled("balance")) this.getCommand("balance").setExecutor(new BalanceCommand(vaultEconomy));
        if (!isCommandDisabled("balancetop")) this.getCommand("balancetop").setExecutor(new BalanceTopCommand(this, vaultEconomy));
        if (!isCommandDisabled("eco")) this.getCommand("eco").setExecutor(new EcoCommand(vaultEconomy));
        if (!isCommandDisabled("pay")) this.getCommand("pay").setExecutor(new PayCommand(vaultEconomy));
        if (!isCommandDisabled("coinflip")) this.getCommand("coinflip").setExecutor(new CoinFlipCommand(vaultEconomy));


        if (!isCommandDisabled("glow")) getCommand("glow").setExecutor(new GlowCommand(funMessages));
        if (!isCommandDisabled("ban")) getCommand("ban").setExecutor(new BanCommand(getBanManager(), this));
        if (!isCommandDisabled("unban")) getCommand("unban").setExecutor(new UnbanCommand(this, banManager));
        if (!isCommandDisabled("powertool")) getCommand("powertool").setExecutor(new PowerToolCommand(this, funMessages));
        if (!isCommandDisabled("editsign")) this.getCommand("editsign").setExecutor(new EditSignCommand(this));
        if (!isCommandDisabled("auction")) getCommand("auction").setExecutor(new AuctionCommand(this));

        File shopFolder = new File(getDataFolder(), "shop");
        EconomyMessagesManager.setup(this);
        EconomyMessagesManager economymessages = new EconomyMessagesManager();
        getServer().getPluginManager().registerEvents(new AuctionListeners(this), this);

        if (!isCommandDisabled("spawner")) getCommand("spawner").setExecutor(new SpawnerCommand(serverMessages));
        if (!isCommandDisabled("paytoggle")) getCommand("paytoggle").setExecutor(new PayToggleCommand());
        if (!isCommandDisabled("payconfirmtoggle")) getCommand("payconfirmtoggle").setExecutor(new PayConfirmToggleCommand());



        if (!isCommandDisabled("whois")) getCommand("whois").setExecutor(new WhoIsCommand());
        if (!isCommandDisabled("seen")) getCommand("seen").setExecutor(new SeenCommand(this));

        KittyCannonCommand kittyCannonCommand = new KittyCannonCommand(funMessages);
        if (!isCommandDisabled("kittycannon")) {
            getCommand("kittycannon").setExecutor(kittyCannonCommand);
            getCommand("kittycannon").setTabCompleter(kittyCannonCommand);
        }

        BeezookaCommand beezookaCommand = new BeezookaCommand(funMessages);
        if (!isCommandDisabled("beezooka")) {
            getCommand("beezooka").setExecutor(beezookaCommand);
            getCommand("beezooka").setTabCompleter(beezookaCommand);
        }



        if (!isCommandDisabled("togglefly")) getCommand("togglefly").setExecutor(new ToggleFlyCommand());




        new FirstJoinManager(this);

        FireballCommand fireballCommand = new FireballCommand(funMessages);
        if (!isCommandDisabled("fireball")) getCommand("fireball").setExecutor(fireballCommand);

        if (!isCommandDisabled("lightning")) getCommand("lightning").setExecutor(new LightningCommand(funMessages));

        ThunderCommand thunderCommand = new ThunderCommand(funMessages);
        if (!isCommandDisabled("thunder")) {
            getCommand("thunder").setExecutor(thunderCommand);
            getCommand("thunder").setTabCompleter(thunderCommand);
        }

        if (!isCommandDisabled("broadcastworld")) getCommand("broadcastworld").setExecutor(new BroadcastWorldCommand(serverMessages));



        if (!isCommandDisabled("stonecutter")) getCommand("stonecutter").setExecutor(new StonecutterCommand(this));
        if (!isCommandDisabled("loom")) getCommand("loom").setExecutor(new LoomCommand(this));
        if (!isCommandDisabled("cartographytable")) getCommand("cartographytable").setExecutor(new CartographyTableCommand(this));
        if (!isCommandDisabled("smithingtable")) getCommand("smithingtable").setExecutor(new SmithingTableCommand(this));
        if (!isCommandDisabled("brewingstand")) getCommand("brewingstand").setExecutor(new BrewingStandCommand(this));
        if (!isCommandDisabled("craftingtable")) getCommand("craftingtable").setExecutor(new CraftingTableCommand(this));
        if (!isCommandDisabled("anvil")) getCommand("anvil").setExecutor(new AnvilCommand(this));
        if (!isCommandDisabled("furnace")) getCommand("furnace").setExecutor(new FurnaceCommand(this));
        if (!isCommandDisabled("blastfurnace")) getCommand("blastfurnace").setExecutor(new BlastFurnaceCommand(this));
        if (!isCommandDisabled("smoker")) getCommand("smoker").setExecutor(new SmokerCommand(this));
        if (!isCommandDisabled("grindstone")) getCommand("grindstone").setExecutor(new GrindstoneCommand(this));

        if (!isCommandDisabled("fuck")) this.getCommand("fuck").setExecutor(new FuckCommand());

        saveResource("messages/homes.yml", false);
        this.messages = new HomeMessages(this, "messages/homes.yml");
        this.homesGUI = new HomesGUI(this, homeManager, messages);
        this.homeListener = new HomeListener(this, homeManager, homesGUI, messages);
        Bukkit.getPluginManager().registerEvents(homeListener, this);

        if (!isCommandDisabled("unloadworld")) getCommand("unloadworld").setExecutor(new UnloadWorldCommand(serverMessages));
        if (!isCommandDisabled("loadworld")) getCommand("loadworld").setExecutor(new LoadWorldCommand(serverMessages));
        if (!isCommandDisabled("worldlist")) getCommand("worldlist").setExecutor(new WorldListCommand(serverMessages));

        vaultMessages = new VaultMessages(this);
        vaultManager = new VaultManager(this, vaultMessages);
        if (!isCommandDisabled("pv")) this.getCommand("pv").setExecutor(new VaultCommand(vaultManager, vaultMessages));

        getServer().getPluginManager().registerEvents(lockdownCommand, this);

        long serverStartTime = System.currentTimeMillis();
        if (!isCommandDisabled("uptime")) getCommand("uptime").setExecutor(new UptimeCommand(serverStartTime, serverMessages));



        if (!isCommandDisabled("tree")) getCommand("tree").setExecutor(new TreeCommand(playerMessages));

        CanonCommand canonCommand = new CanonCommand(funMessages);
        if (!isCommandDisabled("canon") && getCommand("canon") != null) getCommand("canon").setExecutor(canonCommand);

        ExplosionCommand explosionCommand = new ExplosionCommand(funMessages);
        if (!isCommandDisabled("explosion")) {
            getCommand("explosion").setExecutor(explosionCommand);
            getCommand("explosion").setTabCompleter(explosionCommand);
        }

        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }



        killTracker = new KillTracker(this);

        SessionManager sessionManager = new SessionManager(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(sessionManager), this);
        if (!isCommandDisabled("session")) getCommand("session").setExecutor(new SessionCommand(playerMessages, sessionManager));

        if (!isCommandDisabled("vanish")) getCommand("vanish").setExecutor(adminUtils);
        if (!isCommandDisabled("god")) getCommand("god").setExecutor(adminUtils);
        if (!isCommandDisabled("invsee")) getCommand("invsee").setExecutor(adminUtils);



        if (!isCommandDisabled("sereload")) getCommand("sereload").setExecutor(new ReloadCommand(this));



        if (!isCommandDisabled("invclear")) getCommand("invclear").setExecutor(adminUtils);
        if (!isCommandDisabled("tp")) getCommand("tp").setExecutor(adminUtils);



        if (!isCommandDisabled("adminchat")) getCommand("adminchat").setExecutor(new AdminChatCommand());
        if (!isCommandDisabled("broadcast")) getCommand("broadcast").setExecutor(new BroadcastCommand(serverMessages));
        if (!isCommandDisabled("ping")) this.getCommand("ping").setExecutor(new PingCommand(this));

        Bukkit.getPluginManager().registerEvents(new LobbyListener(), this);
        if (!isCommandDisabled("lobby")) getCommand("lobby").setExecutor(new LobbyCommand());

        BackCommand backCommand = new BackCommand(playerMessages);
        if (!isCommandDisabled("back")) {
            getCommand("back").setExecutor(backCommand);
            getCommand("back").setTabCompleter(backCommand);
        }
        getServer().getPluginManager().registerEvents(new BackListener(), this);

        if (!isCommandDisabled("death")) getCommand("death").setExecutor(new DeathCommand());
        if (!isCommandDisabled("motd")) getCommand("motd").setExecutor(new MotdCommand(this));
        if (!isCommandDisabled("heal")) getCommand("heal").setExecutor(new HealCommand(this));
        if (!isCommandDisabled("feed")) getCommand("feed").setExecutor(new FeedCommand());

        PTimeCommand pTimeCommand = new PTimeCommand(this);
        if (!isCommandDisabled("ptime")) {
            this.getCommand("ptime").setExecutor(pTimeCommand);
            this.getCommand("ptime").setTabCompleter(pTimeCommand);
        }
        if (!isCommandDisabled("playerinfo")) getCommand("playerinfo").setExecutor(new PlayerInfoCommand(this));
        if (!isCommandDisabled("launch")) this.getCommand("launch").setExecutor(new LaunchCommand());
        if (!isCommandDisabled("gravity")) getCommand("gravity").setExecutor(new GravityCommand(funMessages));
        if (!isCommandDisabled("repair")) this.getCommand("repair").setExecutor(new RepairCommand(this));
        if (!isCommandDisabled("workbench")) getCommand("workbench").setExecutor(new WorkbenchCommand());
        if (!isCommandDisabled("enderchest")) getCommand("enderchest").setExecutor(new EnderChestCommand());



        EnderSeeCommand enderSeeCommand = new EnderSeeCommand(this, messagesManager);
        if (!isCommandDisabled("endersee")) {
            getCommand("endersee").setExecutor(enderSeeCommand);
            getCommand("endersee").setTabCompleter(enderSeeCommand);
        }

        if (!isCommandDisabled("mute")) getCommand("mute").setExecutor(new MuteCommand(this));
        if (!isCommandDisabled("unmute")) getCommand("unmute").setExecutor(new UnmuteCommand(this));
        if (!isCommandDisabled("night")) getCommand("night").setExecutor(new NightCommand(this));






        if (!isCommandDisabled("kickall")) getCommand("kickall").setExecutor(new KickAllCommand());
        if (!isCommandDisabled("alts")) getCommand("alts").setExecutor(new AltsCommand(this));
        if (!isCommandDisabled("reboot")) getCommand("reboot").setExecutor(new RebootCommand(this, serverMessages));
        if (!isCommandDisabled("disposal")) getCommand("disposal").setExecutor(new DisposalCommand());
        if (!isCommandDisabled("top")) getCommand("top").setExecutor(new TopCommand(this));
        if (!isCommandDisabled("clearchat")) getCommand("clearchat").setExecutor(new ClearChatCommand(this));
        if (!isCommandDisabled("tpoffline")) getCommand("tpoffline").setExecutor(new TPOfflineCommand(this));
        if (!isCommandDisabled("speed")) getCommand("speed").setExecutor(new SpeedCommand(this));
        if (!isCommandDisabled("swap")) getCommand("swap").setExecutor(new SwapCommand(this, funMessages));
        if (!isCommandDisabled("sleep")) getCommand("sleep").setExecutor(new SleepCommand(this));

        playerMessages = new PlayerMessages(this);
        notesCommand = new NotesCommand(playerMessages, this);
        if (!isCommandDisabled("notes")) {
            getCommand("notes").setExecutor(notesCommand);
            getCommand("notes").setTabCompleter(notesCommand);
        }
        Bukkit.getPluginManager().registerEvents(this, this);


        if (!isCommandDisabled("fakeop")) getCommand("fakeop").setExecutor(new FaekOPCommand());
        if (!isCommandDisabled("hat")) getCommand("hat").setExecutor(new HatCommand());
        if (!isCommandDisabled("realname")) getCommand("realname").setExecutor(new RealNameCommand(this));
        if (!isCommandDisabled("freeze")) getCommand("freeze").setExecutor(new FreezeCommand(getMessagesManager(), this));
        if (!isCommandDisabled("unfreeze")) getCommand("unfreeze").setExecutor(new UnfreezeCommand(new MessagesManager(this)));

        getServer().getPluginManager().registerEvents(new ToolMoveFixListener(this), this);

        if (!isCommandDisabled("iteminfo")) getCommand("iteminfo").setExecutor(new ItemInfoCommand(playerMessages));
        if (!isCommandDisabled("pingall")) getCommand("pingall").setExecutor(new PingAllCommand(this));
        if (!isCommandDisabled("warps")) getCommand("warps").setExecutor(new WarpsCommand(this));
        if (!isCommandDisabled("compass")) getCommand("compass").setExecutor(new CompassCommand());
        if (!isCommandDisabled("stafflist")) getCommand("stafflist").setExecutor(new StaffListCommand(this));
        if (!isCommandDisabled("burn")) getCommand("burn").setExecutor(new BurnCommand(playerMessages));
        if (!isCommandDisabled("celebrate")) getCommand("celebrate").setExecutor(new CelebrateCommand(funMessages));
        if (!isCommandDisabled("serverinfo")) getCommand("serverinfo").setExecutor(new ServerInfoCommand(serverMessages));
        if (!isCommandDisabled("banlist")) getCommand("banlist").setExecutor(new BanListCommand(this, banManager));

        disableCommands();


        Bukkit.getPluginManager().registerEvents(new MotdListener(this), this);



        getServer().getPluginManager().registerEvents(new ScoreboardJoinListener(manager), this);

        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            }
            @org.bukkit.event.EventHandler
            public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
            }
        }, this);

    }
    public void setReloadedItems(List<String> items) {
        this.reloadedItems = items;
    }
    public List<String> getReloadedItems() {
        return reloadedItems;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (command.getName().equalsIgnoreCase("homes")) {
            homesGUI.openMain(p);
            return true;
        }
        return false;
    }
    private void loadOfflineConfig() {
        offlineFile = new File(getDataFolder(), "storage/offline_players.yml");
        if (!offlineFile.getParentFile().exists()) offlineFile.getParentFile().mkdirs();

        if (!offlineFile.exists()) {
            try {
                offlineFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        offlineConfig = YamlConfiguration.loadConfiguration(offlineFile);
    }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (MuteCommand.isMuted(uuid)) {
            event.setCancelled(true);
            return;
        }
    }
    private void saveSectionResource(String path) {
        File outFile = new File(getDataFolder(), path);
        if (!outFile.exists()) {
            saveResource(path, false);
        }
    }
    public AuctionMessagesManager getAuctionMessagesManager() {
        return auctionMessagesManager;
    }
    public WarpManager getWarpManager() {
        return warpManager;
    }
    public WarpMessages getWarpMessages() {
        return warpMessages;
    }
    public static ServerEssentials getInstance() {
        return instance;
    }
    public CustomScoreboardManager getScoreboardManager() {
        return scoreboard;
    }
    public BanManager getBanManager() {
        return banManager;
    }
    public void onServerListPing(ServerListPingEvent event) {

        if (lockdownCommand.isLockdownActive()) {
            event.setMotd("§4§lLOCKDOWN MODE");
        } else {
            event.setMotd(originalMotd);
        }
    }
    private void createPrefixFile() {
        File storageFolder = new File(getDataFolder(), "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
        prefixFile = new File(storageFolder, "storage/prefix.yml");

        if (!prefixFile.exists()) {
            saveResource("storage/prefix.yml", false);
        }
        prefixConfig = YamlConfiguration.loadConfiguration(prefixFile);
    }
    public VaultMessages getVaultMessages() {
        return vaultMessages;
    }
    private void loadPrefixConfig() {
        File storageFolder = new File(getDataFolder(), "storage");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        prefixFile = new File(storageFolder, "storage/prefix.yml");

        if (!prefixFile.exists()) {
            saveResource("storage/prefix.yml", false);
        }

        prefixConfig = YamlConfiguration.loadConfiguration(prefixFile);
    }
    public static String getPrefix() {
        String rawPrefix = getInstance().getPrefixConfig().getString("prefix", "&9&l[&bSE&9&l] &r");
        return formatColors(rawPrefix);
    }
    public void saveOfflineConfig() {
        try {
            offlineConfig.save(offlineFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public ServerMessages getMessages() {
        return serverMessages;
    }
    public void setLastLocation(UUID uuid, Location loc) {
        if (loc == null) return;
        offlineConfig.set(uuid.toString() + ".world", loc.getWorld().getName());
        offlineConfig.set(uuid.toString() + ".x", loc.getX());
        offlineConfig.set(uuid.toString() + ".y", loc.getY());
        offlineConfig.set(uuid.toString() + ".z", loc.getZ());
        offlineConfig.set(uuid.toString() + ".yaw", loc.getYaw());
        offlineConfig.set(uuid.toString() + ".pitch", loc.getPitch());
        saveOfflineConfig();
    }
    public Location getLastLocation(UUID uuid) {
        if (!offlineConfig.contains(uuid.toString())) return null;

        String world = offlineConfig.getString(uuid + ".world");
        double x = offlineConfig.getDouble(uuid + ".x");
        double y = offlineConfig.getDouble(uuid + ".y");
        double z = offlineConfig.getDouble(uuid + ".z");
        float yaw = (float) offlineConfig.getDouble(uuid + ".yaw");
        float pitch = (float) offlineConfig.getDouble(uuid + ".pitch");

        if (Bukkit.getWorld(world) == null) return null;
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        setLastLocation(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
    }
    public static String formatColors(String message) {
        if (message == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
    public ServerEssentials() { instance = this; }
    public AuctionManager getAuctionManager() { return auctionManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public void onDisable() {
        if (warpStorage != null) {
            warpStorage.saveAll();
        }
        if (this.adventure != null) {
            this.adventure.close();
        }
        if (notesCommand != null) {
            notesCommand.saveNotes();
        }
        if (homeManager != null) {
            homeManager.saveAll();
        }
        if (databaseManager != null) {
            databaseManager.closeAll();
        }
        if (magnetListener != null) {
            magnetListener.stop();
        }

        NickManager.reload();
        getLogger().info("Shutting down ServerEssentials! Bye :(");
    }
    private void saveDefaultFile(File file, String resourcePath) {
        if (!file.exists()) {
            try {
                if (resourcePath != null) {
                    file.getParentFile().mkdirs();
                    saveResource(resourcePath.substring(1), false);
                } else {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public HomeMessages getHomeMessages() {
        return messages;
    }
    public HomeManager getHomeManager() {
        return homeManager;
    }
    public HomesGUI getHomesGUI() {
        return homesGUI;
    }
    public BukkitAudiences adventure() {
        return this.adventure;
    }
    public void reloadPluginConfig() {
        reloadConfig();
        FileConfiguration bukkitConfig = getConfig();
        if (guiConfig == null) {
            guiConfig = new GUIConfig(bukkitConfig);
        } else {
            guiConfig.reload(bukkitConfig);
        }
    }
    public WarpStorage getWarpStorage() {
        return warpStorage;
    }
    public static class VersionNotifyJoinListener implements Listener {

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            VersionChecker.notifyIfOutdated(event.getPlayer());
        }
    }
    public FileConfiguration getStarterMoneyConfig() {
        return starterMoneyConfig;
    }
    public FileConfiguration getPrefixConfig() {
        return prefixConfig;
    }
    public PlaytimeManager getPlaytimeManager() {
        return PlaytimeManager;
    }
    public void loadPWConfig() {
        pwFile = new File(getDataFolder(), "pw.yml");
        if (!pwFile.exists()) {
            saveResource("pw.yml", false);
        }
        pwConfig = YamlConfiguration.loadConfiguration(pwFile);
    }
    public static class PWTabCompleter implements TabCompleter {

        private final WarpStorage storage;

        public PWTabCompleter(WarpStorage storage) {
            this.storage = storage;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (!(sender instanceof Player player)) {
                return List.of();
            }
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                List<String> subcommands = List.of("create", "edit", "reload");
                for (String sub : subcommands) {
                    if (sub.startsWith(args[0].toLowerCase())) {
                        completions.add(sub);
                    }
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("edit")) {
                    List<String> warps = storage.getWarps(player.getUniqueId()).stream()
                            .map(warp -> warp.getName())
                            .toList();
                    for (String warpName : warps) {
                        if (warpName.toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(warpName);
                        }
                    }
                }
            }

            return completions;
        }
    }

    public ServerMessages getServerMessages() {
        return serverMessages;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
    private void saveDefaultPlaceholders() {
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        placeholdersFile = new File(getDataFolder(), "placeholders.yml");

        if (!placeholdersFile.exists()) {
            saveResource("placeholders.yml", false);
        }
    }
    private void loadPlaceholders() {
        placeholdersFile = new File(getDataFolder(), "placeholders.yml");
        placeholdersConfig = YamlConfiguration.loadConfiguration(placeholdersFile);
    }
    public FileConfiguration getPlaceholdersConfig() {
        return placeholdersConfig;
    }
    public void savePlaceholdersConfig() {
        try {
            placeholdersConfig.save(placeholdersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public KillTracker getKillTracker() {
        return killTracker;
    }
    public Economy getVaultEconomy() {
        return vaultEconomy;
    }
    public MessagesManager getMessagesManager() {
        return messagesManager;
    }
    public static Economy getEconomy() {
        return economy;
    }


    public PlayerMessages getPlayerMessages() {
        return playerMessages;
    }

    private boolean isCommandDisabled(String commandName) {
        List<String> disabled = getConfig().getStringList("disabled-commands");
        return disabled.contains(commandName.toLowerCase());
    }

    private void disableCommands() {
        List<String> disabled = getConfig().getStringList("disabled-commands");

        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            if (commandMap instanceof SimpleCommandMap) {
                SimpleCommandMap simpleCommandMap = (SimpleCommandMap) commandMap;
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                java.util.Map<String, Command> knownCommands = (java.util.Map<String, Command>) knownCommandsField.get(simpleCommandMap);

                for (String cmdName : disabled) {
                    Command cmd = knownCommands.get(cmdName);

                    if (cmd != null) {
                        // Remove the main command
                        knownCommands.remove(cmdName);

                        // Remove the namespaced version
                        knownCommands.remove(getNameSpaced(cmdName));

                        // Remove all aliases
                        for (String alias : cmd.getAliases()) {
                            knownCommands.remove(alias);
                            knownCommands.remove(getNameSpaced(alias));
                        }

                        getLogger().info("Completely removed command: /" + cmdName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeReportSystem() {
        reportConfig = new ReportConfig(this);
        reportStorage = new ReportStorage(this, databaseManager);
        reportCommand = new ReportCommand(this, playerLanguageManager, reportStorage, reportConfig);
        reportsListCommand = new ReportsListCommand(this, playerLanguageManager, reportStorage);
        reportListener = new ReportListener(reportCommand);
    }







    private void initializeDatabases() {
        // RTP DB
        if (getConfig().contains("databases.rtp")) {
            DatabaseConfig rtpDbConfig = loadDatabaseConfig("rtp");
            databaseManager.initializePool("rtp", rtpDbConfig);
        }

        // Player Data DB
        if (getConfig().contains("databases.playerdata")) {
            DatabaseConfig playerDbConfig = loadDatabaseConfig("playerdata");
            databaseManager.initializePool("playerdata", playerDbConfig);
        }

        // Kits DB (MISSING BEFORE — FIXED)
        if (getConfig().contains("databases.kits")) {
            DatabaseConfig kitsDbConfig = loadDatabaseConfig("kits");
            databaseManager.initializePool("kits", kitsDbConfig);
        } else {
            databaseManager.initializePool(
                    "kits",
                    new DatabaseConfig(DatabaseType.SQLITE, "kits.db", null, 0, null, null, null, 5)
            );
        }
        databaseManager.initializePool("rules", new DatabaseConfig(DatabaseType.SQLITE, "rules.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("mail", new DatabaseConfig(DatabaseType.SQLITE, "mail.db", null,0,null,null,null,15));

        databaseManager.initializePool("sellgui",
                new DatabaseConfig(DatabaseType.SQLITE, "sellgui.db", null, 0, null, null, null, 5));


        databaseManager.initializePool("economy", new DatabaseConfig(
                DatabaseType.SQLITE, "economy.db", null, 0, null, null, null, 10
        ));
        databaseManager.initializePool("daily", new DatabaseConfig(DatabaseType.SQLITE, "daily.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("reports", new DatabaseConfig(DatabaseType.SQLITE, "reports.db", null,0,null,null,null,15));
        databaseManager.initializePool("shop", new DatabaseConfig(DatabaseType.SQLITE, "shop.db", null, 0, null, null, null, 5));        databaseManager.initializePool("command_data", new DatabaseConfig(DatabaseType.SQLITE, "command_data.db", null, 0, null, null, null, 10));
        // TPA DB
        databaseManager.initializePool("tpa",
                new DatabaseConfig(DatabaseType.SQLITE, "tpa.db", null, 0, null, null, null, 5));
    }
    private void initializeShopSystem() {
        shopConfig = new ShopConfig(this);
        shopStorage = new ShopStorage(databaseManager); // Create storage once
        shopGuiManager = new ShopGUIManager(this, playerLanguageManager, shopStorage, shopConfig, vaultEconomy);
        shopCommand = new ShopCommand(this, playerLanguageManager, databaseManager, shopConfig, vaultEconomy);
        shopGuiListener = new ShopGUIListener(shopGuiManager);
    }
    private void initializeRulesSystem() {
        // Initialize config, storage, and GUI first
        this.rulesConfig = new RulesConfig(this);
        this.rulesStorage = new RulesStorage(this, databaseManager);
        this.rulesGUI = new RulesGUI(playerLanguageManager, rulesStorage, rulesConfig, this);

        // Initialize command and listener (depend on GUI)
        this.rulesListener = new RulesListener(playerLanguageManager, rulesStorage, rulesConfig, this);
        this.rulesCommand = new RulesCommand(playerLanguageManager, rulesConfig, rulesStorage, this);

        // Register with Bukkit
        getCommand("rules").setExecutor(rulesCommand);
        getServer().getPluginManager().registerEvents(rulesListener, this);
    }

    private void initializeDailySystem() {
        dailyConfig = new DailyConfig(this);
        dailyStorage = new DailyStorage(this, databaseManager);
        dailyListener = new DailyListener(this, playerLanguageManager, dailyStorage, dailyConfig);
        dailyCommand = new DailyCommand(this, playerLanguageManager, dailyStorage, dailyConfig, dailyListener);
    }



    private void initializeSellSystem() {
        if (vaultEconomy == null) {
            getLogger().warning("Economy not available - Sell GUI disabled!");
            return;
        }

        sellConfig = new SellConfig(this);
        sellStorage = new SellStorage(this, databaseManager);
        sellGuiManager = new SellGUIManager(this, playerLanguageManager, sellStorage, sellConfig, vaultEconomy);
        sellCommand = new SellCommand(playerLanguageManager, sellGuiManager);
        sellGuiListener = new SellGUIListener(sellGuiManager);

        getCommand("sellgui").setExecutor(sellCommand);
        getServer().getPluginManager().registerEvents(sellGuiListener, this);

    }





    private void initializeMailSystem() {
        mailConfig = new MailConfig(this);
        mailStorage = new MailStorage(this, databaseManager);
        mailCommand = new MailCommand(this, playerLanguageManager, mailStorage, mailConfig);
        mailListener = new MailListener(mailStorage, playerLanguageManager, mailConfig, this);
    }
    private void diagnoseMySQL(DatabaseConfig config) {
        Bukkit.getLogger().warning("MySQL Connection Failed!");
        Bukkit.getLogger().warning("=== TROUBLESHOOTING CHECKLIST ===");
        Bukkit.getLogger().warning("1. Is MySQL server running? Check: sudo systemctl status mysql");
        Bukkit.getLogger().warning("2. Host/Port correct? Config shows: " + config.mysqlHost() + ":" + config.mysqlPort());
        Bukkit.getLogger().warning("3. Firewall blocking? Try: telnet " + config.mysqlHost() + " " + config.mysqlPort());
        Bukkit.getLogger().warning("4. User has remote access? Run: GRANT ALL ON " + config.mysqlDatabase() + ".* TO '" + config.mysqlUser() + "'@'%';");
        Bukkit.getLogger().warning("5. Database exists? Run: CREATE DATABASE IF NOT EXISTS " + config.mysqlDatabase() + ";");
        Bukkit.getLogger().warning("6. Check my.cnf for: bind-address = 0.0.0.0");
        Bukkit.getLogger().warning("7. Test manually: mysql -u " + config.mysqlUser() + " -p -h " + config.mysqlHost());
    }



    private void initializeTPASystem() {
        tpaConfig = new TPAConfig(this);
        tpaStorage = new TPAStorage(this, databaseManager);
        tpaListener = new TPAListener(tpaConfig, playerLanguageManager);
        tpaCommand = new TPACommand(this, playerLanguageManager, tpaStorage, tpaConfig, tpaListener);
    }

    private DatabaseConfig loadDatabaseConfig(String key) {
        String type = getConfig().getString("databases." + key + ".type", "sqlite").toUpperCase();
        return new DatabaseConfig(
                DatabaseType.valueOf(type),
                getConfig().getString("databases." + key + ".file", key + ".db"),
                getConfig().getString("databases." + key + ".host", "localhost"),
                getConfig().getInt("databases." + key + ".port", 3306),
                getConfig().getString("databases." + key + ".database", "minecraft"),
                getConfig().getString("databases." + key + ".username", "root"),
                getConfig().getString("databases." + key + ".password", ""),
                getConfig().getInt("databases." + key + ".pool_size", 5)
        );
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    // Helper method to get namespaced version
    private String getNameSpaced(String command) {
        return getName().toLowerCase() + ":" + command.toLowerCase();
    }




    private void initializeKitSystem() {
        // Setup config
        kitConfig = new KitConfig(this);

        // Setup storage
        kitStorage = new KitStorage(this, databaseManager, "kits");

        // CREATE GUI listener FIRST
        kitGuiListener = new KitGUIListener(this, playerLanguageManager, kitStorage, kitConfig);

        // Setup command
        kitCommand = new KitCommand(this, playerLanguageManager, kitStorage, kitConfig, kitGuiListener);
        getCommand("kit").setExecutor(kitCommand);

        // Register GUI listener
        getServer().getPluginManager().registerEvents(kitGuiListener, this);

        // Load kits
        KitConfigManager.setup(this);
        KitManager.loadKits(KitConfigManager.getConfig());

        getLogger().info("Kit system initialized with " + KitManager.getKits().size() + " kits");
    }


    public KitConfig getKitConfig() { return kitConfig; }
    public KitStorage getKitStorage() { return kitStorage; }

    private void initializeCommandModules() {
        commandDataStorage = new CommandDataStorage(this, databaseManager);

        // Initialize fly command
        flyCommand = new FlyCommand(this, playerLanguageManager, commandDataStorage);
        flyListener = new FlyListener(flyCommand);
    }


    private void initializeRtpSystem() {
        RtpConfig rtpConfig = new RtpConfig(this);
        rtpLocationStorage = new RtpLocationStorage(this, databaseManager, "rtp");
        CooldownManager cooldownManager = new CooldownManager();
        BackManager backManager = new BackManager();

        getCommand("rtp").setExecutor(new RtpCommand(this, playerLanguageManager, rtpConfig));
        getServer().getPluginManager().registerEvents(
                new RtpListener(this, playerLanguageManager, rtpLocationStorage, cooldownManager, backManager, rtpConfig), this);
    }



    private final String[] defaultShopFiles = {
            "food.yml",
            "main.yml"
    };

    public void saveDefaultShopFiles() {
        File shopDir = new File(getDataFolder(), "shop");
        if (!shopDir.exists()) shopDir.mkdirs();

        for (String fileName : defaultShopFiles) {
            File out = new File(shopDir, fileName);
            if (!out.exists()) {
                saveResource("shop/" + fileName, false);
                getLogger().info("Created shop file: " + fileName);
            }
        }
    }








}