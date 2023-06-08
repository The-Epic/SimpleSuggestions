package xyz.epicebic.simplesuggestions.commands;

import me.epic.spigotlib.commands.SimpleCommandHandler;
import me.epic.spigotlib.utils.TickUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;

public class TestCommand extends SimpleCommandHandler {

    private final SimpleSuggestions plugin;

    public TestCommand(SimpleSuggestions plugin) {
        super("simplesuggestions.command.test", plugin.getMessageConfig().getString("minecraft.no-permission"));
        this.plugin = plugin;
    }


    @Override
    public void handleCommand(CommandSender commandSender, String[] strings) {
        new BukkitRunnable() {
            int i = 1;
            @Override
            public void run() {
                if (i >= 1000) {
                    cancel();
                    return;
                }
                Bukkit.getOnlinePlayers().forEach(player -> player.chat("/suggest this is my super cool " + i + " suggestion"));
                i++;
            }
        }.runTaskTimer(plugin, TickUtils.fromSeconds(5), 5);
    }
}
