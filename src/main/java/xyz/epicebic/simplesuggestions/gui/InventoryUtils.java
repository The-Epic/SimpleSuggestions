package xyz.epicebic.simplesuggestions.gui;

import lombok.Getter;
import me.epic.spigotlib.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {
    @Getter
    private static final ItemStack fillerItem = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, 1).name(" ").flags(ItemFlag.values()).build();

    public static List<String> splitIntoChunks(String input) {
        List<String> output = new ArrayList<>();

        // Split the first 30 characters
        if (input.length() > 30) {
            int spaceIndex = input.lastIndexOf(' ', 30);
            if (spaceIndex != -1) {
                output.add(input.substring(0, spaceIndex));
                input = input.substring(spaceIndex + 1);
            } else {
                output.add(input.substring(0, 30));
                input = input.substring(30);
            }
        } else {
            output.add(input);
            return output;
        }

        // Split the remaining characters into 40 character chunks
        while (input.length() > 40) {
            int spaceIndex = input.lastIndexOf(' ', 40);
            if (spaceIndex != -1) {
                output.add(input.substring(0, spaceIndex));
                input = input.substring(spaceIndex + 1);
            } else {
                output.add(input.substring(0, 40));
                input = input.substring(40);
            }
        }
        output.add(input);

        return output.stream().map(entry -> "<white>" + entry).toList();
    }
}
