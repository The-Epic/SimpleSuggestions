package xyz.epicebic.simplesuggestions.commands;

import me.epic.spigotlib.commands.SimpleCommandHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.gui.SuggestionViewerInventory;

public class OpenGUICommand extends SimpleCommandHandler {

    private final SimpleSuggestions plugin;

    public OpenGUICommand(SimpleSuggestions plugin) {
        super("simplesuggestions.command.open", plugin.getMessageConfig().getString("minecraft.no-permission"));
        this.plugin = plugin;
    }

    @Override
    public void handleCommand(CommandSender commandSender, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This is a player only command");
            return;
        }

        plugin.getInventoryHandler().openGui(player, new SuggestionViewerInventory(player));
        player.sendMessage(plugin.getMessageConfig().getString("minecraft.suggestion-gui-opened"));
    }

}
