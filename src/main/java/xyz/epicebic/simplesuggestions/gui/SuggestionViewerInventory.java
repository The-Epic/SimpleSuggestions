package xyz.epicebic.simplesuggestions.gui;

import me.epic.spigotlib.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.SuggestionHandler;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;

import java.util.*;

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
    private final Player player;

    private int start;


    public SuggestionViewerInventory(HumanEntity player) {
        this(player, 0);
    }

    public SuggestionViewerInventory(HumanEntity player, int start) {
        super(null, 6, "<white>Suggestions");
        this.player = (Player) player;
        suggestionDataMap.putAll(getAllSuggestions());
        addClickConsumer(event -> {
            event.setCancelled(true);

            Optional<ItemStack> optionalClickedItem = Optional.ofNullable(event.getCurrentItem());
            SuggestionHandler handler = SimpleSuggestions.getInstance().getSuggestionHandler();

            int id = optionalClickedItem
                    .flatMap(InventoryUtils::getSuggestionIdFromItem)
                    .orElse(-1);

            if (id != -1) {
                switch (event.getAction()) {
                    case PICKUP_ALL -> {
                        if (player.hasPermission("simplesuggestions.staff")) {
                            SimpleSuggestions.getInstance().getInventoryHandler().openGui(player, new SuggestionControlInventory(id, player));
                            break;
                        }
                        handler.addVotedSuggestion(id, player.getUniqueId(), true);
                    }
                    case MOVE_TO_OTHER_INVENTORY -> {
                        handler.removeVotedSuggestion(id, player.getUniqueId());
                    }
                    case PICKUP_HALF -> {
                        handler.addVotedSuggestion(id, player.getUniqueId(), false);
                    }
                }
                setItem(event.getRawSlot(), InventoryUtils.makeSuggestionItem(id, suggestionDataMap.get(id), player));
            }
        });
        this.start = start;
        populateItems(player);
    }

    public void populateItems(HumanEntity player) {
        if (suggestionDataMap.isEmpty()) return;
        // Suggestions
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int index = i + start;
            int slot = START_SLOT + i;
            if (index >= suggestionDataMap.size()) {
                setItem(slot, null);
                continue;
            }
            int id = index + 1;
            SuggestionData data = suggestionDataMap.get(id);

            setItem(slot, InventoryUtils.makeSuggestionItem(id, data, player));
        }

        // Filler bars
        for (int i = 0, j = 45; i <= 8 && j <= 53; i++, j++) {
            setItem(i, FILLER);
            setItem(j, FILLER);
        }

        // Buttons
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
        setItem(4, createControlsItem(player));

        // Page Counter
        int maxPages = (int) Math.ceil((double) suggestionDataMap.size() / ITEMS_PER_PAGE);
        int currentPage = start / ITEMS_PER_PAGE + 1;

        ItemStack pageItem = new ItemBuilder(Material.PAPER).name("<gold>Pages").lore("<white>" + currentPage + "/" + maxPages).build();
        setItem(49, pageItem);
    }

    private void gotoPage(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }

        PersistentDataContainer container = event.getCurrentItem().getItemMeta().getPersistentDataContainer();
        start = container.getOrDefault(INDEX_KEY, PersistentDataType.INTEGER, 0);

        populateItems(event.getWhoClicked());
    }


    private Map<Integer, SuggestionData> getAllSuggestions() {
        return SimpleSuggestions.getInstance().getSuggestionHandler().getSuggestions();
    }

    private Map<Integer, SuggestionVote> getPlayerVotes(Player player) {
        return SimpleSuggestions.getInstance().getSuggestionHandler().getVotedSuggestions(player.getUniqueId());
    }


    @Override
    public void reloadItem(int suggestionId, SuggestionData newData) {
        ItemStack[] contents = getBukkitInventory().getContents();
        for(int slot = 0; slot < contents.length; slot++) {
            if (contents[slot] == null || contents[slot].getType().isAir()) continue;
            Optional<Integer> suggestionIdFromItem = InventoryUtils.getSuggestionIdFromItem(contents[slot]);
            int finalSlot = slot;
            suggestionIdFromItem.ifPresent(id -> {
                if (suggestionId == id) {
                    setItem(finalSlot, InventoryUtils.makeSuggestionItem(suggestionId, newData, player));
                }
            });
        }
    }

    public ItemStack createControlsItem(HumanEntity player) {
        ItemBuilder builder = new ItemBuilder(Material.PAPER);
        List<String> lore = new ArrayList<>();
        builder.name("Controls");
        lore.add("");
        if (player.hasPermission("simplesuggestions.staff")) {
            lore.add("<gold>Left Click: <white>Open options menu");
        } else {
            lore.add("<gold>Left CLick: <white>Upvote");
            lore.add("<gold>Right Click: <white>Downvote");
            lore.add("<gold>Shift Left Click: <white>Remove Vote");
        }

        builder.lore(lore);
        return builder.build();
    }

}
