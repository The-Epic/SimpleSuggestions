package xyz.epicebic.simplesuggestions.commands.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.epic.spigotlib.commands.SimpleCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.SuggestionHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class SuggestCommand extends SimpleCommandHandler {

    private final SimpleSuggestions plugin;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();


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
        System.out.println(plugin.getSuggestionHandler().save(data));
    }

    @Override
    public List<String> handleTabCompletion(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}