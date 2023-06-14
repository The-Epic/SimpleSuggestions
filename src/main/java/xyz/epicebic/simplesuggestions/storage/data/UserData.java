package xyz.epicebic.simplesuggestions.storage.data;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class UserData {
    private UUID minecraftUID;
    private Long discordUID;

    private boolean banned;

    private Map<Integer, SuggestionVote> votes = new HashMap<>();

    public UserData(@Nullable Long discordUserId, @Nullable UUID playerUUID) {
        if (discordUserId != null) this.discordUID = discordUserId;
        if (playerUUID != null) this.minecraftUID = playerUUID;
    }

    public UserData(@Nullable Long discordUserId, @Nullable UUID playerUUID, boolean banned, Map<Integer, SuggestionVote> votes) {
        this(discordUserId, playerUUID);
        this.banned = banned;
        this.votes.putAll(votes);
    }

    public UserData() {

    }

    public void updateVote(int id, SuggestionVote newVote) {
        votes.put(id, newVote);
    }

    public boolean matchUUID(UUID uuid) {
        return minecraftUID != null && minecraftUID.equals(uuid);
    }

    public boolean matchId(long id) {
        return discordUID != null && discordUID == id;
    }

}
