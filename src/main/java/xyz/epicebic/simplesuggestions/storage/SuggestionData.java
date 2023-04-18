package xyz.epicebic.simplesuggestions.storage;


import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Data
public class SuggestionData {
    private final Type suggestionType;
    private Status status;

    @Nullable
    private UUID ownerMinecraftUUID;
    private Long ownerDiscordID;
    private String suggestion;
    @Nullable
    private Long discordMessageId;
    private Integer upvotes = 0;
    private Integer downvotes = 0;

    public SuggestionData(Type suggestionType, Status status, @Nullable UUID ownerMinecraftUUID, Long ownerDiscordID, String suggestion, @Nullable Long discordMessageId) {
        this.suggestionType = suggestionType;
        this.status = status;
        this.ownerMinecraftUUID = ownerMinecraftUUID;
        this.ownerDiscordID = ownerDiscordID;
        this.suggestion = suggestion;
        this.discordMessageId = discordMessageId;
    }

    public SuggestionData(Type suggestionType, @Nullable UUID ownerMinecraftUUID, String suggestion) {
        this.suggestionType = suggestionType;
        this.status = Status.WAITING;
        this.ownerMinecraftUUID = ownerMinecraftUUID;
        this.ownerDiscordID = null;
        this.suggestion = suggestion;
        this.discordMessageId = null;
    }

    public SuggestionData(Type suggestionType, Long ownerDiscordID, String suggestion, @Nullable Long discordMessageId) {
        this.suggestionType = suggestionType;
        this.status = Status.WAITING;
        this.ownerMinecraftUUID = null;
        this.ownerDiscordID = ownerDiscordID;
        this.suggestion = suggestion;
        this.discordMessageId = discordMessageId;
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

    public enum Type {
        MINECRAFT,
        DISCORD
    }

    public enum Status {
        APPROVED,
        DENIED,
        CONSIDERED,
        IMPLEMENTED,
        WAITING
    }
}
