package xyz.epicebic.simplesuggestions.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageHandler {

    int saveSuggestion(SuggestionData data);
    CompletableFuture<SuggestionData> readSuggestion(Integer id);
    CompletableFuture<Map<Integer, SuggestionData>> readSuggestions(UUID uuid);
    CompletableFuture<Map<Integer, SuggestionData>> readSuggestions(Long id);

    Map<Integer, Boolean> getVotedSuggestions(UUID uuid);
    void addVotedSuggestion(Integer id, UUID uuid, boolean choice);
    void removeVotedSuggestion(Integer id, UUID uuid);

    CompletableFuture<Map<Integer, Boolean>> getVotedSuggestions(Long id);
    CompletableFuture<Void> addVotedSuggestion(Integer id, Long userId, boolean choice);
    void removeVotedSuggestion(Integer id, Long userId);

    Map<Integer, SuggestionData> getSuggestions();

    void save();

}
