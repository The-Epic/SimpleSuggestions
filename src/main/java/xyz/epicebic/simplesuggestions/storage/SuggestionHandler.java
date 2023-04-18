package xyz.epicebic.simplesuggestions.storage;

import me.epic.spigotlib.utils.TickUtils;
import org.bukkit.scheduler.BukkitTask;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;

import java.util.concurrent.CompletableFuture;

public class SuggestionHandler {

    private final SimpleSuggestions plugin;
    private StorageHandler storageHandler;
    private BukkitTask storageRunnable;

    public SuggestionHandler(SimpleSuggestions plugin) {
        this.plugin = plugin;
        this.storageRunnable = new StorageSaveRunnable(this).runTaskTimerAsynchronously(plugin, TickUtils.fromMinutes(30), TickUtils.fromHours(1));
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
