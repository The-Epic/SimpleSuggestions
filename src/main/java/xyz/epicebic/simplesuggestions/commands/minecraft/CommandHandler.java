package xyz.epicebic.simplesuggestions.commands.minecraft;

import me.epic.spigotlib.commands.ArgumentCommandHandler;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;

public class CommandHandler extends ArgumentCommandHandler {

    public CommandHandler(SimpleSuggestions plugin) {
        super(plugin.getMessageConfig(), "simplesuggestions.command");

        addArgumentExecutor("open", new OpenGUICommand(plugin));
        addArgumentExecutor("test", new TestCommand(plugin));
    }
}
