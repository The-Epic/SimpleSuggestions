package xyz.epicebic.simplesuggestions.storage;

import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;
import xyz.epicebic.simplesuggestions.storage.data.UserData;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface StorageHandler {

    int saveSuggestion(SuggestionData data);
    SuggestionData readSuggestion(Integer id);

    Map<Integer, SuggestionVote> getVotedSuggestions(UUID uuid);
    void addVotedSuggestion(Integer id, UUID uuid, boolean choice);
    void removeVotedSuggestion(Integer id, UUID uuid);

    Map<Integer, SuggestionVote> getVotedSuggestions(Long id);
    void addVotedSuggestion(Integer id, Long userId, boolean choice);
    void removeVotedSuggestion(Integer id, Long userId);

    void setSuggestionStatus(int id, SuggestionStatus newStatus);

    Map<Integer, SuggestionData> getSuggestions();

    void save();
    Optional<UserData> getUserData(UUID uuid);
    Optional<UserData> getUserData(Long id);

}
