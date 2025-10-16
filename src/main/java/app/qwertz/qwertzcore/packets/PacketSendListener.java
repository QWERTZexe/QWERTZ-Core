package app.qwertz.qwertzcore.packets;

import app.qwertz.qwertzcore.QWERTZcore;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

public class PacketSendListener implements PacketListener {

    private QWERTZcore plugin;
    public PacketSendListener(QWERTZcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (plugin.getConfigManager() == null) return;
        if (!plugin.getConfigManager().getScoreBoard()) return;
        if (event.getPacketType() != PacketType.Play.Server.SCOREBOARD_OBJECTIVE) return;
        WrapperPlayServerScoreboardObjective objective = new WrapperPlayServerScoreboardObjective(event);
        String numberColor = "#";
        if (plugin.getScoreboardManager() != null) {
            numberColor = plugin.getScoreboardManager().getNumberColor();
        }
        if (numberColor.equals("#")) {
            objective.setScoreFormat(ScoreFormat.blankScore());
        } else {
            objective.setScoreFormat(ScoreFormat.styledScore(Style.style(TextColor.fromHexString(numberColor))));
        }
        event.markForReEncode(true);
    }
}