/*
 * DiscordSRV - A Minecraft to Discord and back link plugin
 * Copyright (C) 2016-2017 Austin Shapiro AKA Scarsz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package github.scarsz.discordsrv.hooks.chat;

import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.channel.ChannelPlayer;
import com.github.ucchyocean.lc.event.LunaChatChannelChatEvent;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.LangUtil;
import github.scarsz.discordsrv.util.PlayerUtil;
import github.scarsz.discordsrv.util.PluginUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.stream.Collectors;

public class LunaChatHook implements Listener {

    public LunaChatHook() {
        PluginUtil.pluginHookIsEnabled("lunachat");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMessage(LunaChatChannelChatEvent event) {
        // make sure chat channel is registered with a destination
        if (DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(event.getChannel().getName()) == null) return;

        // make sure message isn't just blank
        if (StringUtils.isBlank(event.getNgMaskedMessage())) return;

        // get sender player
        Player player = (event.getPlayer() != null) ? event.getPlayer().getPlayer() : null;

        DiscordSRV.getPlugin().processChatMessage(player, event.getNgMaskedMessage(), event.getChannel().getName(), false);
    }

    public static void broadcastMessageToChannel(String channel, String message) {
        Channel chatChannel = LunaChat.getInstance().getLunaChatAPI().getChannel(channel);
        if (chatChannel == null) return; // no suitable channel found
        chatChannel.sendMessage(null, "", ChatColor.translateAlternateColorCodes('&', LangUtil.Message.CHAT_CHANNEL_MESSAGE.toString()
                .replace("%channelcolor%", chatChannel.getColorCode())
                .replace("%channelname%", chatChannel.getName())
                .replace("%channelnickname%", (chatChannel.getAlias().equals("")) ? chatChannel.getName() : chatChannel.getAlias() )
                .replace("%message%", message)
        ), true, "Discord");

        PlayerUtil.notifyPlayersOfMentions(player ->
                        chatChannel.getMembers().stream()
                                .map(ChannelPlayer::getPlayer)
                                .collect(Collectors.toList())
                                .contains(player),
                message);
    }

}
