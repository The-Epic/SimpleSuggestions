package xyz.epicebic.simplesuggestions.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import xyz.epicebic.simplesuggestions.SimpleSuggestionsPlugin;

@RequiredArgsConstructor
public class DiscordCommandHandler extends ListenerAdapter {

    private final SimpleSuggestionsPlugin plugin;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
    }
}