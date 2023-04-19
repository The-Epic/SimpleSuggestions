package xyz.epicebic.simplesuggestions.gui;

import lombok.Getter;
import me.epic.spigotlib.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {
    @Getter
    private static final ItemStack fillerItem = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, 1).name(" ").flags(ItemFlag.values()).build();

}
