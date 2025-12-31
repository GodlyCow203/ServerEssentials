package com.serveressentials.api;

import com.serveressentials.api.afk.AFKAPI;
import com.serveressentials.api.auction.AuctionAPI;
import com.serveressentials.api.back.BackAPI;
import com.serveressentials.api.daily.DailyAPI;
import com.serveressentials.api.economy.EconomyAPI;
import com.serveressentials.api.home.HomeAPI;
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


public interface PluginAPI {
    String getVersion();
    boolean isEnabled();
    ShopAPI getShopAPI();
    HomeAPI getHomeAPI();
    AuctionAPI getAuctionAPI();
    AFKAPI getAFKAPI();
    BackAPI getBackAPI();
    DailyAPI getDailyAPI();
    EconomyAPI getEconomyAPI();
    KitAPI getKitAPI();
    LobbyAPI getLobbyAPI();
    MailAPI getMailAPI();
    NickAPI getNickAPI();
    ReportAPI getReportAPI();
    RtpAPI getRtpAPI();
    ScoreboardAPI getScoreboardAPI();
    SellGUIAPI getsellguiAPI();
    TPAAPI getTpaAPI();
    WarpAPI getWarpAPI();

}