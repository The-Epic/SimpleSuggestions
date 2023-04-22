package xyz.epicebic.simplesuggestions.storage;

import me.epic.spigotlib.utils.TickUtils;
import org.bukkit.scheduler.BukkitTask;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;
import xyz.epicebic.simplesuggestions.storage.impl.JsonStorageHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuggestionHandler {

    private final SimpleSuggestions plugin;
    private StorageHandler storageHandler;
    private final BukkitTask storageRunnable;

    public SuggestionHandler(SimpleSuggestions plugin) {
        this.plugin = plugin;
        loadStorageType();
        this.storageRunnable = new StorageSaveRunnable(this).runTaskTimerAsynchronously(plugin, TickUtils.fromMinutes(30), TickUtils.fromHours(1));
    }

    private void loadStorageType() {
        switch (plugin.getConfig().getString("storage.type", "sqlite")) {
            case "json" -> this.storageHandler = new JsonStorageHandler(plugin);
            // TODO mysql (S-Q-L) + sqlite
            default -> {
                plugin.getPluginLoader().disablePlugin(plugin);
                throw new IllegalArgumentException("Invalid storage type. Disabling");
            }
        }
    }

    public void shutdown() {
        storageRunnable.cancel();
        storageHandler.save();
    }

    /**
     * Saves a suggestion
     *
     * @param data to save
     * @return Future of id
     */
    public int save(SuggestionData data) {
        return storageHandler.saveSuggestion(data);
    }

    /**
     * Gets suggestion data for an ID
     *
     * @param id to query
     * @return Future of the data
     */
    public SuggestionData read(int id) {
        return storageHandler.readSuggestion(id);
    }

    public Map<Integer, SuggestionData> getSuggestions() {
        return storageHandler.getSuggestions();
    }

    public Map<Integer, SuggestionVote> getVotedSuggestions(UUID uuid) {
        Map<Integer, SuggestionVote> map = storageHandler.getVotedSuggestions(uuid);
        return map == null ? new HashMap<>() : map;
    }

    public void addVotedSuggestion(int id, UUID uuid, boolean choice) {
        storageHandler.addVotedSuggestion(id, uuid, choice);
    }

    public void removeVotedSuggestion(int id, UUID uuid) {
        storageHandler.removeVotedSuggestion(id, uuid);
    }

    public void setSuggestionStatus(int id, SuggestionStatus newStatus) {
        storageHandler.setSuggestionStatus(id, newStatus);
    }

    public void saveData() {
        storageHandler.save();
    }

}
