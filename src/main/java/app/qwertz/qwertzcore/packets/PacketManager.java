package app.qwertz.qwertzcore.packets;

import app.qwertz.qwertzcore.QWERTZcore;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerPriority;


public class PacketManager {
    private final QWERTZcore plugin;
    private boolean initialized = false;
    public PacketManager(PacketEventsAPI<?> api, QWERTZcore plugin) {
        this.plugin = plugin;
        PacketEvents.setAPI(api);
        PacketEvents.getAPI().load();
    }

    public void register() {
        if (!initialized) {
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(false)
                .bStats(true);
        PacketEvents.getAPI().getEventManager().registerListener(new PacketSendListener(plugin),
                PacketListenerPriority.LOW);
        PacketEvents.getAPI().init();
        initialized = true;
        }
    }
}