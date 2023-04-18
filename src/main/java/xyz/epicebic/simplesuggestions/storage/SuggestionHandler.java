package xyz.epicebic.simplesuggestions.storage;

import me.epic.spigotlib.utils.TickUtils;
import org.bukkit.scheduler.BukkitTask;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.impl.JsonStorageHandler;

import java.util.concurrent.CompletableFuture;

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
            case "json" -> this.storageHandler = new JsonStorageHandler();
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
    public CompletableFuture<Integer> save(SuggestionData data) {
        return storageHandler.saveSuggestion(data);
    }

    /**
     * Gets suggestion data for an ID
     *
     * @param id to query
     * @return Future of the data
     */
    public CompletableFuture<SuggestionData> read(int id) {
        return storageHandler.readSuggestion(id);
    }

    public void saveData() {
        storageHandler.save();
    }

}
