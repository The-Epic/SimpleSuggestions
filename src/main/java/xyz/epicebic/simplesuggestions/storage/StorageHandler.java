package xyz.epicebic.simplesuggestions.storage;

import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;
import xyz.epicebic.simplesuggestions.storage.data.UserData;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class StorageHandler {


    public abstract int saveSuggestion(SuggestionData data);
    public abstract SuggestionData readSuggestion(Integer id);

    public abstract Map<Integer, SuggestionVote> getVotedSuggestions(UUID uuid);
    public abstract void addVotedSuggestion(Integer id, UUID uuid, boolean choice);
    public abstract void removeVotedSuggestion(Integer id, UUID uuid);

    public abstract Map<Integer, SuggestionVote> getVotedSuggestions(Long id);
    public abstract void addVotedSuggestion(Integer id, Long userId, boolean choice);
    public abstract void removeVotedSuggestion(Integer id, Long userId);

    public abstract void setSuggestionStatus(int id, SuggestionStatus newStatus);

    public abstract Map<Integer, SuggestionData> getSuggestions();

    public abstract void save();
    public abstract Optional<UserData> getUserData(UUID uuid);
    public abstract Optional<UserData> getUserData(Long id);

    public abstract void updateUserData(UUID uuid, Consumer<UserData> updatedData);
    public abstract void updateUserData(Long id, Consumer<UserData> updatedData);
}
