package xyz.epicebic.simplesuggestions.commands.minecraft;

import me.epic.spigotlib.commands.SimpleCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.SuggestionData;

import java.util.Collections;
import java.util.List;

public class SuggestCommand extends SimpleCommandHandler {

    private final SimpleSuggestions plugin;

    public SuggestCommand(SimpleSuggestions plugin) {
        super("simplesuggestions.command.suggest");
        this.plugin = plugin;
    }

    @Override
    public void handleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This is a player only command!");
            return;
        }
        if (args.length < 1) {
            player.sendMessage(plugin.getMessageConfig().getString("no-suggestion-provided"));
            return;
        }

        SuggestionData data = new SuggestionData(SuggestionData.Type.MINECRAFT, player.getUniqueId(), String.join(" ", args));
        plugin.getSuggestionHandler().save(data).whenComplete((id,ex) -> {
            System.out.println(id);
        });
    }

    @Override
    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}