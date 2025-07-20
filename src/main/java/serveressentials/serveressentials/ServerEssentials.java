package serveressentials.serveressentials;


import me.clip.placeholderapi.libs.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import serveressentials.serveressentials.DailyRewards;




import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ServerEssentials extends JavaPlugin implements Listener {

    private static ServerEssentials instance;
    private final AuctionManager auctionManager = new AuctionManager();
    private static final Map<UUID, Double> balances = new HashMap<>();
    private Listener DailyRewardGUI;
    private RankManager rankManager;
    private RulesManager rulesManager;
    BanManager banManager = new BanManager(this);
    private SimpleScoreboard simpleScoreboard;
    private SellGUIListener sellGUIListener;
    private long startTime;
    private LockdownCommand lockdownCommand;
    private String originalMotd;
    private final HashMap<UUID, UUID> lastMessageMap = new HashMap<>();
    private final HashMap<UUID, BukkitRunnable> jailReleaseTasks = new HashMap<>();
    private Location jailLocation;
    private Location releaseLocation;
    private File prefixFile;
    private FileConfiguration prefixConfig;
    private File starterMoneyFile;
    private FileConfiguration starterMoneyConfig;
    private PlaytimeManager PlaytimeManager;
    private BukkitAudiences adventure;
    private DailyRewards dailyRewards;




    @Override
    public void onEnable() {
        instance = this;
        this.adventure = BukkitAudiences.create(this);

        ShopManager.loadShopItems();
        createPrefixFile();



        String prefix = ChatColor.translateAlternateColorCodes('&',
                prefixConfig.getString("prefix", "&9&l[&bSE&9&l]&r ")
        );
        HomeManager.loadHomes();
        LobbyManager.setup();

        LobbyManager.loadLobby();
        createStarterMoneyConfig();
        WarpManager.setup();
        ShopManager.clearItems();
        WarpManager.setup();
        ShopConfigManager.loadShopItems();
        ReportCommand reportCommand = new ReportCommand(this);
        JoinLeaveManager.load();
        RTPConfig.load();
        EconomyManager.loadBalances();
        simpleScoreboard = new SimpleScoreboard(this);
        this.startTime = System.currentTimeMillis();
        originalMotd = Bukkit.getMotd();
        lockdownCommand = new LockdownCommand(this, originalMotd);
        new TopPlaytimePlaceholder(this).register();
        this.PlaytimeManager = new PlaytimeManager(this);
        this.dailyRewards = new DailyRewards(this);
        KitConfigManager.setup(this);
        KitManager.loadKits(KitConfigManager.getConfig());



        KitConfigManager.setup(this);
        KitCommand kitCommand = new KitCommand();
        rankManager = new RankManager(this);
        VaultManager vaultManager = new VaultManager(this);
        KillTracker killTracker = new KillTracker(this);
        rulesManager = new RulesManager(this);
        ConsoleCommandManager commandManager = new ConsoleCommandManager(this);
        jailLocation = new Location(Bukkit.getWorld("world"), 100, 65, 100);
        releaseLocation = new Location(Bukkit.getWorld("world"), 0, 65, 0);
        getServer().getPluginManager().registerEvents(new JoinCommandListener(commandManager), this);
        DeleteHomeCommand DeleteHomeCommand = new DeleteHomeCommand();


        VersionChecker.checkLatestVersion(this);


        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                VersionChecker.notifyIfOutdated(event.getPlayer());
            }
        }, this);


        Bukkit.getOnlinePlayers().forEach(SimpleScoreboard::setScoreboard);


        sellGUIListener = new SellGUIListener();
        getServer().getPluginManager().registerEvents(sellGUIListener, this);
        getServer().getPluginManager().registerEvents(new RTPListener(), this);
        getServer().getPluginManager().registerEvents(new AdminChatListener(), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(), this);
        getServer().getPluginManager().registerEvents(new KitGUIListener(kitCommand), this);
        getServer().getPluginManager().registerEvents(new ReportNotifierListener(reportCommand), this);
        getServer().getPluginManager().registerEvents(new RankListener(rankManager), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);;
        getServer().getPluginManager().registerEvents(new WarpInventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new WarpInventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnerPlaceListener(this), this);

        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(this, this); // onJoin listener
        getServer().getPluginManager().registerEvents(new BanListener(banManager), this);
        Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);


        Bukkit.getPluginManager().registerEvents(new HomeGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new ShopGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new SellGUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new AuctionListener(auctionManager), this);



        AdminUtilitiesCommand adminUtils = new AdminUtilitiesCommand();
        getServer().getPluginManager().registerEvents(new AdminUtilitiesListener(adminUtils), this);


        DailyRewardGUI dailyRewardCommand = new DailyRewardGUI();


        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ServerEssentialsPlaceholder(this).register();
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders won't work.");
        }


        getCommand("serveressentials").setExecutor(new VersionCommand());
        getCommand("sellgui").setExecutor(new SellGUICommand());
        getCommand("rtp").setExecutor(new RTPCommand());
        this.getCommand("glow").setExecutor(new GlowCommand());
        this.getCommand("reply").setExecutor(new ReplyCommand(lastMessageMap));
        getCommand("eco").setExecutor(new EcoCommand());
        getCommand("ban").setExecutor(new BanCommand(getBanManager()));
        getCommand("unban").setExecutor(new UnbanCommand(getBanManager()));
        getCommand("deletehome").setTabCompleter(DeleteHomeCommand);

        getCommand("rank").setExecutor(new RankCommand(rankManager));
        this.getCommand("unloadworld").setExecutor(new UnloadWorldCommand());
        this.getCommand("loadworld").setExecutor(new LoadWorldCommand());
        getCommand("worldlist").setExecutor(new WorldListCommand());
        getCommand("tpp").setExecutor(new TeleportWorldCommand());
        getCommand("pv").setExecutor(new VaultCommand(vaultManager));
        this.getCommand("lockdown").setExecutor(lockdownCommand);
        getServer().getPluginManager().registerEvents(lockdownCommand, this);
        getCommand("uptime").setExecutor(new UptimeCommand(startTime));
        getCommand("session").setExecutor(new SessionCommand());

        getCommand("kit").setExecutor(kitCommand);
        getCommand("report").setExecutor(reportCommand);
        getCommand("kills").setExecutor(new KillCommand(killTracker));
        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("balancetop").setExecutor(new BalanceTopCommand());
        getCommand("shop").setExecutor(new ShopCommand());
        getCommand("playtime").setExecutor(new PlaytimeCommand());
        getCommand("vanish").setExecutor(adminUtils);
        getCommand("god").setExecutor(adminUtils);
        getCommand("invsee").setExecutor(adminUtils);

        getCommand("daily").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof org.bukkit.entity.Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            if (!player.hasPermission("daily.use")) {
                player.sendMessage("§cYou don't have permission.");
                return true;
            }

            dailyRewards.openRewardsGUI(player, 1);
            return true;
        });

        getCommand("rules").setExecutor(new RulesCommand(rulesManager));
        getCommand("invclear").setExecutor(adminUtils);

        getCommand("tp").setExecutor(adminUtils);
        getCommand("tpa").setExecutor(new TpaCommand());
        getCommand("tpaccept").setExecutor(new TpAcceptCommand());
        getCommand("tpdeny").setExecutor(new TpDenyCommand());
        getCommand("tpall").setExecutor(new TpAllCommand());
        getCommand("tphere").setExecutor(new TpCommandHere());
        getCommand("warps").setExecutor(new WarpsCommand());
        getCommand("setwarpcategory").setExecutor(new SetWarpCategoryCommand());
        getCommand("setwarpmaterial").setExecutor(new SetWarpMaterialCommand());

        PluginCommand cmd = getCommand("setwarpdescription");
        if (cmd != null) {
            cmd.setExecutor(new SetWarpDescriptionCommand());
        }

        getCommand("ah").setExecutor(new AuctionCommand(auctionManager));
        getCommand("adminchat").setExecutor(new AdminChatCommand());
        getCommand("sethome").setExecutor(new SetHomeCommand());
        getCommand("deletehome").setExecutor(new DeleteHomeCommand());
        getCommand("renamehome").setExecutor(new RenameHomeCommand());
        getCommand("broadcast").setExecutor(new BroadcastCommand());
        getCommand("homes").setExecutor(new HomeCommand());
        getCommand("nick").setExecutor(new NickCommand());
        getCommand("rename").setExecutor(new RenameItemCommand());
        getCommand("setloreline").setExecutor(new SetLoreLineCommand());
        getCommand("ping").setExecutor(new PingCommand());
        getCommand("setlobby").setExecutor(new SetLobbyCommand());
        getCommand("lobby").setExecutor(new LobbyCommand());
        getCommand("removelobby").setExecutor(new RemoveLobbyCommand());
        getCommand("back").setExecutor(new BackCommand());
        getCommand("death").setExecutor(new DeathCommand());
        getCommand("setmotd").setExecutor(new MotdCommand(this));
        getCommand("heal").setExecutor(new HealCommand());
        getCommand("feed").setExecutor(new FeedCommand());
        getCommand("ptime").setExecutor(new PTimeCommand());
        getCommand("playerinfo").setExecutor(new PlayerInfoCommand());
        getCommand("launch").setExecutor(new LaunchCommand());
        getCommand("gravity").setExecutor(new GravityCommand());
        getCommand("repair").setExecutor(new RepairCommand());
        getCommand("workbench").setExecutor(new WorkbenchCommand());
        getCommand("enderchest").setExecutor(new EnderChestCommand());
        getCommand("endersee").setExecutor(new EnderSeeCommand());
        getCommand("mute").setExecutor(new MuteCommand());
        getCommand("unmute").setExecutor(new UnmuteCommand());
        getCommand("night").setExecutor(new NightCommand());
        getCommand("day").setExecutor(new DayCommand());
        getCommand("weather").setExecutor(new WeatherCommand());
        getCommand("mail").setExecutor(new MailCommand());
        getCommand("kickall").setExecutor(new KickAllCommand());
        getCommand("suicide").setExecutor(new SuicideCommand());
        getCommand("setwarp").setExecutor(new SetWarpCommand());
        getCommand("alts").setExecutor(new AltsCommand(this));
        getCommand("restart").setExecutor(new RestartCommand(this));
        getCommand("warp").setExecutor(new WarpCommand());
        getCommand("warps").setExecutor(new WarpsCommand());
        getCommand("renamewarp").setExecutor(new RenameWarpCommand());
        getCommand("closewarp").setExecutor(new CloseWarpCommand());
        getCommand("gamemode").setExecutor(new GamemodeCommand());
        getCommand("reopenwarp").setExecutor(new ReOpenWarpCommand());
        getCommand("deletewarp").setExecutor(new DeleteWarpCommand());
        getCommand("disposal").setExecutor(new DisposalCommand());
        getCommand("top").setExecutor(new TopCommand());
        getCommand("bottom").setExecutor(new BottomCommand());
        getCommand("clearchat").setExecutor(new ClearChatCommand());
        getCommand("tpoffline").setExecutor(new TPOfflineCommand());
        getCommand("craft").setExecutor(new CraftCommand());
        getCommand("fly").setExecutor(new FlyCommand());
        getCommand("speed").setExecutor(new SpeedCommand());
        getCommand("swap").setExecutor(new SwapCommand());
        getCommand("sleep").setExecutor(new SleepCommand());
        getCommand("magnet").setExecutor(new MagnetCommand(this));
        getCommand("notes").setExecutor(new NotesCommand());
        getCommand("fakeop").setExecutor(new FaekOPCommand());
        getCommand("hat").setExecutor(new HatCommand());
        getCommand("realname").setExecutor(new RealNameCommand());
        getCommand("freeze").setExecutor(new FreezeCommand());
        getServer().getPluginManager().registerEvents(new ToolMoveFixListener(this), this);

        getCommand("iteminfo").setExecutor(new ItemInfoCommand());
        getCommand("spy").setExecutor(new SpyCommand());
        this.getCommand("pingall").setExecutor(new PingAllCommand());
        getCommand("Unfreeze").setExecutor(new UnfreezeCommand());
        getCommand("World").setExecutor(new WorldCommand());
        getCommand("Break").setExecutor(new BreakCommand());
        getCommand("Compass").setExecutor(new CompassCommand());
        getCommand("Pay").setExecutor(new PayCommand());
        getCommand("coinflip").setExecutor(new CoinFlipCommand());
        getCommand("scoreboard").setExecutor(new ScoreboardCommand(simpleScoreboard));
        getCommand("stafflist").setExecutor(new StaffListCommand());
        getCommand("track").setExecutor(new TrackCommand());
        getCommand("burn").setExecutor(new BurnCommand());
        getCommand("celebrate").setExecutor(new CelebrateCommand());
        this.getCommand("serverinfo").setExecutor(new ServerInfoCommand(this));
        this.getCommand("banlist").setExecutor(new BanListCommand(banManager));


        getServer().getPluginManager().registerEvents(new GamemodeGUI(), this);
        Bukkit.getPluginManager().registerEvents(new MotdListener(this), this);
        getServer().getPluginManager().registerEvents(new WarpInventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new WarpGUIListener(), this);
        getServer().getPluginManager().registerEvents(new HexColorChatListener(), this);






        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                SessionCommand.startSession(event.getPlayer());
            }

            @org.bukkit.event.EventHandler
            public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
                SessionCommand.endSession(event.getPlayer());
            }
        }, this);



        for (Player online : Bukkit.getOnlinePlayers()) {
            simpleScoreboard.setScoreboard(online);
        }


        getLogger().info(" ____                           _____                    _   _       _     ");
        getLogger().info("/ ___|  ___ _ ____   _____ _ __| ____|___ ___  ___ _ __ | |_(_) __ _| |___ ");
        getLogger().info("\\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|  _| / __/ __|/ _ \\ '_ \\| __| |/ _` | / __|");
        getLogger().info(" ___) |  __/ |   \\ V /  __/ |  | |___\\__ \\__ \\  __/ | | | |_| | (_| | \\__ \\");
        getLogger().info("|____/ \\___|_|    \\_/ \\___|_|  |_____|___/___/\\___|_| |_|\\__|_|\\__,_|_|___/");
        getLogger().info("Plugin enabled successfully!");
        Bukkit.getLogger().severe("[ServerEssentials] Buy me a Pizza ❤️  [https://www.patreon.com/c/ADuckPlayingMC]");


    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();


        simpleScoreboard.setScoreboard(player);


        String ip = player.getAddress().getAddress().getHostAddress();
        File altsFile = new File(getDataFolder(), "alts.yml");
        FileConfiguration altsConfig = YamlConfiguration.loadConfiguration(altsFile);

        List<String> names = altsConfig.getStringList("IPs." + ip);
        if (!names.contains(player.getName())) {
            names.add(player.getName());
            altsConfig.set("IPs." + ip, names);
            try {
                altsConfig.save(altsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (MuteCommand.mutedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou are muted.");
        }
    }



    public static ServerEssentials getInstance() {
        return instance;
    }
    public SellGUIListener getSellGUIListener() {
        return sellGUIListener;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public SimpleScoreboard getScoreboard() {
        return simpleScoreboard;
    }
    public void onServerListPing(ServerListPingEvent event) {

        if (lockdownCommand.isLockdownActive()) {
            event.setMotd("§4§lLOCKDOWN MODE");
        } else {
            event.setMotd(originalMotd);
        }
    }
    private void createPrefixFile() {
        prefixFile = new File(getDataFolder(), "prefix.yml");

        if (!prefixFile.exists()) {
            prefixFile.getParentFile().mkdirs();
            saveResource("prefix.yml", false);
        }

        prefixConfig = YamlConfiguration.loadConfiguration(prefixFile);
    }

    public void createStarterMoneyConfig() {
        starterMoneyFile = new File(getDataFolder(), "startermoney.yml");
        if (!starterMoneyFile.exists()) {
            starterMoneyFile.getParentFile().mkdirs();
            saveResource("startermoney.yml", false);
        }

        starterMoneyConfig = YamlConfiguration.loadConfiguration(starterMoneyFile);
    }
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
        }
    }

    public BukkitAudiences getAdventure() {
        return adventure;
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
    
    public void addBalance(UUID uuid, double amount) {
        EconomyManager.addBalance(uuid, amount);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            simpleScoreboard.setScoreboard(player);
        }
    }
}
