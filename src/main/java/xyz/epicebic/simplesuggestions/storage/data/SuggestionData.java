package xyz.epicebic.simplesuggestions.storage.data;


import lombok.Data;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;

import java.util.UUID;

@Data
public class SuggestionData {
    private final Origin suggestionType;
    private SuggestionStatus status;

    @Nullable
    private UUID ownerMinecraftUUID;
    private Long ownerDiscordID;
    private String lastOwnerName;
    private String suggestion;
    @Nullable
    private Long discordMessageId;
    private Integer upvotes = 0;
    private Integer downvotes = 0;

    public SuggestionData(Origin suggestionType, SuggestionStatus status, @Nullable UUID ownerMinecraftUUID, Long ownerDiscordID, String suggestion, @Nullable Long discordMessageId, String lastOwnerName) {
        this.suggestionType = suggestionType;
        this.status = status;
        this.ownerMinecraftUUID = ownerMinecraftUUID;
        this.ownerDiscordID = ownerDiscordID;
        this.suggestion = suggestion;
        this.discordMessageId = discordMessageId;
        this.lastOwnerName = lastOwnerName;
    }

    public SuggestionData(Origin suggestionType, @Nullable UUID ownerMinecraftUUID, String suggestion) {
        this.suggestionType = suggestionType;
        this.status = SuggestionStatus.WAITING;
        this.ownerMinecraftUUID = ownerMinecraftUUID;
        this.ownerDiscordID = null;
        this.suggestion = suggestion;
        this.discordMessageId = null;
    }

    public SuggestionData(Origin suggestionType, Long ownerDiscordID, String suggestion, @Nullable Long discordMessageId, String lastOwnerName) {
        this.suggestionType = suggestionType;
        this.status = SuggestionStatus.WAITING;
        this.ownerMinecraftUUID = null;
        this.ownerDiscordID = ownerDiscordID;
        this.suggestion = suggestion;
        this.discordMessageId = discordMessageId;
        this.lastOwnerName = lastOwnerName;
    }

    public Integer increaseUpvote() {
        return upvotes++;
    }

    public Integer increaseDownvote() {
        return downvotes++;
    }

    public Integer decreaseUpvote() {
        return upvotes--;
    }

    public Integer decreaseDownvote() {
        return downvotes--;
    }

    public String getOwnersName() {
        if (suggestionType == Origin.MINECRAFT) {
            return Bukkit.getOfflinePlayer(ownerMinecraftUUID).getName();
        }
        User user = SimpleSuggestions.getInstance().getJda().getUserById(ownerDiscordID);
        if (user == null) return lastOwnerName;
        return user.getName();
    }
}
