package xyz.epicebic.simplesuggestions.storage;

import me.epic.spigotlib.utils.TickUtils;
import org.bukkit.scheduler.BukkitTask;
import xyz.epicebic.simplesuggestions.SimpleSuggestionsPlugin;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;
import xyz.epicebic.simplesuggestions.storage.data.UserData;
import xyz.epicebic.simplesuggestions.storage.impl.JsonStorageHandler;
import xyz.epicebic.simplesuggestions.storage.impl.SQLiteStorageHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class SuggestionHandler {

    private final SimpleSuggestionsPlugin plugin;
    private StorageHandler storageHandler;
    private final BukkitTask storageRunnable;

    public SuggestionHandler(SimpleSuggestionsPlugin plugin) throws IllegalArgumentException {
        this.plugin = plugin;
        loadStorageType();
        this.storageRunnable = new StorageSaveRunnable(this).runTaskTimerAsynchronously(plugin, TickUtils.fromMinutes(30), TickUtils.fromHours(1));
    }

    private void loadStorageType() throws IllegalArgumentException {
        switch (plugin.getConfig().getString("storage.type", "sqlite")) {
            case "json" -> this.storageHandler = new JsonStorageHandler(plugin);
            case "sqlite" -> this.storageHandler = new SQLiteStorageHandler(plugin);
            // TODO mysql (S-Q-L) + sqlite
            default -> throw new IllegalArgumentException("Invalid storage type, please choose between \"Json\", \"sqlite\"");
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

    public void updateUserData(UUID uuid, Consumer<UserData> newData) {
        storageHandler.updateUserData(uuid, newData);
    }

    public void updateUserData(Long id, Consumer<UserData> newData) {
        storageHandler.updateUserData(id, newData);
    }

    public boolean isUserBanned(UUID uuid) {
        return storageHandler.getUserData(uuid)
                .map(UserData::isBanned)
                .orElse(false);
    }

    public boolean isUserBanned(long id) {
        return storageHandler.getUserData(id)
                .map(UserData::isBanned)
                .orElse(false);
    }



    public void saveData() {
        storageHandler.save();
    }

}
