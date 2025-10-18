package serveressentials.serveressentials;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.milkbowl.vault.economy.Economy;
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
import serveressentials.serveressentials.Daily.DailyCommand;
import serveressentials.serveressentials.Daily.DailyRewards;
import serveressentials.serveressentials.Fun.*;
import serveressentials.serveressentials.Managers.*;
import serveressentials.serveressentials.PlaceholderAPI.ServerEssentialsPlaceholder;
import serveressentials.serveressentials.PlaceholderAPI.TopPlaytimePlaceholder;
import serveressentials.serveressentials.Player.*;
import serveressentials.serveressentials.Rtp.RTPCommand;
import serveressentials.serveressentials.Rtp.RTPListener;
import serveressentials.serveressentials.TPA.TPA;
import serveressentials.serveressentials.Vault.VaultCommand;
import serveressentials.serveressentials.Vault.VaultManager;
import serveressentials.serveressentials.Managers.AFKManager;
import serveressentials.serveressentials.config.RTPConfig;
import serveressentials.serveressentials.kit.KitCommand;
import serveressentials.serveressentials.kit.KitConfigManager;
import serveressentials.serveressentials.kit.KitGUIListener;
import serveressentials.serveressentials.kit.KitManager;
import serveressentials.serveressentials.listeners.*;
import serveressentials.serveressentials.auction.*;
import serveressentials.serveressentials.commands.PWCommand;
import serveressentials.serveressentials.config.GUIConfig;
import serveressentials.serveressentials.economy.*;
import serveressentials.serveressentials.interaction_blocks.*;
import serveressentials.serveressentials.nick.NickManager;
import serveressentials.serveressentials.server.*;
import serveressentials.serveressentials.serverEssentials.ServerEssentialsCommand;
import serveressentials.serveressentials.serverEssentials.VersionChecker;
import serveressentials.serveressentials.util.*;
import serveressentials.serveressentials.lobby.*;
import serveressentials.serveressentials.pw.GUIListeners;
import serveressentials.serveressentials.pw.WarpStorage;
import serveressentials.serveressentials.scoreboard.CustomScoreboardManager;
import serveressentials.serveressentials.scoreboard.util.ScoreboardJoinListener;
import serveressentials.serveressentials.staff.*;
import serveressentials.serveressentials.util.MessagesManager;
import serveressentials.serveressentials.utility.EditSignCommand;
import serveressentials.serveressentials.utility.FuckCommand;
import serveressentials.serveressentials.utility.ToggleFlyCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import serveressentials.serveressentials.homes.HomeManager;
import serveressentials.serveressentials.homes.HomeListener;
import serveressentials.serveressentials.homes.HomesGUI;
import serveressentials.serveressentials.util.HomeMessages;
import serveressentials.serveressentials.warp.WarpCommand;
import serveressentials.serveressentials.warp.WarpManager;
import serveressentials.serveressentials.warp.WarpsCommand;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class ServerEssentials extends JavaPlugin implements Listener {

    private static ServerEssentials instance;
    private static final Map<UUID, Double> balances = new HashMap<>();
    private Listener DailyRewardGUI;
    private RulesManager rulesManager;
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
    private DailyRewards dailyRewards;
    private GUIConfig guiConfig;
    private WarpStorage warpStorage;
    private File pwFile;
    private FileConfiguration pwConfig;
    private IgnoreCommand ignoreCommand;
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
    private MsgToggleCommand msgToggleCommand;
    private KillTracker killTracker;
    private NotesCommand notesCommand;
    private List<String> reloadedItems = new ArrayList<>();
    private VaultMessages vaultMessages;
    private VaultManager vaultManager;
    private HomeManager homeManager;
    private HomeMessages messages;
    private HomesGUI homesGUI;
    private HomeListener homeListener;
    private KitMessages kitMessages;
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
    private TPA tpa;
    private boolean vaultInstalled;
    private boolean lpInstalled;
    private boolean papiInstalled;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        saveDefaultConfig();
        homeManager = new HomeManager(this);
        instance = this;
        warpManager = new WarpManager(this);
        AFKManager afkManager;
        warpMessages = new WarpMessages(this);
        saveDefaultConfig();
        vaultInstalled = Bukkit.getPluginManager().getPlugin("Vault") != null;
        lpInstalled = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
        papiInstalled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        Bukkit.getPluginManager().registerEvents(this, this);
        if (!vaultInstalled) getLogger().warning("Vault not found! Some features may be disabled.");
        if (!lpInstalled) getLogger().warning("LuckPerms not found! Some features may be disabled.");
        if (!papiInstalled) getLogger().warning("PlaceholderAPI not found! Some features may be disabled.");
        Bukkit.getPluginManager().registerEvents(this, this);
        dailyRewards = new DailyRewards(this);
        saveDefaultPlaceholders();
        afkManager = new AFKManager(this);
        nickConfigFile = new File(getDataFolder(), "config/nick/nick.yml");
        messagesFile = new File(getDataFolder(), "messages/player.yml");
        nicksFile = new File(getDataFolder(), "storage/nicks.yml");
        saveDefaultFile(nickConfigFile, "/config/nick/nick.yml");
        saveDefaultFile(messagesFile, "/messages/player.yml");
        saveDefaultFile(nicksFile, null);
        loadPlaceholders();
        tpa = new TPA(this);
        getCommand("daily").setExecutor(new DailyCommand(this));
        EconomyManager.loadBalances(getDataFolder());
        HashMap<UUID, UUID> lastMessageMap = new HashMap<>();
        getCommand("warp").setExecutor(new WarpCommand(this));
        getCommand("setwarp").setExecutor(new WarpCommand(this));
        getCommand("delwarp").setExecutor(new WarpCommand(this));
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
        messagesManager.load("utility.yml");
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
        messagesManager.load("utility.yml");
        messagesManager.load("fun.yml");
        auctionManager = new AuctionManager(this);
        guiManager = new GUIManager(this);
        File messagesFile = new File(getDataFolder(), "messages/scoreboard_system.yml");
        if (!messagesFile.exists()) {
            saveResource("messages/scoreboard_system.yml", false);
        }
        reloadPluginConfig();
        String[] sectionFiles = {
                "food.yml",
                "tools.yml",
                "blocks.yml",
                "misc.yml",
                "decoration.yml",
                "mobs.yml",
                "redstone.yml",
                "ores.yml",
                "farming.yml",
                "coloredblocks.yml",
                "customsection1.yml",
                "customsection2.yml",
                "customsection3.yml",
                "customsection4.yml",
                "customsection5.yml"
        };
        for (String fileName : sectionFiles) {
            File sectionFile = new File(getDataFolder(), "Shop/" + fileName);
            if (!sectionFile.exists()) {
                saveResource("Shop/" + fileName, false);
            }
        }
        saveDefaultConfig();
        saveResource("config.yml", false);
        saveResource("Shop/main.yml", false);
        JoinLeaveManager.load();
        saveResource("messages/lobby.yml", false);
        LobbyMessages.setup();
        LobbyStorage.setup();
        LobbyConfig.setup();
        messagesManager.load("player.yml");
        EconomyManager.loadBalances(getDataFolder());
        this.startTime = System.currentTimeMillis();
        originalMotd = Bukkit.getMotd();
        CustomScoreboardManager manager = new CustomScoreboardManager(this);
        String originalMotd = Bukkit.getMotd();
        LockdownCommand lockdownCommand = new LockdownCommand(this, messagesManager, originalMotd);
        PluginCommand cmd = getCommand("lockdown");
        new TopPlaytimePlaceholder(this).register();
        this.PlaytimeManager = new PlaytimeManager(this);
        setupEconomy();
        this.serverMessages = new ServerMessages(this, "messages/server.yml");
        BookCommand bookCommand = new BookCommand(playerMessages);
        if (getCommand("book") != null) {
            getCommand("book").setExecutor(bookCommand);
        }
               this.guiConfig = new GUIConfig(getConfig());
        this.warpStorage = new WarpStorage(this, guiConfig);
        warpStorage.loadAll();
        auctionMessagesManager = new AuctionMessagesManager(this);
        auctionMessagesManager.loadMessages(); // load auction.yml
        playerMessages = new PlayerMessages(this);
        KillTracker killTracker = new KillTracker(this);
        rulesManager = new RulesManager(this);
        ConsoleCommandManager commandManager = new ConsoleCommandManager(this);
        releaseLocation = new Location(Bukkit.getWorld("world"), 0, 65, 0);
        getServer().getPluginManager().registerEvents(new JoinCommandListener(commandManager), this);
        PWCommand pwCommandExecutor = new PWCommand(this, warpStorage, guiConfig);
        PluginCommand pwCommand = getCommand("pw");
        if (pwCommand != null) {
            pwCommand.setExecutor(pwCommandExecutor);
            pwCommand.setTabCompleter(new PWTabCompleter(warpStorage));
        }
        FileConfiguration pwConfig = getConfig();
        if (!pwConfig.contains("gui")) {

            saveConfig();
        }
        this.getCommand("se").setExecutor(new ServerEssentialsCommand(this));
        getCommand("shop").setExecutor(new ShopCommand());
        VersionChecker.checkLatestVersion(this);
        Bukkit.getPluginManager().registerEvents(new VersionNotifyJoinListener(), this);
        getServer().getServicesManager().register(
                net.milkbowl.vault.economy.Economy.class,
                new EconomyManager(),
                this,
                org.bukkit.plugin.ServicePriority.Highest
        );
        getServer().getPluginManager().registerEvents(new RTPListener(this), this);
        nickManager = new NickManager(getDataFolder());
        getCommand("nick").setExecutor(nickManager);
        getCommand("nicks").setExecutor(nickManager);
        RTPConfig.load();
        new RTPMessages(this).fullReload();
        getServer().getPluginManager().registerEvents(new AdminChatListener(), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);
        getLogger().info("ServerEssentials enabled (dashboard command registered).");
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);;
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
            public void onJoin(PlayerJoinEvent e) {
            }
        }, this);
        vaultEconomy = getServer().getServicesManager().load(Economy.class);
        if (vaultEconomy == null) {
            getLogger().severe("No economy provider found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ShopGUIManager.init(vaultEconomy, getDataFolder());
        ShopGUIManager.loadShopConfigs(new File(getDataFolder(), "shop"));
        getServer().getPluginManager().registerEvents(new ShopGUIListener(), this);
        this.getCommand("balance").setExecutor(new BalanceCommand(vaultEconomy));
        this.getCommand("balancetop").setExecutor(new BalanceTopCommand(this, vaultEconomy));
        this.getCommand("eco").setExecutor(new EcoCommand(vaultEconomy));
        this.getCommand("pay").setExecutor(new PayCommand(vaultEconomy));
        this.getCommand("coinflip").setExecutor(new CoinFlipCommand(vaultEconomy));
        ReportsListCommand reportsListCommand = new ReportsListCommand(this, playerMessages);
        getCommand("reports").setExecutor(reportsListCommand);
        getCommand("rtp").setExecutor(new RTPCommand(this));
        getCommand("glow").setExecutor(new GlowCommand(funMessages));
        getCommand("reply").setExecutor(new ReplyCommand(lastMessageMap, playerMessages));
        getCommand("ban").setExecutor(new BanCommand(getBanManager(), this));
        getCommand("unban").setExecutor(new UnbanCommand(this, banManager));
        getCommand("powertool").setExecutor(new PowerToolCommand(this, funMessages));
        this.getCommand("editsign").setExecutor(new EditSignCommand(this));
        getCommand("auction").setExecutor(new AuctionCommand(this));
        File shopFolder = new File(getDataFolder(), "shop");
        EconomyMessagesManager.setup(this);
        EconomyMessagesManager economymessages = new EconomyMessagesManager();
        getServer().getPluginManager().registerEvents(new AuctionListeners(this), this);
        getCommand("spawner").setExecutor(new SpawnerCommand(serverMessages));
        getCommand("condense").setExecutor(new CondenseCommand(this));
        getCommand("paytoggle").setExecutor(new PayToggleCommand());
        getCommand("payconfirmtoggle").setExecutor(new PayConfirmToggleCommand());
        ignoreCommand = new IgnoreCommand(playerMessages);
        if (getCommand("ignore") != null) getCommand("ignore").setExecutor(ignoreCommand);
        getCommand("whois").setExecutor(new WhoIsCommand());
        getCommand("seen").setExecutor(new SeenCommand(this));
        getCommand("ext").setExecutor(new ExtCommand(this));
        KittyCannonCommand kittyCannonCommand = new KittyCannonCommand(funMessages);
        getCommand("kittycannon").setExecutor(kittyCannonCommand);
        getCommand("kittycannon").setTabCompleter(kittyCannonCommand);
        BeezookaCommand beezookaCommand = new BeezookaCommand(funMessages);
        getCommand("beezooka").setExecutor(beezookaCommand);
        getCommand("beezooka").setTabCompleter(beezookaCommand);
        getCommand("togglefly").setExecutor(new ToggleFlyCommand());
        InventorySortCommand inventorySortCommand = new InventorySortCommand(playerMessages);
        getCommand("inventorysort").setExecutor(inventorySortCommand);
        getCommand("nuke").setExecutor(new NukeCommand(this));
        new FirstJoinManager(this);
        FireballCommand fireballCommand = new FireballCommand(funMessages);
        getCommand("fireball").setExecutor(fireballCommand);
        getCommand("lightning").setExecutor(new LightningCommand(funMessages));
        ThunderCommand thunderCommand = new ThunderCommand(funMessages);
        getCommand("thunder").setExecutor(thunderCommand);
        getCommand("thunder").setTabCompleter(thunderCommand);
        getCommand("broadcastworld").setExecutor(new BroadcastWorldCommand(serverMessages));
        getCommand("recipe").setExecutor(new RecipeCommand(playerMessages));
        NearCommand nearCommand = new NearCommand(playerMessages);
        if (getCommand("near") != null) {
            getCommand("near").setExecutor(nearCommand);
        }        getCommand("stonecutter").setExecutor(new StonecutterCommand(this));
        getCommand("loom").setExecutor(new LoomCommand(this));
        getCommand("cartographytable").setExecutor(new CartographyTableCommand(this));
        getCommand("smithingtable").setExecutor(new SmithingTableCommand(this));
        getCommand("brewingstand").setExecutor(new BrewingStandCommand(this));getCommand("craftingtable").setExecutor(new CraftingTableCommand(this));
        getCommand("anvil").setExecutor(new AnvilCommand(this));
        getCommand("furnace").setExecutor(new FurnaceCommand(this));
        getCommand("blastfurnace").setExecutor(new BlastFurnaceCommand(this));
        getCommand("smoker").setExecutor(new SmokerCommand(this));
        getCommand("grindstone").setExecutor(new GrindstoneCommand(this));
        this.getCommand("fuck").setExecutor(new FuckCommand());
        saveResource("messages/homes.yml", false);
        this.messages = new HomeMessages(this, "messages/homes.yml");
        this.homesGUI = new HomesGUI(this, homeManager, messages);
        this.homeListener = new HomeListener(this, homeManager, homesGUI, messages);
        Bukkit.getPluginManager().registerEvents(homeListener, this);
        getCommand("unloadworld").setExecutor(new UnloadWorldCommand(serverMessages));
        getCommand("loadworld").setExecutor(new LoadWorldCommand(serverMessages));
        getCommand("worldlist").setExecutor(new WorldListCommand(serverMessages));
        vaultMessages = new VaultMessages(this);
        vaultManager = new VaultManager(this, vaultMessages);
        this.getCommand("pv").setExecutor(new VaultCommand(vaultManager, vaultMessages));
        getServer().getPluginManager().registerEvents(lockdownCommand, this);
        long serverStartTime = System.currentTimeMillis();
        getCommand("uptime").setExecutor(new UptimeCommand(serverStartTime, serverMessages));
        getCommand("tpp").setExecutor(new TeleportWorldCommand(playerMessages));
        getCommand("tpp").setTabCompleter(new TeleportWorldCommand(playerMessages));
        getCommand("tree").setExecutor(new TreeCommand(playerMessages));
        CanonCommand canonCommand = new CanonCommand(funMessages);
        if (getCommand("canon") != null) getCommand("canon").setExecutor(canonCommand);
        ExplosionCommand explosionCommand = new ExplosionCommand(funMessages);
        getCommand("explosion").setExecutor(explosionCommand);
        getCommand("explosion").setTabCompleter(explosionCommand);
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Vault not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        KitConfigManager.reload();
        KitManager.loadKits(KitConfigManager.getConfig());
        kitMessages = new KitMessages(this);
        KitCommand kitCommand = new KitCommand();
        getCommand("kit").setExecutor(kitCommand);
        getServer().getPluginManager().registerEvents(new KitGUIListener(kitCommand), this);
        killTracker = new KillTracker(this);
        getCommand("kills").setExecutor(new KillCommand(killTracker, playerMessages));
        SessionManager sessionManager = new SessionManager(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(sessionManager), this);
        getCommand("session").setExecutor(new SessionCommand(playerMessages, sessionManager));
        getCommand("playtime").setExecutor(new PlaytimeCommand(playerMessages));
        getCommand("vanish").setExecutor(adminUtils);
        getCommand("god").setExecutor(adminUtils);
        getCommand("invsee").setExecutor(adminUtils);
        getCommand("daily").setExecutor((sender, command, label, args) -> {
            if (sender instanceof org.bukkit.entity.Player player) {
                if (player.hasPermission("daily.use")) {
                    dailyRewards.openRewardsGUI(player, 1);
                } else {
                    player.sendMessage("§cYou don't have permission.");
                }
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        });
        getCommand("sereload").setExecutor(new ReloadCommand(this));
        rulesManager = new RulesManager(this);
        RulesCommand rulesCommand = new RulesCommand(rulesManager, playerMessages);
        getCommand("rules").setExecutor(rulesCommand);
        getCommand("rules").setTabCompleter(rulesCommand);
        getCommand("invclear").setExecutor(adminUtils);
        getCommand("tp").setExecutor(adminUtils);
        ReportCommand reportCommand = new ReportCommand(this, playerMessages);
        getCommand("report").setExecutor(reportCommand);
        getCommand("report").setTabCompleter(reportCommand);
        getCommand("reportclear").setExecutor(reportCommand);
        getCommand("reportclear").setTabCompleter(reportCommand);
        getCommand("adminchat").setExecutor(new AdminChatCommand());
        getCommand("broadcast").setExecutor(new BroadcastCommand(serverMessages));
        getCommand("rename").setExecutor(new RenameItemCommand(this));
        getCommand("setloreline").setExecutor(new SetLoreLineCommand(this));
        this.getCommand("ping").setExecutor(new PingCommand(this));
        Bukkit.getPluginManager().registerEvents(new LobbyListener(), this);
        getCommand("lobby").setExecutor(new LobbyCommand());
        getCommand("lobby").setExecutor(new LobbyCommand());
        BackCommand backCommand = new BackCommand(playerMessages);
        getCommand("back").setExecutor(backCommand);
        getCommand("back").setTabCompleter(backCommand);
        getServer().getPluginManager().registerEvents(new BackListener(), this);
        getCommand("death").setExecutor(new DeathCommand());
        getCommand("motd").setExecutor(new MotdCommand(this));
        getCommand("heal").setExecutor(new HealCommand(this));
        getCommand("feed").setExecutor(new FeedCommand());
        PTimeCommand pTimeCommand = new PTimeCommand(this);
        this.getCommand("ptime").setExecutor(pTimeCommand);
        this.getCommand("ptime").setTabCompleter(pTimeCommand);
        getCommand("playerinfo").setExecutor(new PlayerInfoCommand(this));
        this.getCommand("launch").setExecutor(new LaunchCommand());
        getCommand("gravity").setExecutor(new GravityCommand());
        this.getCommand("repair").setExecutor(new RepairCommand(this));
        getCommand("workbench").setExecutor(new WorkbenchCommand());
        getCommand("enderchest").setExecutor(new EnderChestCommand());
        EnderSeeCommand enderSeeCommand = new EnderSeeCommand(this, messagesManager);
        getCommand("endersee").setExecutor(enderSeeCommand);
        getCommand("endersee").setTabCompleter(enderSeeCommand);
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        this.getCommand("night").setExecutor(new NightCommand(this));
        getCommand("day").setExecutor(new DayCommand());
        this.getCommand("weather").setExecutor(new WeatherCommand(playerMessages));
        this.getCommand("weather").setTabCompleter(new WeatherCommand(playerMessages));
        this.getCommand("mail").setExecutor(new MailCommand(this));
        msgToggleCommand = new MsgToggleCommand(this);
        getCommand("msgtoggle").setExecutor(msgToggleCommand);
        getServer().getPluginManager().registerEvents(new MsgListener(this), this);
        getCommand("kickall").setExecutor(new KickAllCommand());
        this.getCommand("suicide").setExecutor(new SuicideCommand(this));
        getCommand("alts").setExecutor(new AltsCommand(this));
        getCommand("reboot").setExecutor(new RebootCommand(this, serverMessages));
        getCommand("disposal").setExecutor(new DisposalCommand());
        this.getCommand("top").setExecutor(new TopCommand(this));
        getCommand("bottom").setExecutor(new BottomCommand(playerMessages));
        getCommand("clearchat").setExecutor(new ClearChatCommand(this));
        getCommand("tpoffline").setExecutor(new TPOfflineCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        this.getCommand("speed").setExecutor(new SpeedCommand(this));
        this.getCommand("swap").setExecutor(new SwapCommand(this, funMessages));
        this.getCommand("sleep").setExecutor(new SleepCommand(this));
        this.getCommand("magnet").setExecutor(new MagnetCommand(this));
        playerMessages = new PlayerMessages(this);
        notesCommand = new NotesCommand(playerMessages, this);
        getCommand("notes").setExecutor(notesCommand);
        getCommand("notes").setTabCompleter(notesCommand);
        getCommand("fakeop").setExecutor(new FaekOPCommand());
        getCommand("hat").setExecutor(new HatCommand());
        this.getCommand("realname").setExecutor(new RealNameCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(getMessagesManager(), this));
        getServer().getPluginManager().registerEvents(new ToolMoveFixListener(this), this);
        getCommand("iteminfo").setExecutor(new ItemInfoCommand(playerMessages));
        this.getCommand("pingall").setExecutor(new PingAllCommand(this));
        MessagesManager messages = new MessagesManager(this);
        getCommand("unfreeze").setExecutor(new UnfreezeCommand(messages));
        this.getCommand("world").setExecutor(new WorldCommand(this));
        getCommand("warps").setExecutor(new WarpsCommand(this));
        getCommand("break").setExecutor(new BreakCommand(playerMessages));
        getCommand("Compass").setExecutor(new CompassCommand());
        Economy econ = ServerEssentials.getInstance().getEconomy();
        getCommand("stafflist").setExecutor(new StaffListCommand(this));
        this.getCommand("track").setExecutor(new TrackCommand(this));
        getCommand("burn").setExecutor(new BurnCommand(playerMessages));
        getCommand("celebrate").setExecutor(new CelebrateCommand(funMessages));
        this.getCommand("serverinfo").setExecutor(new ServerInfoCommand(serverMessages));
        this.getCommand("banlist").setExecutor(new BanListCommand(this,banManager));
        Bukkit.getPluginManager().registerEvents(new MotdListener(this), this);
        GUIListeners guiListeners = new GUIListeners(warpStorage, this, guiConfig);
        getServer().getPluginManager().registerEvents(guiListeners, this);
        getServer().getPluginManager().registerEvents(new ScoreboardJoinListener(manager), this);
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                reportCommand.handleJoin(event.getPlayer());
            }
        }, this);
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onDisable(org.bukkit.event.server.PluginDisableEvent e) {
                if (e.getPlugin().equals(ServerEssentials.this)) {
                    EconomyManager.saveBalances();
                }
            }
        }, this);
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            }
            @org.bukkit.event.EventHandler
            public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
            }
        }, this);
        long elapsed = System.currentTimeMillis() - start;
        BannerUtil.printBanner(elapsed);

    }
    public void setReloadedItems(List<String> items) {
        this.reloadedItems = items;
    }
    public List<String> getReloadedItems() {
        return reloadedItems;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
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
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("myplugin.admin")) return;
        if (!vaultInstalled || !lpInstalled || !papiInstalled) {
            player.sendMessage(ChatColor.RED + "⚠ Some dependencies are missing:");
            if (!vaultInstalled)
                player.sendMessage(ChatColor.YELLOW + " - Vault not found!");
            if (!lpInstalled)
                player.sendMessage(ChatColor.YELLOW + " - LuckPerms not found!");
            if (!papiInstalled)
                player.sendMessage(ChatColor.YELLOW + " - PlaceholderAPI not found!");
        }
    }
    public KitMessages getKitMessages() {
        return kitMessages;
    }
    public RulesManager getRulesManager() {
        return rulesManager;
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
    public IgnoreCommand getIgnoreCommand() {
        return ignoreCommand;
    }
    public DailyRewards getDailyRewards() {
        return dailyRewards;
    }
    public PlayerMessages getPlayerMessages() {
        return playerMessages;
    }
}

