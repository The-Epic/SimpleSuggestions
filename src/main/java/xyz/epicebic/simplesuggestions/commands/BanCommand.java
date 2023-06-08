package xyz.epicebic.simplesuggestions.commands;

import me.epic.spigotlib.commands.SimpleCommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;

public class BanCommand extends SimpleCommandHandler {

    private final SimpleSuggestions plugin;

    public BanCommand(SimpleSuggestions plugin) {
        super("simplesuggestions.command.ban", plugin.getMessageConfig().getString("minecraft.no-permission"));
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void handleCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageConfig().getString("minecraft.no-player-provided"));
            return;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
        if (sender instanceof Player player && offlinePlayer.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageConfig().getString("minecraft.cannot-ban-yourself"));
            return;
        }

        plugin.getSuggestionHandler().updateUserData(offlinePlayer.getUniqueId(), data -> data.setBanned(true));
        sender.sendMessage(plugin.getMessageConfig().getString("minecraft.banned-player").replace("%name%", offlinePlayer.getName()));
    }
}
