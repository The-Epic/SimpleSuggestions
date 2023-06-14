package xyz.epicebic.simplesuggestions;

import lombok.Getter;
import lombok.SneakyThrows;
import me.epic.spigotlib.EpicSpigotLib;
import me.epic.spigotlib.config.ConfigUpdater;
import me.epic.spigotlib.language.MessageConfig;
import me.epic.spigotlib.utils.FileUtils;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.epicebic.simplesuggestions.commands.CommandHandler;
import xyz.epicebic.simplesuggestions.commands.SuggestCommand;
import xyz.epicebic.simplesuggestions.discord.DiscordCommandHandler;
import xyz.epicebic.simplesuggestions.gui.InventoryHandler;
import xyz.epicebic.simplesuggestions.storage.SuggestionHandler;

@Getter
public class SimpleSuggestionsPlugin extends JavaPlugin {

    private MessageConfig messageConfig;
    private JDA jda;
    @Getter
    private static SimpleSuggestionsPlugin instance;
    private SuggestionHandler suggestionHandler;
    private InventoryHandler inventoryHandler;

    @Override
    public void onLoad() {
        loadLibraries();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        EpicSpigotLib.loadNMS(instance);
        FileUtils.loadResourceFile(instance, "messages.yml").ifPresent(file -> this.messageConfig = new MessageConfig(file));
        ConfigUpdater.runConfigUpdater(instance);
        try {
            this.suggestionHandler = new SuggestionHandler(instance);
        } catch (IllegalArgumentException ex) {
            getLogger().warning("Suggestion Handler failed to load, disabling.");
            getPluginLoader().disablePlugin(this);
            ex.printStackTrace();
            return;
        }
        this.inventoryHandler = new InventoryHandler();
        Bukkit.getPluginManager().registerEvents(inventoryHandler, instance);
        reload();
        loadCommands();
        loadDiscordBot();
    }

    @Override
    public void onDisable() {
        if (isDiscordEnabled()) jda.shutdown();
        if (suggestionHandler != null) suggestionHandler.shutdown();
    }

    public void reload() {
        messageConfig.refresh();
    }

    public void loadCommands() {
        getCommand("suggest").setExecutor(new SuggestCommand(instance));
        getCommand("simplesuggestions").setExecutor(new CommandHandler(instance));
    }

    public boolean isDiscordEnabled() {
        return jda != null;
    }

    @SneakyThrows
    public void loadDiscordBot() {
        ConfigurationSection botSection = getConfig().getConfigurationSection("discord-bot");
        if (!botSection.getBoolean("enabled", false)) return;
        String token = botSection.getString("token", "CHANGE-ME");
        long guildId = botSection.getLong("guild-id");
        if (token.equals("CHANGE-ME") || guildId == 0) {
            getLogger().severe("Invalid configuration options. Discord not enabling.");
            return;
        }
        ConfigurationSection activitySection = botSection.getConfigurationSection("activity");
        this.jda = JDABuilder.createDefault(token)
                .setStatus(OnlineStatus.valueOf(botSection.getString("status")))
                .setActivity(Activity.of(Activity.ActivityType.valueOf(activitySection.getString("type", "WATCHING")), activitySection.getString("text", "Your suggestions!")))
                .addEventListeners(new DiscordCommandHandler(this))
                .build().awaitReady();
        Guild guild = jda.getGuildById(guildId);
        guild.updateCommands().addCommands(Commands.slash("suggest", "Creates a new suggestion")).queue();
    }

    private void loadLibraries() {
        final BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
        Library lib = Library.builder().groupId("me.epic").artifactId("epicspigotlib").version("1.2.10-SNAPSHOT").url("https://github.com/The-Epic/EpicSpigotLib/releases/download/1.2.10-SNAPSHOT/epicspigotlib-1.2.10-SNAPSHOT.jar").id("EpicSpigotLib").build();
        Library JDA = Library.builder().groupId("net.dv8tion").artifactId("JDA").version("5.0.0-beta.8").id("JDA (Java Discord API)").build();
        Library hikariCP = Library.builder().groupId("com.zaxxer").artifactId("HikariCP").version("5.0.1").id("HikariCP (Database Manager)").build();
        libraryManager.addMavenCentral();
        libraryManager.loadLibrary(lib);
        libraryManager.loadLibrary(JDA);
        libraryManager.loadLibrary(hikariCP);
    }
}