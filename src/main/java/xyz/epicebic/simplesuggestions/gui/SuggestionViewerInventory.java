package xyz.epicebic.simplesuggestions.gui;

import me.epic.spigotlib.PDT;
import me.epic.spigotlib.formatting.Formatting;
import me.epic.spigotlib.items.ItemBuilder;
import me.epic.spigotlib.utils.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.SuggestionHandler;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;

import java.util.*;

public class SuggestionViewerInventory extends CustomInventory {

    private static final int START_SLOT = 9;
    private static final int END_SLOT = 45;
    private static final int ITEMS_PER_PAGE = END_SLOT - START_SLOT;
    private final Map<Integer, SuggestionData> suggestionDataMap = new HashMap<>();
    private final Map<Integer, SuggestionVote> playerVotes = new HashMap<>();
    private static final NamespacedKey INDEX_KEY = NamespacedKey.fromString("simplechatgames:index");
    private static final NamespacedKey SUGGESTION_ID_KEY = NamespacedKey.fromString("simplechatgames:suggestionid");
    private static final ItemStack PREV = new ItemBuilder(Material.PLAYER_HEAD).name("Previous Page")
            .skullTexture("81c96a5c3d13c3199183e1bc7f086f54ca2a6527126303ac8e25d63e16b64ccf").build();
    private static final ItemStack NEXT = new ItemBuilder(Material.PLAYER_HEAD).name("Next Page")
            .skullTexture("333ae8de7ed079e38d2c82dd42b74cfcbd94b3480348dbb5ecd93da8b81015e3").build();
    private static final ItemStack FILLER = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();

    private int start;


    public SuggestionViewerInventory(Player player) {
        super(null, 6, Formatting.translate("<white>Suggestions"));
        suggestionDataMap.putAll(getAllSuggestions());
        playerVotes.putAll(getPlayerVotes(player));
        addClickConsumer(event -> {
            event.setCancelled(true);

            Optional<ItemStack> optionalClickedItem = Optional.ofNullable(event.getCurrentItem());
            SuggestionHandler handler = SimpleSuggestions.getInstance().getSuggestionHandler();

            int id = optionalClickedItem
                    .flatMap(this::getSuggestionIdFromItem)
                    .orElse(-1);

            if (id != -1) {
                switch (event.getAction()) {
                    case PICKUP_ALL -> {
                        handler.addVotedSuggestion(id, player.getUniqueId(), true);
                        playerVotes.put(id, SuggestionVote.UPVOTE);
                    }
                    case MOVE_TO_OTHER_INVENTORY -> {
                        handler.removeVotedSuggestion(id, player.getUniqueId());
                        playerVotes.remove(id);
                    }
                    case PICKUP_HALF -> {
                        handler.addVotedSuggestion(id, player.getUniqueId(), false);
                        playerVotes.put(id, SuggestionVote.DOWNVOTE);
                    }
                }
                setItem(event.getRawSlot(), makeSuggestionItem(id, suggestionDataMap.get(id)));
            }
        });
        populateItems();
    }

    public void populateItems() {
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

            setItem(slot, makeSuggestionItem(id, data));
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

        populateItems();
    }

    private ItemStack makeSuggestionItem(int id, SuggestionData data) {
        ItemBuilder builder = new ItemBuilder(Material.BOOK)
                .name("<white>#" + id)
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
        SuggestionVote playerVote = playerVotes.get(id);
        if (playerVote != null) {
            builder.enchantment(Enchantment.MENDING, 1);
            builder.flags(ItemFlag.HIDE_ENCHANTS);
            lore.add("<gold>Choice: <white>" + playerVote.toString().toLowerCase());
        }
        lore.add("<gold>Upvotes: <white>" + data.getUpvotes());
        lore.add("<gold>Downvotes: <white>" + data.getDownvotes());
        builder.lore(lore);
        return builder.build();
    }

    private Map<Integer, SuggestionData> getAllSuggestions() {
        return SimpleSuggestions.getInstance().getSuggestionHandler().getSuggestions();
    }

    private Map<Integer, SuggestionVote> getPlayerVotes(Player player) {
        return SimpleSuggestions.getInstance().getSuggestionHandler().getVotedSuggestions(player.getUniqueId());
    }

    private Optional<Integer> getSuggestionIdFromItem(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(SUGGESTION_ID_KEY, PDT.INTEGER)) {
            return Optional.of(item.getItemMeta().getPersistentDataContainer().get(SUGGESTION_ID_KEY, PDT.INTEGER));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void reloadItem(int suggestionId, SuggestionData newData) {
        ItemStack[] contents = getBukkitInventory().getContents();
        for(int slot = 0; slot < contents.length; slot++) {
            if (contents[slot] == null || contents[slot].getType().isAir()) continue;
            Optional<Integer> suggestionIdFromItem = getSuggestionIdFromItem(contents[slot]);
            int finalSlot = slot;
            suggestionIdFromItem.ifPresent(id -> {
                if (suggestionId == id) {
                    setItem(finalSlot, makeSuggestionItem(suggestionId, newData));
                }
            });
        }
    }
}
