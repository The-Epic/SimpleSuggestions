package xyz.epicebic.simplesuggestions.commands;

import me.epic.spigotlib.commands.ArgumentCommandHandler;
import xyz.epicebic.simplesuggestions.SimpleSuggestionsPlugin;

public class CommandHandler extends ArgumentCommandHandler {

    public CommandHandler(SimpleSuggestionsPlugin plugin) {
        super(plugin.getMessageConfig(), "simplesuggestions.command", plugin.getMessageConfig().getString("minecraft.no-permission"));

        addArgumentExecutor("open", new OpenGUICommand(plugin));
        addArgumentExecutor("test", new TestCommand(plugin));
        addArgumentExecutor("ban", new BanCommand(plugin));
    }
}
