package xyz.epicebic.simplesuggestions.storage;

import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class StorageSaveRunnable extends BukkitRunnable {

    private final SuggestionHandler handler;

    @Override
    public void run() {
        handler.saveData();
    }
}
