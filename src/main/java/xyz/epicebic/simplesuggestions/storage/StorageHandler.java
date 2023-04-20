package xyz.epicebic.simplesuggestions.storage;

import java.util.Map;
import java.util.UUID;

public interface StorageHandler {

    int saveSuggestion(SuggestionData data);
    SuggestionData readSuggestion(Integer id);
    Map<Integer, SuggestionData> readSuggestions(UUID uuid);
    Map<Integer, SuggestionData> readSuggestions(Long id);

    Map<Integer, Boolean> getVotedSuggestions(UUID uuid);
    void addVotedSuggestion(Integer id, UUID uuid, boolean choice);
    void removeVotedSuggestion(Integer id, UUID uuid);

    Map<Integer, Boolean> getVotedSuggestions(Long id);
    void addVotedSuggestion(Integer id, Long userId, boolean choice);
    void removeVotedSuggestion(Integer id, Long userId);

    Map<Integer, SuggestionData> getSuggestions();

    void save();

}
