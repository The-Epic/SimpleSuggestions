package xyz.epicebic.simplesuggestions;

import lombok.Getter;
import lombok.SneakyThrows;
import me.epic.spigotlib.language.MessageConfig;
import me.epic.spigotlib.utils.FileUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.epicebic.simplesuggestions.commands.minecraft.SuggestCommand;
import xyz.epicebic.simplesuggestions.storage.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.SuggestionHandler;
import xyz.epicebic.simplesuggestions.storage.impl.JsonStorageHandler;

import java.util.UUID;

public class SimpleSuggestions extends JavaPlugin {

    @Getter
    private MessageConfig messageConfig;
    @Getter
    private JDA jda;
    @Getter
    private static SimpleSuggestions instance;
    @Getter
    private SuggestionHandler suggestionHandler;


    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        FileUtils.loadResourceFile(this, "messages.yml").ifPresent(file -> this.messageConfig = new MessageConfig(file));
        reload();
        loadCommands();
        loadDiscordBot();
        this.suggestionHandler = new SuggestionHandler(this);
    }

    @Override
    public void onDisable() {
        if (isDiscordEnabled()) jda.shutdown();
        suggestionHandler.shutdown();
    }

    public void reload() {
        messageConfig.refresh();
    }

    public void loadCommands() {
        getCommand("suggest").setExecutor(new SuggestCommand(this));
        //getCommand("simplesuggestions").setExecutor(new CommandHandler(this));
    }

    public boolean isDiscordEnabled() {
        return jda != null;
    }

    @SneakyThrows
    public void loadDiscordBot() {
        ConfigurationSection botSection = getConfig().getConfigurationSection("discord-bot");
        if (!botSection.getBoolean("enabled", false)) return;
        String token = botSection.getString("token", "CHANGE-ME");
        if (token.equals("CHANGE-ME")) {
            getLogger().severe("Invalid discord bot token provided. Shutting down.");
            getPluginLoader().disablePlugin(this);
            return;
        }
        ConfigurationSection activitySection = botSection.getConfigurationSection("activity");
        this.jda = JDABuilder.createDefault(token)
                .setStatus(OnlineStatus.valueOf(botSection.getString("status")))
                .setActivity(Activity.of(Activity.ActivityType.valueOf(activitySection.getString("type", "WATCHING")), activitySection.getString("text", "Your suggestions!")))
                .build().awaitReady();

    }
}