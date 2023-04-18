package xyz.epicebic.simplesuggestions.storage;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@ToString
@EqualsAndHashCode
public class SuggestionData {
    private final Type suggestionType;
    private Status status;

    @Nullable
    private UUID ownerMinecraftUUID;
    private Long ownerDiscordID;
    private String suggestion;
    @Nullable
    private Long discordMessageId;

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
