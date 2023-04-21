package xyz.epicebic.simplesuggestions.storage.data;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class DiscordUserData  {
    @Getter
    private long discordUserId;

    public DiscordUserData(long discordUserId) {
        this.discordUserId = discordUserId;
    }

    @Getter
    private Map<Integer, SuggestionVote> votes = new HashMap<>();


    public void updateVote(int id, SuggestionVote newVote) {
        votes.put(id, newVote);
    }
}