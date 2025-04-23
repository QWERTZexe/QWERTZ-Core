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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PollVoteCommand implements CommandExecutor {

    private final QWERTZcore plugin;
    private final PollCommand pollCommand;

    public PollVoteCommand(QWERTZcore plugin, PollCommand pollCommand) {
        this.plugin = plugin;
        this.pollCommand = pollCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getMessageManager().sendMessage(sender, "general.only-player-execute");
            return true;
        }

        if (args.length != 1) {
            plugin.getMessageManager().sendInvalidUsage(sender, "/pollvote <option>");
            plugin.getSoundManager().playSoundToSender(sender);
            return true;
        }

        try {
            int option = Integer.parseInt(args[0]);
            pollCommand.vote((Player) sender, option);
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendMessage(sender, "poll.invalid-option");
            plugin.getSoundManager().playSoundToSender(sender);
        }

        return true;
    }
}