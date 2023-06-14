package xyz.epicebic.simplesuggestions.gui;

import lombok.Getter;
import me.epic.spigotlib.PDT;
import me.epic.spigotlib.items.ItemBuilder;
import me.epic.spigotlib.utils.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import xyz.epicebic.simplesuggestions.SimpleSuggestionsPlugin;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryUtils {
    @Getter
    private static final ItemStack fillerItem = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE, 1).name(" ").flags(ItemFlag.values()).build();
    private static final NamespacedKey SUGGESTION_ID_KEY = NamespacedKey.fromString("simplechatgames:suggestionid");
    private static final int ITEMS_PER_PAGE = 45 - 9;

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

    public static ItemStack makeSuggestionItem(int id, SuggestionData data, HumanEntity player) {
        ItemBuilder builder = new ItemBuilder(Material.BOOK)
                .name("<white>#" + id + (data.getStatus() == SuggestionStatus.WAITING ? "" : " " + WordUtils.getNiceName(data.getStatus().toString())))
                .persistentData(SUGGESTION_ID_KEY, PDT.INTEGER, id);
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        List<String> suggestionFormatted = InventoryUtils.splitIntoChunks(data.getSuggestion());
        lore.add("<gold>Suggestion: <white>" + suggestionFormatted.get(0));
        for (int i = 1; i < suggestionFormatted.size(); i++) {
            lore.add(suggestionFormatted.get(i));
        }
        lore.add("<gold>Origin: <white>" + WordUtils.getNiceName(data.getSuggestionType().toString()));
        lore.add("<gold>Owner: <white>" + data.getOwnersName());
        lore.add("");
        SuggestionVote playerVote = SimpleSuggestionsPlugin.getInstance().getSuggestionHandler().getVotedSuggestions(player.getUniqueId()).get(id);
        if (playerVote != null && playerVote != SuggestionVote.NOVOTE) {
            builder.enchantment(Enchantment.MENDING, 1);
            builder.flags(ItemFlag.HIDE_ENCHANTS);
            lore.add("<gold>Choice: <white>" + playerVote.toString().toLowerCase());
        }
        lore.add("<gold>Upvotes: <white>" + data.getUpvotes());
        lore.add("<gold>Downvotes: <white>" + data.getDownvotes());
        builder.lore(lore);
        return builder.build();
    }

    public static  Optional<Integer> getSuggestionIdFromItem(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(SUGGESTION_ID_KEY, PDT.INTEGER)) {
            return Optional.of(item.getItemMeta().getPersistentDataContainer().get(SUGGESTION_ID_KEY, PDT.INTEGER));
        } else {
            return Optional.empty();
        }
    }

    public static int getStartSlot(int suggestionId) {
        int pageIndex = suggestionId / ITEMS_PER_PAGE;
        return pageIndex * ITEMS_PER_PAGE;
    }
}
