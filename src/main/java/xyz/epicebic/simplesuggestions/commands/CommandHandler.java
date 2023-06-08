package xyz.epicebic.simplesuggestions.commands;

import me.epic.spigotlib.commands.ArgumentCommandHandler;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;

public class CommandHandler extends ArgumentCommandHandler {

    public CommandHandler(SimpleSuggestions plugin) {
        super(plugin.getMessageConfig(), "simplesuggestions.command", plugin.getMessageConfig().getString("minecraft.no-permission"));

        addArgumentExecutor("open", new OpenGUICommand(plugin));
        addArgumentExecutor("test", new TestCommand(plugin));
        addArgumentExecutor("ban", new BanCommand(plugin));
    }
}
