package xyz.epicebic.simplesuggestions.commands.minecraft;

import me.epic.spigotlib.commands.ArgumentCommandHandler;
import me.epic.spigotlib.language.MessageConfig;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;

public class CommandHandler extends ArgumentCommandHandler {

    private final SimpleSuggestions plugin;

    public CommandHandler(SimpleSuggestions plugin) {
        super(plugin.getMessageConfig(), "simplesuggestions.command");
        this.plugin = plugin;
    }
}
