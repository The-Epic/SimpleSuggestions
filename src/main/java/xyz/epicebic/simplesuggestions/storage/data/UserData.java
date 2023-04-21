package xyz.epicebic.simplesuggestions.storage.data;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class UserData {
    private DiscordUserData discordUserData;
    private PlayerData playerData;

    public UserData(@Nullable Long discordUserId, @Nullable UUID playerUUID) {
        if (discordUserId != null) this.discordUserData = new DiscordUserData(discordUserId);
        if (playerUUID != null) this.playerData = new PlayerData(playerUUID);
    }

    public UserData(DiscordUserData discordUserData, PlayerData playerData) {
        this.playerData = playerData;
        this.discordUserData = discordUserData;
    }

    public UserData() {

    }

    public Map<Integer, SuggestionVote> getVotes() {
        Map<Integer, SuggestionVote> toReturn = new HashMap<>();
        if (discordUserData != null) {
            toReturn.putAll(discordUserData.getVotes());
        }
        if (playerData != null) {
            toReturn.putAll(playerData.getVotes());
        }
        return toReturn;
    }

    public void updateVote(Origin origin, int id, SuggestionVote newVote) {
        switch (origin) {
            case DISCORD -> discordUserData.updateVote(id, newVote);
            case MINECRAFT -> playerData.updateVote(id, newVote);
        }
    }

    public boolean matchUUID(UUID uuid) {
        return playerData != null && playerData.getPlayerUUID().equals(uuid);
    }


}
