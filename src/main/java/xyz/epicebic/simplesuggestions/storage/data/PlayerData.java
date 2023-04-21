package xyz.epicebic.simplesuggestions.storage.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    @Getter
    private UUID playerUUID;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Getter
    private Map<Integer, SuggestionVote> votes = new HashMap<>();

    public void updateVote(int id, SuggestionVote newVote) {
        votes.put(id, newVote);
    }

}