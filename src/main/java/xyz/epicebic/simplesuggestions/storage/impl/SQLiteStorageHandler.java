package xyz.epicebic.simplesuggestions.storage.impl;

import me.epic.spigotlib.storage.SQliteConnectionPool;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.StorageHandler;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;
import xyz.epicebic.simplesuggestions.storage.data.UserData;

import java.util.*;
import java.util.function.Consumer;

/* TODO:
    2 tables, 1 for suggestion data, 1 for player data
    playerdata table format
    UUID | suggestionId | choice
 */
public class SQLiteStorageHandler extends StorageHandler {
    private int nextId = 1;
    private final SQliteConnectionPool connectionPool;
    private final SimpleSuggestions plugin;

    public SQLiteStorageHandler(SimpleSuggestions plugin) {
        super(plugin);
        this.plugin = plugin;
        this.connectionPool = new SQliteConnectionPool("SimpleSuggestions", "data.db", plugin.getDataFolder());
    }

    @Override
    public int saveSuggestion(SuggestionData data) {
        return 0;
    }

    @Override
    public SuggestionData readSuggestion(Integer id) {
        return null;
    }

    @Override
    public Map<Integer, SuggestionVote> getVotedSuggestions(UUID uuid) {
        return null;
    }

    @Override
    public void addVotedSuggestion(Integer id, UUID uuid, boolean choice) {

    }

    @Override
    public void removeVotedSuggestion(Integer id, UUID uuid) {

    }

    @Override
    public Map<Integer, SuggestionVote> getVotedSuggestions(Long id) {
        return null;
    }

    @Override
    public void addVotedSuggestion(Integer id, Long userId, boolean choice) {

    }

    @Override
    public void removeVotedSuggestion(Integer id, Long userId) {

    }

    @Override
    public void setSuggestionStatus(int id, SuggestionStatus newStatus) {

    }

    @Override
    public Map<Integer, SuggestionData> getSuggestions() {
        return null;
    }

    @Override
    public void save() {

    }

    @Override
    public Optional<UserData> getUserData(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public Optional<UserData> getUserData(Long id) {
        return Optional.empty();
    }

    @Override
    public void updateUserData(UUID uuid, Consumer<UserData> updatedData) {

    }

    @Override
    public void updateUserData(Long id, Consumer<UserData> updatedData) {

    }
}
