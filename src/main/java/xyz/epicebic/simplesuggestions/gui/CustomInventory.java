package xyz.epicebic.simplesuggestions.gui;

import org.bukkit.inventory.InventoryHolder;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;

public abstract class CustomInventory extends me.epic.spigotlib.inventory.CustomInventory {

    public CustomInventory(InventoryHolder holder, int rows, String name) {
        super(holder, rows, name);
    }

    public abstract void reloadItem(int suggestionId, SuggestionData newData);

    @Override
    public void reload() {}
}