package net.godlycow.org.api;

import com.serveressentials.api.PluginAPI;
import com.serveressentials.api.afk.AFKAPI;
import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.home.HomeAPI;
import com.serveressentials.api.lobby.LobbyAPI;
import com.serveressentials.api.mail.MailAPI;
import com.serveressentials.api.nick.NickAPI;
import com.serveressentials.api.report.ReportAPI;
import com.serveressentials.api.rtp.RtpAPI;
import com.serveressentials.api.shop.ShopAPI;
import com.serveressentials.api.kit.KitAPI;
import net.godlycow.org.ServerEssentials;
import net.godlycow.org.afk.AFKManager;
import net.godlycow.org.afk.api.AFKAPIImpl;
import net.godlycow.org.auction.api.AuctionAPIImpl;
import net.godlycow.org.homes.HomeManager;

public class PluginAPIImpl implements PluginAPI {
    private final ServerEssentials plugin;
    private final ShopAPI shopAPI;
    private final HomeAPI homeAPI;
    private final AuctionAPI auctionAPI;
    private final AFKAPI afkAPI;
    private final BackAPI backAPI;
    private final DailyAPI dailyAPI;
    private final EconomyAPI economyAPI;
    private final KitAPI kitAPI;
    private final LobbyAPI lobbyAPI;
    private final MailAPI mailAPI;
    private final NickAPI nickAPI;
    private final ReportAPI reportAPI;
    private final RtpAPI rtpAPI;




    public PluginAPIImpl(ServerEssentials plugin, ShopAPI shopAPI,
                         HomeManager homeManager, AuctionAPI auctionAPI,
                         AFKManager afkManager, BackAPI backAPI, DailyAPI dailyAPI, EconomyAPI economyAPI, KitAPI kitAPI, LobbyAPI lobbyAPI, MailAPI mailAPI, NickAPI nickAPI, ReportAPI reportAPI, RtpAPI rtpAPI) {
        this.plugin = plugin;
        this.shopAPI = shopAPI;
        this.homeAPI = new net.godlycow.org.homes.api.HomeAPIImpl(homeManager);
        this.auctionAPI = auctionAPI;
        this.afkAPI = new AFKAPIImpl(plugin, afkManager);
        this.backAPI = backAPI;
        this.dailyAPI = dailyAPI;
        this.economyAPI = economyAPI;
        this.kitAPI = kitAPI;
        this.lobbyAPI = lobbyAPI;
        this.mailAPI = mailAPI;
        this.nickAPI = nickAPI;
        this.reportAPI = reportAPI;
        this.rtpAPI = rtpAPI;


    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public ShopAPI getShopAPI() {
        return shopAPI;
    }

    @Override
    public HomeAPI getHomeAPI() {
        return homeAPI;
    }
    public DailyAPI getDailyAPI() {
        return dailyAPI;
    }

    public EconomyAPI getEconomyAPI() {
        return economyAPI;
    }

    public  KitAPI getKitAPI() {
        return kitAPI;
    }

    public LobbyAPI getLobbyAPI() {
        return lobbyAPI;
    }

    @Override
    public AuctionAPI getAuctionAPI() {
        return auctionAPI;
    }

    @Override
    public AFKAPI getAFKAPI() {
        return afkAPI;
    }

    public BackAPI getBackAPI() {
        return backAPI;
    }
    public MailAPI getMailAPI() {
        return mailAPI;
    }
    public NickAPI getNickAPI() {
        return nickAPI;
    }

    public ReportAPI getReportAPI() {
        return reportAPI;
    }

    public RtpAPI getRtpAPI(){
        return rtpAPI;
    }

}