package xyz.epicebic.simplesuggestions.storage;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;
import xyz.epicebic.simplesuggestions.storage.data.UserData;

import java.util.*;
import java.util.function.Consumer;

public abstract class StorageHandler implements Listener {

    protected final Map<Integer, SuggestionData> suggestions = new HashMap<>();
    protected final List<UserData> playerData = new ArrayList<>();

    protected final SimpleSuggestions plugin;

    public StorageHandler(SimpleSuggestions plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public abstract int saveSuggestion(SuggestionData data);
    public abstract SuggestionData readSuggestion(Integer id);

    public abstract Map<Integer, SuggestionVote> getVotedSuggestions(UUID uuid);
    public abstract void addVotedSuggestion(Integer id, UUID uuid, boolean choice);
    public abstract void removeVotedSuggestion(Integer id, UUID uuid);

    public abstract Map<Integer, SuggestionVote> getVotedSuggestions(Long id);
    public abstract void addVotedSuggestion(Integer id, Long userId, boolean choice);
    public abstract void removeVotedSuggestion(Integer id, Long userId);

    public abstract void setSuggestionStatus(int id, SuggestionStatus newStatus);

    public abstract Map<Integer, SuggestionData> getSuggestions();

    public abstract void save();
    public abstract Optional<UserData> getUserData(UUID uuid);
    public abstract Optional<UserData> getUserData(Long id);

    public abstract void updateUserData(UUID uuid, Consumer<UserData> updatedData);
    public abstract void updateUserData(Long id, Consumer<UserData> updatedData);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        Optional<UserData> optionalData = getUserData(uuid);
        UserData userData = optionalData.orElseGet(() -> new UserData(null, uuid));

        playerData.add(userData);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        playerData.removeIf(data -> data.matchUUID(uuid));

        if (Bukkit.getPlayer(uuid) == null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> removeVotedSuggestionsByUUID(uuid), 20 * 60 * 5);
        }
    }

    private void removeVotedSuggestionsByUUID(UUID uuid) {
        Map<Integer, SuggestionVote> votedSuggestions = getVotedSuggestions(uuid);
        for (Map.Entry<Integer, SuggestionVote> entry : votedSuggestions.entrySet()) {
            int suggestionId = entry.getKey();
            removeVotedSuggestion(suggestionId, uuid);
        }
    }
}
