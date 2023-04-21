package xyz.epicebic.simplesuggestions.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;

import java.util.HashMap;
import java.util.Map;

public class InventoryHandler implements Listener {
    private final HashMap<InventoryView, CustomInventory> inventoryMap = new HashMap<>();

    public void openGui(HumanEntity player, CustomInventory inventory) {
        this.inventoryMap.put(inventory.open(player), inventory);
    }

    public void reloadSuggestionItem(int suggestionId, SuggestionData newData) {
        for (Map.Entry<InventoryView, CustomInventory> entry : inventoryMap.entrySet()) {
            entry.getValue().reloadItem(suggestionId, newData);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;

        CustomInventory inventory = this.inventoryMap.get(event.getView());

        if (inventory != null) {
            inventory.consumeClickEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        CustomInventory inventory = inventoryMap.get(event.getView());

        if (inventory != null) {
            inventory.consumeCloseEvent(event);

            this.inventoryMap.remove(event.getView());
        }
    }
}
