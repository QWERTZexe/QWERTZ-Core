/*
        Copyright (C) 2024 QWERTZ_EXE

        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License
        as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
        without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
        See the GNU Affero General Public License for more details.

        You should have received a copy of the GNU Affero General Public License along with this program.
        If not, see <http://www.gnu.org/licenses/>.
*/

package app.qwertz.qwertzcore.commands;

import app.qwertz.qwertzcore.QWERTZcore;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordCommand implements CommandExecutor {

    private final QWERTZcore plugin;

    public DiscordCommand(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String discordLink = plugin.getConfigManager().getDiscordLink();

        if (discordLink == null || discordLink.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Discord link is not set in the configuration.");
            return true;
        }

        TextComponent message = new TextComponent(QWERTZcore.CORE_ICON + " ");

        TextComponent clickHere = new TextComponent("CLICK HERE TO JOIN OUR DISCORD!");
        clickHere.setColor(ChatColor.AQUA);
        clickHere.setBold(true);
        clickHere.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordLink));
        clickHere.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click to join our Discord server!").create()));

        message.addExtra(clickHere);

        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(message);
        } else {
            sender.sendMessage(ChatColor.AQUA + "Discord link: " + discordLink);
        }

        return true;
    }
}