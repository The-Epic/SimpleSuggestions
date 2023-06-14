package xyz.epicebic.simplesuggestions.gui;

import me.epic.spigotlib.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.epicebic.simplesuggestions.SimpleSuggestionsPlugin;
import xyz.epicebic.simplesuggestions.storage.SuggestionHandler;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;

import java.util.Optional;

public class SuggestionControlInventory extends CustomInventory {
    private static final ItemStack FILLER = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
    private SuggestionData suggestionData;
    private static int suggestionID;

    public SuggestionControlInventory(int suggestionId, HumanEntity player) {
        super(null, 6, "<white>Suggestion Controls");
        this.suggestionData = SimpleSuggestionsPlugin.getInstance().getSuggestionHandler().getSuggestions().get(suggestionId);

        suggestionID = suggestionId;
        addClickConsumer((event) -> event.setCancelled(true));
        populateItems(suggestionId, player);
    }

    private void populateItems(int suggestionId, HumanEntity player) {
        // Filler items
        for (int i = 0; i <= 53; i++) {
            setItem(i, FILLER);
        }

        SuggestionHandler handler = SimpleSuggestionsPlugin.getInstance().getSuggestionHandler();

        // Suggestion item
        setItem(13, InventoryUtils.makeSuggestionItem(suggestionId, suggestionData, player));

        // Voting
        addButton(30, new ItemBuilder(Material.GREEN_CONCRETE).name("<green>Upvote").build(), (event) -> {
            handler.addVotedSuggestion(suggestionId, player.getUniqueId(), true);
        });
        addButton(31, new ItemBuilder(Material.YELLOW_CONCRETE).name("<yellow>No vote").build(), (event) -> {
            handler.removeVotedSuggestion(suggestionId, player.getUniqueId());
        });
        addButton(32, new ItemBuilder(Material.RED_CONCRETE).name("<red>Downvote").build(), (event) -> {
            handler.addVotedSuggestion(suggestionId, player.getUniqueId(), false);
        });

        // Status'
        addButton(38, new ItemBuilder(Material.GREEN_TERRACOTTA).name("<green>Approved").build(), (event) -> {
            handler.setSuggestionStatus(suggestionId, SuggestionStatus.APPROVED);
            this.suggestionData = handler.getSuggestions().get(suggestionId);
        });
        addButton(39, new ItemBuilder(Material.YELLOW_TERRACOTTA).name("<yellow>Considered").build(), (event) -> {
            handler.setSuggestionStatus(suggestionId, SuggestionStatus.CONSIDERED);
            this.suggestionData = handler.getSuggestions().get(suggestionId);
        });
        addButton(40, new ItemBuilder(Material.WHITE_TERRACOTTA).name("<white>Remove answer").build(), (event) -> {
            handler.setSuggestionStatus(suggestionId, SuggestionStatus.WAITING);
            this.suggestionData = handler.getSuggestions().get(suggestionId);
        });
        addButton(41, new ItemBuilder(Material.PINK_TERRACOTTA).name("<light_purple>Implemented").build(), (event) -> {
            handler.setSuggestionStatus(suggestionId, SuggestionStatus.IMPLEMENTED);
            this.suggestionData = handler.getSuggestions().get(suggestionId);
        });
        addButton(42, new ItemBuilder(Material.RED_TERRACOTTA).name("<red>Denied").build(), (event) -> {
            handler.setSuggestionStatus(suggestionId, SuggestionStatus.DENIED);
            this.suggestionData = handler.getSuggestions().get(suggestionId);
        });

        //Back button
        addButton(49, new ItemBuilder(Material.BARRIER).name("<red>Back").build(), this::openOldInv);
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
                    setItem(finalSlot, InventoryUtils.makeSuggestionItem(suggestionId, newData, getBukkitInventory().getViewers().get(0)));
                }
            });
        }
    }

    private void openOldInv(InventoryClickEvent event) {
        Player player = (Player) event.getView().getPlayer();
        player.closeInventory();
        SimpleSuggestionsPlugin.getInstance().getInventoryHandler().openGui(player, new SuggestionViewerInventory(player, InventoryUtils.getStartSlot(suggestionID)));
    }
}
