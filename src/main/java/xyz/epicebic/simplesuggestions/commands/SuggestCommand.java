package xyz.epicebic.simplesuggestions.commands;

import me.epic.spigotlib.commands.SimpleCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.data.Origin;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;

public class SuggestCommand extends SimpleCommandHandler {

    private final SimpleSuggestions plugin;

    public SuggestCommand(SimpleSuggestions plugin) {
        super("simplesuggestions.command.suggest", plugin.getMessageConfig().getString("minecraft.no-permission"));
        this.plugin = plugin;
    }


    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This is a player only command!");
            return;
        }
        if (args.length < 1) {
            player.sendMessage(plugin.getMessageConfig().getString("minecraft.no-suggestion-provided"));
            return;
        }
        if (plugin.getSuggestionHandler().isUserBanned(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageConfig().getString("minecraft.banned-user-message"));
            return;
        }

        SuggestionData data = new SuggestionData(Origin.MINECRAFT, player.getUniqueId(), String.join(" ", args));
        int id = plugin.getSuggestionHandler().save(data);
        player.sendMessage(plugin.getMessageConfig().getString("minecraft.suggestion-created").replace("%id%", String.valueOf(id)));
    }

}