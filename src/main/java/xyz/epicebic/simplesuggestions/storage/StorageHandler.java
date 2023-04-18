package xyz.epicebic.simplesuggestions.storage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageHandler {

    CompletableFuture<Integer> saveSuggestion(SuggestionData data);
    CompletableFuture<SuggestionData> readSuggestion(Integer id);
    CompletableFuture<List<SuggestionData>> readSuggestions(UUID uuid);
    CompletableFuture<List<SuggestionData>> readSuggestions(Long id);

    CompletableFuture<Map<Integer, Boolean>> getVotedSuggestions(UUID uuid);
    CompletableFuture<Void> addVotedSuggestiono(Integer id, UUID uuid);

    void save();

}
