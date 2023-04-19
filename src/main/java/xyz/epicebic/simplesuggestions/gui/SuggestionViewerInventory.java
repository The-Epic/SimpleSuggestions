package xyz.epicebic.simplesuggestions.gui;

import me.epic.spigotlib.formatting.Formatting;
import me.epic.spigotlib.inventory.CustomInventory;
import me.epic.spigotlib.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.SuggestionData;

import java.util.HashMap;
import java.util.Map;

public class SuggestionViewerInventory extends CustomInventory {

    private static final int START_SLOT = 9;
    private static final int END_SLOT = 45;
    private static final int ITEMS_PER_PAGE = END_SLOT - START_SLOT;
    private final Map<Integer, SuggestionData> suggestionDataMap = new HashMap<>();
    private static final NamespacedKey INDEX_KEY = NamespacedKey.fromString("simplechatgames:index");
    private static final ItemStack PREV = new ItemBuilder(Material.PLAYER_HEAD).name("Previous Page")
            .skullTexture("81c96a5c3d13c3199183e1bc7f086f54ca2a6527126303ac8e25d63e16b64ccf").build();
    private static final ItemStack NEXT = new ItemBuilder(Material.PLAYER_HEAD).name("Next Page")
            .skullTexture("333ae8de7ed079e38d2c82dd42b74cfcbd94b3480348dbb5ecd93da8b81015e3").build();
    private static final ItemStack FILLER = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();

    private int start;


    public SuggestionViewerInventory(Player player) {
        super(null, 6, Formatting.translate("<white>Suggestions"));
        suggestionDataMap.putAll(SimpleSuggestions.getInstance().getSuggestionHandler().getSuggestions());
        //SimpleSuggestions.getInstance().getSuggestionHandler().
        addClickConsumer(event -> event.setCancelled(true));
        populateItems();
    }

    private void populateItems() {
        if (suggestionDataMap.isEmpty()) return;
        for (int i = 1; i < ITEMS_PER_PAGE; i++) {
            int index = i + start;
            if (index >= suggestionDataMap.size()) {
                setItem(START_SLOT + i, null);
                continue;
            }
            int slot = START_SLOT + i - 1;
            SuggestionData data = suggestionDataMap.get(index);

            setItem(slot, new ItemBuilder(Material.GLASS).name(String.valueOf(index)).lore(data.toString()).build());
        }

        for (int i = 0, j = 45; i <= 8 && j <= 53; i++, j++) {
            setItem(i, FILLER);
            setItem(j, FILLER);
        }

        if (start + ITEMS_PER_PAGE < suggestionDataMap.size()) {
            addButton(52,
                    ItemBuilder.modifyItem(NEXT)
                            .persistentData(INDEX_KEY, PersistentDataType.INTEGER, start + ITEMS_PER_PAGE).build(),
                    this::gotoPage);
        } else {
            removeButton(52);
        }
        if (start > 0) {
            addButton(46,
                    ItemBuilder.modifyItem(PREV)
                            .persistentData(INDEX_KEY, PersistentDataType.INTEGER, start - ITEMS_PER_PAGE).build(),
                    this::gotoPage);
        } else {
            removeButton(46);
        }
    }

    private void gotoPage(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        PersistentDataContainer container = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
        start = container.getOrDefault(INDEX_KEY, PersistentDataType.INTEGER, 0);

        populateItems();
    }

}
