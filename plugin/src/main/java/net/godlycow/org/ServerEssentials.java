package net.godlycow.org;

import com.serveressentials.api.afk.AFKAPI;
import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.kit.KitAPI;
import com.serveressentials.api.lobby.LobbyAPI;
import com.serveressentials.api.mail.MailAPI;
import com.serveressentials.api.nick.NickAPI;
import com.serveressentials.api.report.ReportAPI;
import com.serveressentials.api.rtp.RtpAPI;
import com.serveressentials.api.scoreboard.ScoreboardAPI;
import com.serveressentials.api.sellgui.SellGUIAPI;
import com.serveressentials.api.shop.ShopAPI;
import com.serveressentials.api.tpa.TPAAPI;
import com.serveressentials.api.warp.WarpAPI;
import net.godlycow.org.PlaceholderAPI.*;
import net.godlycow.org.afk.api.AFKAPIImpl;
import net.godlycow.org.api.PluginAPIImpl;
import net.godlycow.org.auction.api.AuctionAPIImpl;
import net.godlycow.org.auction.gui.AuctionGUIListener;
import net.godlycow.org.auction.storage.AuctionStorage;
import net.godlycow.org.back.storage.BackDataStorage;
import net.godlycow.org.back.trigger.BackListener;
import net.godlycow.org.ban.storage.BanStorage;
import net.godlycow.org.ban.trigger.BanListener;
import net.godlycow.org.daily.api.DailyAPIImpl;
import net.godlycow.org.daily.trigger.DailyListener;
import net.godlycow.org.database.type.DatabaseType;
import net.godlycow.org.economy.api.EconomyAPIImpl;
import net.godlycow.org.economy.eco.EconomyManager;
import net.godlycow.org.economy.shop.ShopDataManager;
import net.godlycow.org.economy.shop.api.ShopAPIImpl;
import net.godlycow.org.economy.shop.gui.ShopGUIManager;
import net.godlycow.org.economy.shop.gui.trigger.ShopGUIListener;
import net.godlycow.org.economy.shop.storage.ShopStorage;
import net.godlycow.org.executor.CommandExecutor;
import net.godlycow.org.homes.gui.trigger.HomeGUIListener;
import net.godlycow.org.homes.storage.HomeStorage;
import net.godlycow.org.kit.api.KitAPIImpl;
import net.godlycow.org.kit.trigger.KitGUIListener;
import net.godlycow.org.listeners.admin.AdminChatListener;
import net.godlycow.org.listeners.admin.AdminUtilitiesListener;
import net.godlycow.org.listeners.admin.FlyListener;
import net.godlycow.org.listeners.admin.FreezeListener;
import net.godlycow.org.listeners.block.SpawnerPlaceListener;
import net.godlycow.org.listeners.chat.MuteListener;
import net.godlycow.org.listeners.event.JoinLeaveListener;
import net.godlycow.org.listeners.gui.DisposalListener;
import net.godlycow.org.lobby.api.LobbyAPIImpl;
import net.godlycow.org.lobby.storage.LobbyStorage;
import net.godlycow.org.lobby.trigger.LobbyListener;
import net.godlycow.org.mail.api.MailAPIImpl;
import net.godlycow.org.mail.storage.MailStorage;
import net.godlycow.org.mail.trigger.MailListener;
import net.godlycow.org.managers.cooldown.CooldownManager;
import net.godlycow.org.managers.player.PlaytimeManager;
import net.godlycow.org.managers.player.SessionManager;
import net.godlycow.org.managers.plugin.ReloadManager;
import net.godlycow.org.mute.storage.MuteStorage;
import net.godlycow.org.nick.api.NickAPIImpl;
import net.godlycow.org.nick.storage.NickStorage;
import net.godlycow.org.notes.storage.NotesStorage;
import net.godlycow.org.reports.api.ReportAPIImpl;
import net.godlycow.org.reports.storage.ReportStorage;
import net.godlycow.org.reports.trigger.ReportListener;
import net.godlycow.org.rtp.*;
import net.godlycow.org.rtp.api.RtpAPIImpl;
import net.godlycow.org.rtp.storage.RtpLocationStorage;
import net.godlycow.org.rtp.trigger.RtpListener;
import net.godlycow.org.rules.gui.RulesGUI;
import net.godlycow.org.rules.storage.RulesStorage;
import net.godlycow.org.rules.trigger.RulesListener;
import net.godlycow.org.scoreboard.api.ScoreboardAPIImpl;
import net.godlycow.org.scoreboard.runtime.ScoreboardListener;
import net.godlycow.org.scoreboard.runtime.ScoreboardUpdater;
import net.godlycow.org.scoreboard.storage.ScoreboardStorage;
import net.godlycow.org.sellgui.api.SellGUIAPIImpl;
import net.godlycow.org.sellgui.gui.trigger.SellGUIListener;
import net.godlycow.org.sellgui.gui.SellGUIManager;
import net.godlycow.org.sellgui.storage.SellStorage;
import net.godlycow.org.serverEssentials.core.ServerEssentialsCommand;
import net.godlycow.org.serverEssentials.version.resolver.VersionChecker;
import net.godlycow.org.afk.*;
import net.godlycow.org.back.*;
import net.godlycow.org.commands.*;
import net.godlycow.org.commands.config.*;
import net.godlycow.org.commands.impl.*;
import net.godlycow.org.daily.*;
import net.godlycow.org.daily.storage.*;
import net.godlycow.org.database.*;
import net.godlycow.org.homes.*;
import net.godlycow.org.kit.*;
import net.godlycow.org.kit.storage.*;
import net.godlycow.org.language.*;
import net.godlycow.org.language.storage.*;
import net.godlycow.org.mail.*;
import net.godlycow.org.nick.*;
import net.godlycow.org.reports.*;
import net.godlycow.org.tpa.TPAConfig;
import net.godlycow.org.tpa.api.TPAAPIImpl;
import net.godlycow.org.tpa.trigger.TPAListener;
import net.godlycow.org.tpa.storage.TPAStorage;
import net.godlycow.org.util.logger.BannerUtil;
import net.godlycow.org.warp.*;

import net.godlycow.org.hooks.*;

import net.godlycow.org.warp.api.WarpAPIImpl;
import net.godlycow.org.warp.storage.WarpStorage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;



public class ServerEssentials extends JavaPlugin implements Listener {

    private static ServerEssentials instance;
    private long startTime;
    private long serverStartTime;
    private final HashMap<UUID, UUID> lastMessageMap = new HashMap<>();
    private final HashMap<UUID, BukkitRunnable> jailReleaseTasks = new HashMap<>();
    private Location releaseLocation;
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private File prefixFile;
    private FileConfiguration prefixConfig;
    private FileConfiguration starterMoneyConfig;
    private PlaytimeManager PlaytimeManager;
    private File pwFile;
    private FileConfiguration pwConfig;
    private BukkitAudiences adventure;
    private FileConfiguration offlineConfig;
    private File offlineFile;
    private AuctionGUIListener guiManager;
    private static final int BSTATS_PLUGIN_ID = 27221;
    private List<String> reloadedItems = new ArrayList<>();

    private FileConfiguration placeholdersConfig;
    private File placeholdersFile;
    private File nickConfigFile;
    private final Map<String, YamlConfiguration> configs = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();
    private File messagesFile;
    private FirstJoinManager firstJoinManager;
    private YamlConfiguration cfg;
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
    private EnderChestConfig enderChestConfig;
    private EnderChestCommand enderChestCommand;
    private SeenConfig seenConfig;
    private SeenCommand seenCommand;
    private BackConfig backConfig;
    private BackManager backManager;
    private BackListener backListener;
    private BackCommand backCommand;
    private BackDataStorage backDataStorage;
    private TreeConfig treeConfig;
    private TreeCommand treeCommand;
    private NotesConfig notesConfig;
    private NotesStorage notesStorage;
    private NotesCommand notesCommand;
    private ScoreboardConfig scoreboardConfig;
    private ScoreboardStorage scoreboardStorage;
    private ScoreboardUpdater scoreboardUpdater;
    private ScoreboardListener scoreboardListener;
    private ScoreboardCommand scoreboardCommand;
    private SessionManager sessionManager;
    private SessionConfig sessionConfig;
    private SessionCommand sessionCommand;
    private SessionListener sessionListener;
    private ItemInfoConfig itemInfoConfig;
    private ItemInfoCommand itemInfoCommand;
    private DeathConfig deathConfig;
    private DeathCommand deathCommand;
    private RealNameConfig realNameConfig;
    private RealNameCommand realNameCommand;
    private SpeedConfig speedConfig;
    private SpeedCommand speedCommand;
    private LaunchConfig launchConfig;
    private LaunchCommand launchCommand;
    private RepairConfig repairConfig;
    private RepairCommand repairCommand;
    private SleepConfig sleepConfig;
    private SleepCommand sleepCommand;
    private PingConfig pingConfig;
    private PingCommand pingCommand;
    private DisposalConfig disposalConfig;
    private DisposalCommand disposalCommand;
    private DisposalListener disposalListener;
    private CompassConfig compassConfig;
    private CompassCommand compassCommand;
    private TopConfig topConfig;
    private TopCommand topCommand;
    private BurnConfig burnConfig;
    private BurnCommand burnCommand;
    private FeedConfig feedConfig;
    private FeedCommand feedCommand;
    private HatConfig hatConfig;
    private HatCommand hatCommand;
    private PTimeConfig pTimeConfig;
    private PTimeCommand pTimeCommand;
    private NightConfig nightConfig;
    private NightCommand nightCommand;
    private AuctionConfig auctionConfig;
    private AuctionStorage auctionStorage;
    private AuctionGUIListener auctionGUIListener;
    private AuctionCommand auctionCommand;
    private PayToggleConfig payToggleConfig;
    private PayToggleCommand payToggleCommand;
    private PayConfig payConfig;
    private PayCommand payCommand;
    private EcoConfig ecoConfig;
    private EcoCommand ecoCommand;
    private PayConfirmToggleConfig payConfirmToggleConfig;
    private PayConfirmToggleCommand payConfirmToggleCommand;
    private BalanceConfig balanceConfig;
    private BalanceCommand balanceCommand;
    private BalanceTopConfig balanceTopConfig;
    private BalanceTopCommand balanceTopCommand;
    private CoinFlipConfig coinFlipConfig;
    private CoinFlipCommand coinFlipCommand;
    private ShopConfig shopConfig;
    private PowerToolConfig powerToolConfig;
    private PowerToolCommand powerToolCommand;
    private GravityConfig gravityConfig;
    private GravityCommand gravityCommand;
    private ThunderConfig thunderConfig;
    private ThunderCommand thunderCommand;
    private CelebrateConfig celebrateConfig;
    private CelebrateCommand celebrateCommand;
    private FireballConfig fireballConfig;
    private FireballCommand fireballCommand;
    private FakeopConfig fakeopConfig;
    private FakeopCommand fakeopCommand;
    private WhoisConfig whoisConfig;
    private WhoisCommand whoisCommand;
    private KittyCannonConfig kittycannonConfig;
    private KittyCannonCommand kittycannonCommand;
    private ExplosionConfig explosionConfig;
    private ExplosionCommand explosionCommand;
    private CanonConfig canonConfig;
    private CanonCommand canonCommand;
    private LightningConfig lightningConfig;
    private LightningCommand lightningCommand;
    private SwapConfig swapConfig;
    private SwapCommand swapCommand;
    private BeezookaConfig beezookaConfig;
    private BeezookaCommand beezookaCommand;
    private GlowConfig glowConfig;
    private GlowCommand glowCommand;
    private HomesConfig homesConfig;
    private HomeStorage homeStorage;
    private HomeManager homeManager;
    private HomeGUIListener homeGUIListener;
    private HomeCommand homeCommand;
    private LoomConfig loomConfig;
    private LoomCommand loomCommand;
    private StonecutterConfig stonecutterConfig;
    private StonecutterCommand stonecutterCommand;
    private GrindstoneConfig grindstoneConfig;
    private GrindstoneCommand grindstoneCommand;
    private CartographyTableConfig cartographyTableConfig;
    private CartographyTableCommand cartographyTableCommand;
    private AnvilConfig anvilConfig;
    private AnvilCommand anvilCommand;
    private CraftingTableConfig craftingTableConfig;
    private CraftingTableCommand craftingTableCommand;
    private SmithingTableConfig smithingTableConfig;
    private SmithingTableCommand smithingTableCommand;
    private LobbyConfig lobbyConfig;
    private LobbyStorage lobbyStorage;
    private LobbyCommand lobbyCommand;
    private LobbyListener lobbyListener;
    private BroadcastConfig broadcastConfig;
    private BroadcastCommand broadcastCommand;
    private RebootConfig rebootConfig;
    private RebootCommand rebootCommand;
    private BroadcastWorldConfig broadcastWorldConfig;
    private BroadcastWorldCommand broadcastWorldCommand;
    private SpawnerConfig spawnerConfig;
    private SpawnerCommand spawnerCommand;
    private ServerInfoConfig serverInfoConfig;
    private ServerInfoCommand serverInfoCommand;
    private UnloadWorldConfig unloadWorldConfig;
    private UnloadWorldCommand unloadWorldCommand;
    private UptimeConfig uptimeConfig;
    private UptimeCommand uptimeCommand;
    private LoadWorldConfig loadWorldConfig;
    private LoadWorldCommand loadWorldCommand;
    private WorldListConfig worldListConfig;
    private WorldListCommand worldListCommand;
    private PlayerInfoConfig playerInfoConfig;
    private PlayerInfoCommand playerInfoCommand;

    private BanConfig banConfig;
    private BanStorage banStorage;
    private BanCommand banCommand;
    private BanListCommand banListCommand;
    private BanListener banListener;
    private UnbanCommand unbanCommand;
    private HealConfig healConfig;
    private HealCommand healCommand;
    private EnderSeeConfig enderSeeConfig;
    private EnderSeeCommand enderSeeCommand;
    private FreezeConfig freezeConfig;
    private FreezeCommand freezeCommand;
    private UnfreezeConfig unfreezeConfig;
    private UnfreezeCommand unfreezeCommand;
    private AdminChatConfig adminChatConfig;
    private AdminChatCommand adminChatCommand;
    private ClearChatConfig clearChatConfig;
    private ClearChatCommand clearChatCommand;
    private VanishConfig vanishConfig;
    private VanishCommand vanishCommand;
    private GodConfig godConfig;
    private GodCommand godCommand;
    private InvseeConfig invseeConfig;
    private InvseeCommand invseeCommand;
    private InvClearConfig invClearConfig;
    private InvClearCommand invClearCommand;
    private TpConfig tpConfig;
    private TpCommand tpCommand;
    private KickAllConfig kickAllConfig;
    private KickAllCommand kickAllCommand;
    private PingAllConfig pingAllConfig;
    private PingAllCommand pingAllCommand;
    private MuteConfig muteConfig;
    private MuteStorage muteStorage;
    private MuteCommand muteCommand;
    private UnmuteCommand unmuteCommand;
    private MuteListener muteListener;
    private AltsConfig altsConfig;
    private AltsCommand altsCommand;
    private StaffListConfig staffListConfig;
    private StaffListCommand staffListCommand;
    private EditSignConfig editSignConfig;
    private EditSignCommand editSignCommand;
    private WarpStorage warpStorage;
    private WarpConfig warpConfig;
    private WarpManager warpManager;
    private WarpTeleportCommand warpCommand;
    private WarpSetCommand setwarpCommand;
    private WarpDeleteCommand delwarpCommand;
    private WarpsCommand warpsCommand;
    private HooksManager hooksManager;
    private EconomyManager economyManager;
    private ChatFilterConfig chatFilterConfig;
    private ChatFilterCommand chatFilterListener;
    private NickConfig nickConfig;
    private NickCommand nickCommand;
    private NickManager nickManager;
    private AFKConfig afkConfig;
    private AFKCommand afkCommand;
    private AFKManager afkManager;
    private NickStorage nickStorage;
    private ReloadManager reloadManager;
    private ShopAPI shopAPI;
    private AuctionAPI auctionAPI;
    private AFKAPI afkAPI;
    private BackAPI backAPI;
    private KitManager kitManager;
    private KitConfigManager kitConfigManager;
    private LobbyAPI lobbyAPI;
    private ReportAPI reportAPI;
    private NickAPI nickAPI;
    private DailyAPI dailyAPI;
    private EconomyAPI economyAPI;
    private MailAPI mailAPI;
    private KitAPI kitAPI;
    private RtpListener rtpListener;
    private RtpConfig rtpConfig;
    private RtpAPI rtpAPI;
    private ScoreboardAPI scoreboardAPI;
    private SellGUIManager sellGUIManager;
    private SellGUIAPI sellguiAPI;
    private TPAAPI tpaAPI;
    private WarpAPI warpAPI;
    private ShopGUIManager shopGUIManager;
    private ShopCommand shopCommand;
    private ShopDataManager shopDataManager;
    private ShopStorage shopStorage;
    private ShopGUIManager shopGuiManager;




    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        serverStartTime = System.currentTimeMillis();

        saveDefaultConfig();
        saveResourceFolder("shop");
        instance = this;
        languageManager = new LanguageManager(this);
        languageManager.loadLanguages();
        languageManager.setDefaultLanguage(getConfig().getString("default_language", "en"));
        String prefix = getConfig().getString("message-prefix", "");
        languageManager.setGlobalPrefix(prefix);
        databaseManager = new DatabaseManager(this);
        initializeDatabases();





        hooksManager = HooksManager.getInstance(this);
        hooksManager.initializeHooks();


        boolean papiAvailable = PlaceholderAPIHook.getInstance().init();

        if (papiAvailable) {
            new ServerEssentialsPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        } else {
            getLogger().warning("PlaceholderAPI not found. Placeholders will be unavailable.");
        }


        this.economyManager = EconomyManager.getInstance(this, databaseManager, hooksManager);
        economyManager.initialize();

        if (economyManager.isEnabled()) {
            getLogger().info("§aEconomy system initialized: " + economyManager.getEconomyName());
        } else {
            getLogger().warning("§cEconomy system failed to initialize!");
        }

        reloadManager = new ReloadManager(this);




        if (hooksManager.isLuckPermsActive()) {
        }

        PlayerLanguageStorage langStorage = new PlayerLanguageStorage.YamlStorage(this);
        playerLanguageManager = new PlayerLanguageManager(langStorage, languageManager);

        initializeKitSystem();
        initializeTPASystem();
        initializeCommandModules();
        initializeMailSystem();
        initializeReportSystem();
        initializeRtpSystem();
        initializeDailySystem();
        initializeRulesSystem();
        initializeBackSystem();
        initializeNotesSystem();
        initializeScoreboardSystem();
        initializeSessionSystem();
        initializeAuctionSystem();
        initializeSellGuiSystem();
        initializeHomesSystem();
        initializeLobbySystem();
        initializeBanSystem();
        initializeMuteSystem();
        initializeWarpSystem();
        initializeNickSystem();
        initializeShopSystem();







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

        this.enderChestConfig = new EnderChestConfig(this);
        this.enderChestCommand = new EnderChestCommand(playerLanguageManager, enderChestConfig, commandDataStorage);

        this.seenConfig = new SeenConfig(this);
        this.seenCommand = new SeenCommand(playerLanguageManager, seenConfig, commandDataStorage);

        this.treeConfig = new TreeConfig(this);
        this.treeCommand = new TreeCommand(playerLanguageManager, treeConfig, commandDataStorage);

        NotesStorage notesStorage = new NotesStorage(this, databaseManager);
        NotesConfig notesConfig = new NotesConfig(this);

        this.itemInfoConfig = new ItemInfoConfig(this);
        this.itemInfoCommand = new ItemInfoCommand(playerLanguageManager, itemInfoConfig, commandDataStorage);

        this.sessionManager = new SessionManager(databaseManager, commandDataStorage);

        this.deathConfig = new DeathConfig(this);
        this.deathCommand = new DeathCommand(playerLanguageManager, deathConfig, commandDataStorage);

        this.realNameConfig = new RealNameConfig(this);
        this.realNameCommand = new RealNameCommand(playerLanguageManager, realNameConfig, commandDataStorage);

        this.sessionConfig = new SessionConfig(this);
        this.sessionCommand = new SessionCommand(
                playerLanguageManager,
                sessionConfig,
                sessionManager,
                commandDataStorage
        );

        this.speedConfig = new SpeedConfig(this);
        this.speedCommand = new SpeedCommand(playerLanguageManager, speedConfig, commandDataStorage);

        this.launchConfig = new LaunchConfig(this);
        this.launchCommand = new LaunchCommand(playerLanguageManager, launchConfig, commandDataStorage);

        this.repairConfig = new RepairConfig(this);
        this.repairCommand = new RepairCommand(playerLanguageManager, repairConfig, commandDataStorage);

        this.sleepConfig = new SleepConfig(this);
        this.sleepCommand = new SleepCommand(playerLanguageManager, sleepConfig, commandDataStorage);

        this.pingConfig = new PingConfig(this);
        this.pingCommand = new PingCommand(playerLanguageManager, pingConfig, commandDataStorage);

        this.disposalConfig = new DisposalConfig(this);
        this.disposalCommand = new DisposalCommand(playerLanguageManager, disposalConfig, commandDataStorage);
        this.disposalListener = new DisposalListener(disposalConfig);

        this.compassConfig = new CompassConfig(this);
        this.compassCommand = new CompassCommand(playerLanguageManager, compassConfig, commandDataStorage);

        this.topConfig = new TopConfig(this);
        this.topCommand = new TopCommand(playerLanguageManager, topConfig, commandDataStorage);

        this.burnConfig = new BurnConfig(this);
        this.burnCommand = new BurnCommand(playerLanguageManager, burnConfig, commandDataStorage);

        this.feedConfig = new FeedConfig(this);
        this.feedCommand = new FeedCommand(playerLanguageManager, feedConfig, commandDataStorage);

        this.hatConfig = new HatConfig(this);
        this.hatCommand = new HatCommand(playerLanguageManager, hatConfig, commandDataStorage);

        this.pTimeConfig = new PTimeConfig(this);
        this.pTimeCommand = new PTimeCommand(playerLanguageManager, pTimeConfig, commandDataStorage);
        this.nightConfig = new NightConfig(this);
        this.nightCommand = new NightCommand(playerLanguageManager, nightConfig, commandDataStorage);
        payToggleConfig = new PayToggleConfig(this);
        payToggleCommand = new PayToggleCommand(playerLanguageManager, payToggleConfig, economyManager);
        payConfig = new PayConfig(this);
        payCommand = new PayCommand(this, playerLanguageManager, payConfig, economyManager);

        ecoConfig = new EcoConfig(this);
        ecoCommand = new EcoCommand(this,playerLanguageManager, ecoConfig, economyManager);

        payConfirmToggleConfig = new PayConfirmToggleConfig(this);
        payConfirmToggleCommand = new PayConfirmToggleCommand(playerLanguageManager, payConfirmToggleConfig, economyManager);

        balanceConfig = new BalanceConfig(this);
        balanceCommand = new BalanceCommand(playerLanguageManager, balanceConfig, economyManager);

        balanceTopConfig = new BalanceTopConfig(this);
        balanceTopCommand = new BalanceTopCommand(playerLanguageManager, balanceTopConfig, economyManager);

        coinFlipConfig = new CoinFlipConfig(this);
        coinFlipCommand = new CoinFlipCommand(this,playerLanguageManager, coinFlipConfig, economyManager);


        this.powerToolConfig = new PowerToolConfig(this);
        this.powerToolCommand = new PowerToolCommand(playerLanguageManager, powerToolConfig, commandDataStorage, this);
        this.gravityConfig = new GravityConfig(this);

        this.gravityCommand = new GravityCommand(playerLanguageManager, gravityConfig, commandDataStorage);

        this.thunderConfig = new ThunderConfig(this);
        this.thunderCommand = new ThunderCommand(playerLanguageManager, thunderConfig, commandDataStorage);
        this.celebrateConfig = new CelebrateConfig(this);
        this.celebrateCommand = new CelebrateCommand(playerLanguageManager, celebrateConfig, commandDataStorage, this);
        this.fireballConfig = new FireballConfig(this);
        this.fireballCommand = new FireballCommand(playerLanguageManager, fireballConfig, commandDataStorage);
        this.fakeopConfig = new FakeopConfig(this);
        this.fakeopCommand = new FakeopCommand(playerLanguageManager, fakeopConfig, commandDataStorage);

        this.whoisConfig = new WhoisConfig(this);
        this.whoisCommand = new WhoisCommand(playerLanguageManager, whoisConfig,commandDataStorage, this);
        this.kittycannonConfig = new KittyCannonConfig(this);
        this.kittycannonCommand = new KittyCannonCommand(playerLanguageManager, kittycannonConfig, commandDataStorage);
        this.explosionConfig = new ExplosionConfig(this);
        this.explosionCommand = new ExplosionCommand(playerLanguageManager, explosionConfig, commandDataStorage);
        this.canonConfig = new CanonConfig(this);
        this.canonCommand = new CanonCommand(playerLanguageManager, canonConfig, commandDataStorage);
        this.lightningConfig = new LightningConfig(this);
        this.lightningCommand = new LightningCommand(playerLanguageManager, lightningConfig, commandDataStorage, this);
        this.swapConfig = new SwapConfig(this);
        this.swapCommand = new SwapCommand(playerLanguageManager, swapConfig, commandDataStorage);
        this.beezookaConfig = new BeezookaConfig(this);
        this.beezookaCommand = new BeezookaCommand(playerLanguageManager, beezookaConfig, commandDataStorage, this);
        this.glowConfig = new GlowConfig(this);
        this.glowCommand = new GlowCommand(playerLanguageManager, glowConfig, commandDataStorage);
        this.loomConfig = new LoomConfig(this);
        this.loomCommand = new LoomCommand(playerLanguageManager, loomConfig, commandDataStorage, this);

        this.stonecutterConfig = new StonecutterConfig(this);
        this.stonecutterCommand = new StonecutterCommand(playerLanguageManager, stonecutterConfig, commandDataStorage, this);

        this.grindstoneConfig = new GrindstoneConfig(this);
        this.grindstoneCommand = new GrindstoneCommand(playerLanguageManager, grindstoneConfig, commandDataStorage, this);

        this.cartographyTableConfig = new CartographyTableConfig(this);
        this.cartographyTableCommand = new CartographyTableCommand(playerLanguageManager, cartographyTableConfig, commandDataStorage, this);
        this.anvilConfig = new AnvilConfig(this);
        this.anvilCommand = new AnvilCommand(playerLanguageManager, anvilConfig, commandDataStorage, this);
        this.craftingTableConfig = new CraftingTableConfig(this);
        this.craftingTableCommand = new CraftingTableCommand(playerLanguageManager, craftingTableConfig, commandDataStorage, this);
        smithingTableConfig = new SmithingTableConfig(this);
        smithingTableCommand = new SmithingTableCommand(playerLanguageManager, smithingTableConfig);
        broadcastConfig = new BroadcastConfig(this);
        broadcastCommand = new BroadcastCommand(playerLanguageManager, broadcastConfig);

        rebootConfig = new RebootConfig(this);
        rebootCommand = new RebootCommand(this, playerLanguageManager, rebootConfig);

        broadcastWorldConfig = new BroadcastWorldConfig(this);
        broadcastWorldCommand = new BroadcastWorldCommand(playerLanguageManager, broadcastWorldConfig);

        spawnerConfig = new SpawnerConfig(this);
        spawnerCommand = new SpawnerCommand(playerLanguageManager, spawnerConfig);

        serverInfoConfig = new ServerInfoConfig(this);
        serverInfoCommand = new ServerInfoCommand(this, playerLanguageManager, serverInfoConfig);

        unloadWorldConfig = new UnloadWorldConfig(this);
        unloadWorldCommand = new UnloadWorldCommand(playerLanguageManager, unloadWorldConfig);

        uptimeConfig = new UptimeConfig(this);
        uptimeCommand = new UptimeCommand(this, serverStartTime, playerLanguageManager, uptimeConfig);

        loadWorldConfig = new LoadWorldConfig(this);
        loadWorldCommand = new LoadWorldCommand(playerLanguageManager, loadWorldConfig);

        worldListConfig = new WorldListConfig(this);
        worldListCommand = new WorldListCommand(playerLanguageManager, worldListConfig);
        playerInfoConfig = new PlayerInfoConfig(this);
        playerInfoCommand = new PlayerInfoCommand(playerLanguageManager, playerInfoConfig);
        unbanCommand = new UnbanCommand(playerLanguageManager, commandDataStorage, banStorage);
        healConfig = new HealConfig(this);
        healCommand = new HealCommand(playerLanguageManager, healConfig);
        enderSeeConfig = new EnderSeeConfig(this);
        enderSeeCommand = new EnderSeeCommand(playerLanguageManager, enderSeeConfig);
        freezeConfig = new FreezeConfig(this);
        freezeCommand = new FreezeCommand(playerLanguageManager, freezeConfig, this);
        unfreezeConfig = new UnfreezeConfig(this);
        unfreezeCommand = new UnfreezeCommand(playerLanguageManager, unfreezeConfig);
        adminChatConfig = new AdminChatConfig(this);
        adminChatCommand = new AdminChatCommand(playerLanguageManager, adminChatConfig, commandDataStorage);
        clearChatConfig = new ClearChatConfig(this);
        clearChatCommand = new ClearChatCommand(playerLanguageManager, clearChatConfig);
        vanishConfig = new VanishConfig(this);
        vanishCommand = new VanishCommand(this,playerLanguageManager, vanishConfig, commandDataStorage);
        godConfig = new GodConfig(this);
        godCommand = new GodCommand(playerLanguageManager, godConfig, commandDataStorage);
        invseeConfig = new InvseeConfig(this);
        invseeCommand = new InvseeCommand(playerLanguageManager, invseeConfig);
        invClearConfig = new InvClearConfig(this);
        invClearCommand = new InvClearCommand(playerLanguageManager, invClearConfig);
        tpConfig = new TpConfig(this);
        tpCommand = new TpCommand(playerLanguageManager, tpConfig);
        RtpConfig rtpConfig = new RtpConfig(this);
        rtpLocationStorage = new RtpLocationStorage(this, databaseManager, "rtp");
        CooldownManager cooldownManager = new CooldownManager();
        kickAllConfig = new KickAllConfig(this);
        kickAllCommand = new KickAllCommand(playerLanguageManager, kickAllConfig);
        pingAllConfig = new PingAllConfig(this);
        pingAllCommand = new PingAllCommand(playerLanguageManager, pingAllConfig);
        altsConfig = new AltsConfig(this);
        altsCommand = new AltsCommand(playerLanguageManager, altsConfig);
        staffListConfig = new StaffListConfig(this);
        staffListCommand = new StaffListCommand(playerLanguageManager, staffListConfig);
        editSignConfig = new EditSignConfig(this);
        editSignCommand = new EditSignCommand(playerLanguageManager, editSignConfig);

        afkConfig = new AFKConfig(this);
        afkManager = new AFKManager(this, afkConfig);
        afkCommand = new AFKCommand(playerLanguageManager, afkConfig, afkManager);



        chatFilterConfig   = new ChatFilterConfig(this);
        chatFilterListener = new ChatFilterCommand(this, chatFilterConfig,playerLanguageManager);



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
        getCommand("bottom").setExecutor(bottomCommand);
        getCommand("condense").setExecutor(condenseCommand);
        getCommand("nuke").setExecutor(nukeCommand);
        getCommand("weather").setExecutor(weatherCommand);
        getCommand("playtime").setExecutor(playtimeCommand);
        getCommand("recipe").setExecutor(recipeCommand);
        getCommand("enderchest").setExecutor(enderChestCommand);
        getCommand("seen").setExecutor(seenCommand);
        getCommand("tree").setExecutor(treeCommand);
        getCommand("session").setExecutor(sessionCommand);
        getCommand("iteminfo").setExecutor(itemInfoCommand);
        getCommand("death").setExecutor(deathCommand);
        getCommand("realname").setExecutor(realNameCommand);
        getCommand("speed").setExecutor(speedCommand);
        getCommand("launch").setExecutor(launchCommand);
        getCommand("repair").setExecutor(repairCommand);
        getCommand("sleep").setExecutor(sleepCommand);
        getCommand("ping").setExecutor(pingCommand);
        getCommand("disposal").setExecutor(disposalCommand);
        getCommand("compass").setExecutor(compassCommand);
        getCommand("top").setExecutor(topCommand);
        getCommand("burn").setExecutor(burnCommand);
        getCommand("feed").setExecutor(feedCommand);
        getCommand("hat").setExecutor(hatCommand);
        getCommand("ptime").setExecutor(pTimeCommand);
        getCommand("night").setExecutor(nightCommand);
        getCommand("auction").setExecutor(auctionCommand);
        getCommand("paytoggle").setExecutor(payToggleCommand);
        getCommand("pay").setExecutor(payCommand);
        getCommand("eco").setExecutor(ecoCommand);
        getCommand("payconfirmtoggle").setExecutor(payConfirmToggleCommand);
        getCommand("balance").setExecutor(balanceCommand);
        getCommand("balancetop").setExecutor(balanceTopCommand);
        getCommand("coinflip").setExecutor(coinFlipCommand);
        getCommand("sell").setExecutor(sellCommand);
        getCommand("powertool").setExecutor(powerToolCommand);
        getCommand("gravity").setExecutor(gravityCommand);
        getCommand("thunder").setExecutor(thunderCommand);
        getCommand("celebrate").setExecutor(celebrateCommand);
        getCommand("fireball").setExecutor(fireballCommand);
        getCommand("fakeop").setExecutor(fakeopCommand);
        getCommand("whois").setExecutor(whoisCommand);
        getCommand("kittycannon").setExecutor(kittycannonCommand);
        getCommand("explosion").setExecutor(explosionCommand);
        getCommand("canon").setExecutor(canonCommand);
        getCommand("lightning").setExecutor(lightningCommand);
        getCommand("swap").setExecutor(swapCommand);
        getCommand("beezooka").setExecutor(beezookaCommand);
        getCommand("glow").setExecutor(glowCommand);
        getCommand("home").setExecutor(homeCommand);
        getCommand("loom").setExecutor(loomCommand);
        getCommand("stonecutter").setExecutor(stonecutterCommand);
        getCommand("grindstone").setExecutor(grindstoneCommand);
        getCommand("cartographytable").setExecutor(cartographyTableCommand);
        getCommand("anvil").setExecutor(anvilCommand);
        getCommand("craftingtable").setExecutor(craftingTableCommand);
        getCommand("smithingtable").setExecutor(smithingTableCommand);
        getCommand("lobby").setExecutor(lobbyCommand);
        getCommand("broadcast").setExecutor(broadcastCommand);
        getCommand("reboot").setExecutor(rebootCommand);
        getCommand("broadcastworld").setExecutor(broadcastWorldCommand);
        getCommand("spawner").setExecutor(spawnerCommand);
        getCommand("serverinfo").setExecutor(serverInfoCommand);
        getCommand("unloadworld").setExecutor(unloadWorldCommand);
        getCommand("uptime").setExecutor(uptimeCommand);
        getCommand("loadworld").setExecutor(loadWorldCommand);
        getCommand("worldlist").setExecutor(worldListCommand);
        getCommand("playerinfo").setExecutor(playerInfoCommand);
        getCommand("ban").setExecutor(banCommand);
        getCommand("banlist").setExecutor(banListCommand);
        getCommand("unban").setExecutor(unbanCommand);
        getCommand("heal").setExecutor(healCommand);
        getCommand("endersee").setExecutor(enderSeeCommand);
        getCommand("freeze").setExecutor(freezeCommand);
        getCommand("unfreeze").setExecutor(unfreezeCommand);
        getCommand("clearchat").setExecutor(clearChatCommand);
        getCommand("vanish").setExecutor(vanishCommand);
        getCommand("god").setExecutor(godCommand);
        getCommand("invsee").setExecutor(invseeCommand);
        getCommand("invclear").setExecutor(invClearCommand);
        getCommand("tp").setExecutor(tpCommand);
        getCommand("kickall").setExecutor(kickAllCommand);
        getCommand("pingall").setExecutor(pingAllCommand);
        getCommand("mute").setExecutor(muteCommand);
        getCommand("unmute").setExecutor(unmuteCommand);
        getCommand("language").setExecutor(new LanguageCommand(languageManager, playerLanguageManager));
        getCommand("database").setExecutor(new DatabaseCommand(databaseManager, languageManager));
        getCommand("rtp").setExecutor(new RtpCommand(this, playerLanguageManager, rtpConfig));
        getCommand("kit").setExecutor(kitCommand);
        getCommand("alts").setExecutor(altsCommand);
        getCommand("stafflist").setExecutor(staffListCommand);
        getCommand("editsign").setExecutor(editSignCommand);
        getCommand("warp").setExecutor(warpCommand);
        getCommand("setwarp").setExecutor(setwarpCommand);
        getCommand("delwarp").setExecutor(delwarpCommand);
        getCommand("warps").setExecutor(warpsCommand);
        getCommand("nick").setExecutor(nickCommand);
        getCommand("nicks").setExecutor(nickCommand);
        getCommand("afk").setExecutor(afkCommand);



        // Tab Completer
        getCommand("tpp").setTabCompleter(tppCommand);
        getCommand("weather").setTabCompleter(weatherCommand);
        getCommand("recipe").setTabCompleter(recipeCommand);
        getCommand("tree").setTabCompleter(treeCommand);
        getCommand("ptime").setTabCompleter(pTimeCommand);
        getCommand("auction").setTabCompleter(auctionCommand);
        getCommand("eco").setTabCompleter(ecoCommand);
        getCommand("coinflip").setTabCompleter(coinFlipCommand);
        getCommand("gravity").setTabCompleter(gravityCommand);
        getCommand("thunder").setTabCompleter(thunderCommand);
        getCommand("celebrate").setTabCompleter(celebrateCommand);
        getCommand("kittycannon").setTabCompleter(kittycannonCommand);
        getCommand("explosion").setTabCompleter(explosionCommand);
        getCommand("beezooka").setTabCompleter(beezookaCommand);
        getCommand("spawner").setTabCompleter(spawnerCommand);
        getCommand("unloadworld").setTabCompleter(unloadWorldCommand);
        getCommand("ban").setTabCompleter(banCommand);
        getCommand("endersee").setTabCompleter(enderSeeCommand);
        getCommand("freeze").setTabCompleter(freezeCommand);
        getCommand("adminchat").setExecutor(adminChatCommand);
        getCommand("invsee").setTabCompleter(invseeCommand);
        getCommand("tp").setTabCompleter(tpCommand);
        getCommand("invclear").setTabCompleter(invClearCommand);









        getServer().getPluginManager().registerEvents(reportListener, this);
        getServer().getPluginManager().registerEvents(tpaListener, this);
        getServer().getPluginManager().registerEvents(mailListener, this);
        getServer().getPluginManager().registerEvents(dailyListener, this);
        getServer().getPluginManager().registerEvents(rulesListener, this);
        getServer().getPluginManager().registerEvents(new SessionListener(sessionManager), this);
        getServer().getPluginManager().registerEvents(disposalListener, this);
        getServer().getPluginManager().registerEvents(auctionGUIListener, this);
        getServer().getPluginManager().registerEvents(homeGUIListener, this);
        getServer().getPluginManager().registerEvents(lobbyListener, this);
        getServer().getPluginManager().registerEvents(banListener, this);
        getServer().getPluginManager().registerEvents(new FreezeListener(playerLanguageManager), this);
        getServer().getPluginManager().registerEvents(new AdminChatListener(playerLanguageManager), this);
        getServer().getPluginManager().registerEvents(new AdminUtilitiesListener(this, godCommand, vanishCommand), this);
        getServer().getPluginManager().registerEvents(muteListener, this);
        getServer().getPluginManager().registerEvents(afkManager, this);
        getServer().getPluginManager().registerEvents(muteListener, this);











        registerAPIServices();


        long elapsed = System.currentTimeMillis() - start;
        BannerUtil.printBanner(elapsed);
        saveDefaultPlaceholders();
        loadPlaceholders();


        HashMap<UUID, UUID> lastMessageMap = new HashMap<>();


        try {
            Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.adventure = BukkitAudiences.create(this);






        createPrefixFile();
        String chickensandwich = ChatColor.translateAlternateColorCodes('&',
                prefixConfig.getString("prefix", "&9&l[&bSE&9&l]&r ")
        );

        loadOfflineConfig();

        new CommandExecutor(this).runIfFirstInstall();




        saveLangFile("de.json");
        saveLangFile("fr.json");
        saveLangFile("br.json");
        saveLangFile("fi.json");
        saveLangFile("it.json");
        saveLangFile("nl.json");
        saveLangFile("pl.json");
        saveLangFile("ru.json");
        saveLangFile("es.json");
        saveLangFile("sv.json");













        saveDefaultConfig();
        saveResource("config.yml", false);


        this.startTime = System.currentTimeMillis();


        this.PlaytimeManager = new PlaytimeManager(this);
        releaseLocation = new Location(Bukkit.getWorld("world"), 0, 65, 0);

        if (!isCommandDisabled("se")) this.getCommand("se").setExecutor(new ServerEssentialsCommand(this));

        VersionChecker.checkLatestVersion(this);
        Bukkit.getPluginManager().registerEvents(new VersionNotifyJoinListener(), this);


        getServer().getPluginManager().registerEvents(new SpawnerPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(getConfig()), this);

        ServerEssentials plugin = ServerEssentials.getInstance();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ServerEssentialsPlaceholderExpansion(this).register();
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders won't work.");
        }

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent e) {}
        }, this);


        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @EventHandler
            public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                adminChatCommand.loadPlayerState(event.getPlayer().getUniqueId());
            }

            @EventHandler
            public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
                adminChatCommand.unloadPlayerState(event.getPlayer().getUniqueId());
            }
        }, this);





        File shopFolder = new File(getDataFolder(), "shop");
        new FirstJoinManager(this);


        Bukkit.getPluginManager().registerEvents(this, this);

        disableCommands();
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            }
            @org.bukkit.event.EventHandler
            public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
            }
        }, this);

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


    public static ServerEssentials getInstance() {
        return instance;
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        setLastLocation(event.getPlayer().getUniqueId(), event.getPlayer().getLocation());
    }
    public static String formatColors(String message) {
        if (message == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
    }
    public ServerEssentials() { instance = this; }
    public AuctionGUIListener getGuiManager() { return guiManager; }
    public void onDisable() {
        if (this.adventure != null) {
            this.adventure.close();
        }
        if (hooksManager != null) {
            hooksManager.cleanupHooks();
        }

        if (databaseManager != null) {
            databaseManager.closeAll();
        }
        if (magnetListener != null) {
            magnetListener.stop();
        }


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


    public BukkitAudiences adventure() {
        return this.adventure;
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

    public HooksManager getHooksManager() {
        return hooksManager;
    }


    public EconomyManager getEconomyManager() {
        return economyManager;
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

                java.util.Map<String, Command> knownCommands = (java.util.Map<String, Command>) knownCommandsField.get(simpleCommandMap);

                for (String cmdName : disabled) {
                    Command cmd = knownCommands.get(cmdName);

                    if (cmd != null) {
                        knownCommands.remove(cmdName);

                        knownCommands.remove(getNameSpaced(cmdName));

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
        if (getConfig().contains("databases.rtp")) {
            DatabaseConfig rtpDbConfig = loadDatabaseConfig("rtp");
            databaseManager.initializePool("rtp", rtpDbConfig);
        }

        if (getConfig().contains("databases.playerdata")) {
            DatabaseConfig playerDbConfig = loadDatabaseConfig("playerdata");
            databaseManager.initializePool("playerdata", playerDbConfig);
        }

        if (getConfig().contains("databases.kits")) {
            DatabaseConfig kitsDbConfig = loadDatabaseConfig("kits");
            databaseManager.initializePool("kits", kitsDbConfig);
        } else {
            databaseManager.initializePool(
                    "kits",
                    new DatabaseConfig(DatabaseType.SQLITE, "kits.db", null, 0, null, null, null, 5)
            );
        }
        databaseManager.initializePool("nicks", new DatabaseConfig(DatabaseType.SQLITE, "nicks.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("warps", new DatabaseConfig(DatabaseType.SQLITE, "warps.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("vaults", new DatabaseConfig(DatabaseType.SQLITE,"vaults.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("bans", new DatabaseConfig(DatabaseType.SQLITE, "bans.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("mutes", new DatabaseConfig(DatabaseType.SQLITE, "mutes.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("lobby", new DatabaseConfig(DatabaseType.SQLITE, "lobby.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("homes", new DatabaseConfig(DatabaseType.SQLITE, "homes.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("auction", new DatabaseConfig(DatabaseType.SQLITE, "auction.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("scoreboard", new DatabaseConfig(DatabaseType.SQLITE, "scoreboard.db", null, 0, null, null, null, 10));
        databaseManager.initializePool("rules", new DatabaseConfig(DatabaseType.SQLITE, "rules.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("mail", new DatabaseConfig(DatabaseType.SQLITE, "mail.db", null,0,null,null,null,15));
        databaseManager.initializePool("sellgui", new DatabaseConfig(DatabaseType.SQLITE, "sellgui.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("back", new DatabaseConfig(DatabaseType.SQLITE, "back.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("economy", new DatabaseConfig(DatabaseType.SQLITE, "economy.db", null, 0, null, null, null, 10));
        databaseManager.initializePool("notes", new DatabaseConfig(DatabaseType.SQLITE, "notes.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("daily", new DatabaseConfig(DatabaseType.SQLITE, "daily.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("reports", new DatabaseConfig(DatabaseType.SQLITE, "reports.db", null,0,null,null,null,15));
        databaseManager.initializePool("shop", new DatabaseConfig(DatabaseType.SQLITE, "shop.db", null, 0, null, null, null, 5));
        databaseManager.initializePool("command_data", new DatabaseConfig(DatabaseType.SQLITE, "command_data.db", null, 0, null, null, null, 10));
        databaseManager.initializePool("tpa", new DatabaseConfig(DatabaseType.SQLITE, "tpa.db", null, 0, null, null, null, 5));
    }




    private void initializeMuteSystem() {
        muteConfig = new MuteConfig(this);
        muteStorage = new MuteStorage(this, databaseManager);
        muteCommand = new MuteCommand(this, playerLanguageManager, commandDataStorage, muteStorage, muteConfig);
        unmuteCommand = new UnmuteCommand(playerLanguageManager, commandDataStorage, muteCommand);
        muteListener = new MuteListener(muteStorage, playerLanguageManager, this);
    }

    private void initializeHomesSystem() {
        homesConfig = new HomesConfig(this);
        homeStorage = new HomeStorage(databaseManager);
        homeManager = new HomeManager(homeStorage, homesConfig);
        homeGUIListener = new HomeGUIListener(this, playerLanguageManager, homesConfig, homeManager);
        homeCommand = new HomeCommand(this, playerLanguageManager, homeGUIListener, homesConfig);
        getCommand("home").setExecutor(homeCommand);

    }
    private void initializeAuctionSystem() {
        auctionConfig = new AuctionConfig(this);
        auctionStorage = new AuctionStorage(databaseManager);
        auctionGUIListener = new AuctionGUIListener(this, playerLanguageManager, auctionConfig, auctionStorage, economyManager);
        auctionCommand = new AuctionCommand(this, playerLanguageManager, auctionConfig, auctionStorage, auctionGUIListener, economyManager);
        auctionAPI = new AuctionAPIImpl(this, auctionConfig, auctionGUIListener, auctionStorage, economyManager);
        getLogger().info("Auction system initialized successfully");
    }
    private void initializeScoreboardSystem() {
        this.scoreboardConfig = new ScoreboardConfig(this);
        this.scoreboardStorage = new ScoreboardStorage(this, databaseManager);
        this.scoreboardUpdater = new ScoreboardUpdater(this, scoreboardConfig, scoreboardStorage);
        this.scoreboardListener = new ScoreboardListener(this ,playerLanguageManager, scoreboardConfig, scoreboardStorage, scoreboardUpdater);
        this.scoreboardCommand = new ScoreboardCommand(this, playerLanguageManager, scoreboardConfig, scoreboardStorage, scoreboardUpdater);

        getCommand("scoreboard").setExecutor(scoreboardCommand);
        getServer().getPluginManager().registerEvents(scoreboardListener, this);

        getLogger().info("Scoreboard system initialized with " + scoreboardConfig.layouts.size() + " layouts");
    }

    private void initializeRulesSystem() {
        this.rulesConfig = new RulesConfig(this);
        this.rulesStorage = new RulesStorage(this, databaseManager);
        this.rulesGUI = new RulesGUI(playerLanguageManager, rulesStorage, rulesConfig, this);

        this.rulesListener = new RulesListener(playerLanguageManager, rulesStorage, rulesConfig, this);
        this.rulesCommand = new RulesCommand(playerLanguageManager, rulesConfig, rulesStorage, this);


    }

    private void initializeNotesSystem() {
        this.notesConfig = new NotesConfig(this);
        this.notesStorage = new NotesStorage(this, databaseManager);

        this.notesCommand = new NotesCommand(playerLanguageManager, notesConfig, notesStorage);

        getCommand("notes").setExecutor(notesCommand);
    }

    private void initializeLobbySystem() {
        lobbyConfig = new LobbyConfig(this);
        lobbyStorage = new LobbyStorage(this, databaseManager);
        lobbyListener = new LobbyListener(playerLanguageManager, lobbyStorage, lobbyConfig);
        lobbyCommand = new LobbyCommand(this, playerLanguageManager, commandDataStorage, lobbyStorage, lobbyConfig);
    }

    private void initializeDailySystem() {
        dailyConfig = new DailyConfig(this);
        dailyStorage = new DailyStorage(this, databaseManager);
        dailyListener = new DailyListener(this, playerLanguageManager, dailyStorage, dailyConfig);
        dailyCommand = new DailyCommand(this, playerLanguageManager, dailyStorage, dailyConfig, dailyListener);
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

    private String getNameSpaced(String command) {
        return getName().toLowerCase() + ":" + command.toLowerCase();
    }




    private void initializeKitSystem() {
        kitConfig = new KitConfig(this);

        kitStorage = new KitStorage(this, databaseManager, "kits");

        kitGuiListener = new KitGUIListener(this, playerLanguageManager, kitStorage, kitConfig);

        kitCommand = new KitCommand(this, playerLanguageManager, kitStorage, kitConfig, kitGuiListener);
        getCommand("kit").setExecutor(kitCommand);

        getServer().getPluginManager().registerEvents(kitGuiListener, this);

        KitConfigManager.setup(this);
        KitManager.loadKits(KitConfigManager.getConfig());

        getLogger().info("Kit system initialized with " + KitManager.getKits().size() + " kits");
    }




    private void initializeCommandModules() {
        commandDataStorage = new CommandDataStorage(this, databaseManager);

        flyCommand = new FlyCommand(this, playerLanguageManager, commandDataStorage);
        flyListener = new FlyListener(flyCommand);
    }


    private void initializeRtpSystem() {
        RtpConfig rtpConfig = new RtpConfig(this);
        rtpLocationStorage = new RtpLocationStorage(this, databaseManager, "rtp");
        CooldownManager cooldownManager = new CooldownManager();

        getCommand("rtp").setExecutor(new RtpCommand(this, playerLanguageManager, rtpConfig));
        getServer().getPluginManager().registerEvents(
                new RtpListener(this, playerLanguageManager, rtpLocationStorage, cooldownManager, backManager, rtpConfig), this);
    }



    private final String[] defaultShopFiles = {
            "food.yml",
            "main.yml",
            "blocks.yml",
            "coloredblocks.yml",
            "tools.yml",
            "ores.yml",
            "redstone.yml",
            "decoration.yml",
            "mobs.yml",
            "farming.yml",
            "misc.yml",
            "customsection5",
            "customsection4",
            "customsection3",
            "customsection2",
            "customsection1",




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

    private void initializeSellGuiSystem() {
        sellConfig = new SellConfig(this);
        sellStorage = new SellStorage(this, databaseManager);
        sellGuiManager = new SellGUIManager(this, playerLanguageManager, sellStorage, sellConfig, economyManager);
        sellCommand = new SellCommand(this, playerLanguageManager, sellConfig, sellGuiManager, economyManager);
        sellGuiListener = new SellGUIListener(playerLanguageManager, sellGuiManager);

        getCommand("sell").setExecutor(sellCommand);
        getServer().getPluginManager().registerEvents(sellGuiListener, this);

        getLogger().info("SellGUI system initialized with " + sellConfig.getSellableMaterialsCount() + " items");
    }

    private void initializeSessionSystem() {
        this.sessionManager = new SessionManager(databaseManager, commandDataStorage);

        sessionManager.initialize();

        this.sessionConfig = new SessionConfig(this);
        this.sessionCommand = new SessionCommand(
                playerLanguageManager,
                sessionConfig,
                sessionManager,
                commandDataStorage
        );

        this.sessionListener = new SessionListener(sessionManager);
        getServer().getPluginManager().registerEvents(sessionListener, this);

        getCommand("session").setExecutor(sessionCommand);

        getLogger().info("Session system initialized");
    }

    public static class SessionListener implements org.bukkit.event.Listener {
        private final SessionManager sessionManager;

        public SessionListener(SessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @org.bukkit.event.EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            sessionManager.startSession(event.getPlayer());
        }

        @org.bukkit.event.EventHandler
        public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
            sessionManager.endSession(event.getPlayer());
        }
    }

    private void initializeBanSystem() {
        banConfig = new BanConfig(this);
        banStorage = new BanStorage(this, databaseManager);
        banListener = new BanListener(banStorage, playerLanguageManager, this);
        banCommand = new BanCommand(this, playerLanguageManager, commandDataStorage, banStorage, banConfig);
        banListCommand = new BanListCommand(playerLanguageManager, commandDataStorage, banStorage);
    }
    private void initializeBackSystem() {
        this.backConfig = new BackConfig(this);
        this.backDataStorage = new BackDataStorage(this, databaseManager);
        this.backManager = new BackManager(backDataStorage);
        this.backListener = new BackListener(backManager, backConfig);
        this.backCommand = new BackCommand(
                playerLanguageManager,
                backConfig,
                backManager,
                commandDataStorage,
                this
        );

        getCommand("back").setExecutor(backCommand);
        getCommand("back").setTabCompleter(backCommand);
        getServer().getPluginManager().registerEvents(backListener, this);

    }



    private void initializeWarpSystem() {
        warpConfig = new WarpConfig(this);
        warpStorage = new WarpStorage(this, databaseManager);
        warpManager = new WarpManager(warpStorage, warpConfig, playerLanguageManager);
        warpCommand = new WarpTeleportCommand(this,warpManager, playerLanguageManager, commandDataStorage);
        setwarpCommand = new WarpSetCommand(warpManager, playerLanguageManager, commandDataStorage);
        delwarpCommand = new WarpDeleteCommand(warpManager, playerLanguageManager, commandDataStorage);
        warpsCommand = new WarpsCommand(warpManager, playerLanguageManager, commandDataStorage);
    }

    private void saveLangFile(String fileName) {
        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File outFile = new File(langFolder, fileName);

        if (!outFile.exists()) {
            saveResource("lang/" + fileName, false);
            getLogger().info("Saved default language file: " + fileName);
        } else {
            getLogger().info("Language file already exists: " + fileName);
        }
    }


    private void initializeNickSystem() {
        nickConfig = new NickConfig(this);
        nickStorage = new NickStorage(this, databaseManager, "nicks");
        nickManager = new NickManager(this, nickStorage);
        nickCommand = new NickCommand(playerLanguageManager, nickConfig, nickStorage, nickManager);
        getServer().getPluginManager().registerEvents(new NickListener(nickManager), this);

    }

    public HomeManager getHomeManager() {
        return homeManager;
    }



    private void initializeShopSystem() {
        shopConfig = new ShopConfig(this);
        shopDataManager = new net.godlycow.org.economy.shop.ShopDataManager(this, databaseManager);

        net.godlycow.org.economy.shop.storage.ShopStorage storage =
                new net.godlycow.org.economy.shop.storage.ShopStorage(this, databaseManager);

        shopGuiManager = new net.godlycow.org.economy.shop.gui.ShopGUIManager(
                this,
                playerLanguageManager,
                storage,
                shopConfig,
                economyManager,
                shopDataManager
        );

        shopAPI = new net.godlycow.org.economy.shop.api.ShopAPIImpl(shopConfig, shopGuiManager, shopDataManager);

        shopCommand = new ShopCommand(
                this,
                playerLanguageManager,
                databaseManager,
                shopConfig,
                economyManager,
                shopAPI,
                shopGuiManager,
                shopDataManager
        );

        getCommand("shop").setExecutor(shopCommand);
        getServer().getPluginManager().registerEvents(new ShopGUIListener(shopGuiManager), this);
    }

    private void saveResourceFolder(String folderPath) {
        try {
            var jar = getClass().getProtectionDomain().getCodeSource().getLocation();
            try (var zip = new java.util.zip.ZipFile(jar.getPath())) {
                for (var entry : java.util.Collections.list(zip.entries())) {
                    String name = entry.getName();

                    if (name.startsWith(folderPath + "/") && !entry.isDirectory()) {

                        File outFile = new File(getDataFolder(), name);

                        if (outFile.exists()) continue;

                        outFile.getParentFile().mkdirs();

                        try (var in = zip.getInputStream(entry);
                             var out = new java.io.FileOutputStream(outFile)) {

                            in.transferTo(out);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    private void registerAPIServices() {
        if (shopAPI == null || auctionAPI == null) {
            getLogger().severe("FAILED TO REGISTER API: API instances are null!");
            return;
        }

        getServer().getServicesManager().register(
                com.serveressentials.api.shop.ShopAPI.class,
                shopAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        getServer().getServicesManager().register(
                com.serveressentials.api.auction.AuctionAPI.class,
                auctionAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.afkAPI = new AFKAPIImpl(this, afkManager);
        getServer().getServicesManager().register(
                com.serveressentials.api.afk.AFKAPI.class,
                afkAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.backAPI = new net.godlycow.org.back.api.BackAPIImpl(
                this, backManager, backConfig, backDataStorage, playerLanguageManager, commandDataStorage
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.back.BackAPI.class,
                backAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.dailyAPI = new DailyAPIImpl(
                this, dailyConfig, dailyStorage, playerLanguageManager
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.daily.DailyAPI.class,
                dailyAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.economyAPI = new EconomyAPIImpl(this, economyManager);
        getServer().getServicesManager().register(
                com.serveressentials.api.economy.EconomyAPI.class,
                economyAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.kitAPI = new KitAPIImpl(
                this,
                kitManager,
                kitConfigManager,
                kitStorage,
                kitConfig,
                playerLanguageManager
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.kit.KitAPI.class,
                kitAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.lobbyAPI = new LobbyAPIImpl(
                this, lobbyStorage, lobbyConfig
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.lobby.LobbyAPI.class,
                lobbyAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.mailAPI = new MailAPIImpl(
                this, mailStorage, mailConfig, mailListener
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.mail.MailAPI.class,
                mailAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.nickAPI = new NickAPIImpl(
                this, nickStorage, nickConfig, nickManager
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.nick.NickAPI.class,
                nickAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.reportAPI = new ReportAPIImpl(
                this, reportStorage, reportConfig, reportCommand
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.report.ReportAPI.class,
                reportAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.rtpAPI = new RtpAPIImpl(this, rtpLocationStorage, rtpConfig, rtpListener, playerLanguageManager);
        getServer().getServicesManager().register(
                com.serveressentials.api.rtp.RtpAPI.class,
                rtpAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.scoreboardAPI = new ScoreboardAPIImpl(
                this, scoreboardConfig, scoreboardStorage, scoreboardUpdater
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.scoreboard.ScoreboardAPI.class,
                scoreboardAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        com.serveressentials.api.PluginAPI pluginAPI = new PluginAPIImpl(
                this, shopAPI, homeManager, auctionAPI, afkManager, backAPI,
                dailyAPI, economyAPI, kitAPI, lobbyAPI, mailAPI, nickAPI, reportAPI, rtpAPI,
                scoreboardAPI, sellguiAPI, tpaAPI, warpAPI
        );


        this.warpAPI = new WarpAPIImpl(
                this, warpConfig, warpManager
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.warp.WarpAPI.class,
                warpAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );


        this.sellguiAPI = new SellGUIAPIImpl(
                this, sellConfig, sellStorage, sellGUIManager, economyManager
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.sellgui.SellGUIAPI.class,
                sellguiAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        this.tpaAPI = new TPAAPIImpl(
                this, tpaConfig, tpaStorage, tpaListener
        );
        getServer().getServicesManager().register(
                com.serveressentials.api.tpa.TPAAPI.class,
                tpaAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        getServer().getServicesManager().register(
                com.serveressentials.api.PluginAPI.class,
                pluginAPI,
                this,
                org.bukkit.plugin.ServicePriority.High
        );

        getLogger().info("Successfully registered all API services!");
    }



}