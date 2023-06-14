package xyz.epicebic.simplesuggestions.storage.impl;

import lombok.Cleanup;
import me.epic.spigotlib.storage.SQliteConnectionPool;
import xyz.epicebic.simplesuggestions.SimpleSuggestionsPlugin;
import xyz.epicebic.simplesuggestions.storage.StorageHandler;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;
import xyz.epicebic.simplesuggestions.storage.data.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/* TODO:
    2 tables, 1 for suggestion data, 1 for player data
    playerdata table format
    UUID | suggestionId | choice
 */
public class SQLiteStorageHandler extends StorageHandler {
    private int nextId = 1;
    private final SQliteConnectionPool connectionPool;
    private final SimpleSuggestionsPlugin plugin;

    private final Map<Integer, SuggestionData> suggestions = new HashMap<>();
    private final List<UserData> playerData = new ArrayList<>();

    public SQLiteStorageHandler(SimpleSuggestionsPlugin plugin) {
        this.plugin = plugin;
        this.connectionPool = new SQliteConnectionPool("SimpleSuggestionsPlugin", "data.db", plugin.getDataFolder());
    }

    public void createTables() {
        try (Connection connection = this.connectionPool.getConnection()) {
            @Cleanup PreparedStatement createPlayerDataTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'player-data' (" +
                    "");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int saveSuggestion(SuggestionData data) {
        int newId = getNextId();
        this.suggestions.put(newId, data);
        return newId;
    }

    @Override
    public SuggestionData readSuggestion(Integer id) {
        return this.suggestions.get(id);
    }

    @Override
    public Map<Integer, SuggestionVote> getVotedSuggestions(UUID uuid) {
        return getUserData(uuid).map(UserData::getVotes).orElse(Collections.emptyMap());
    }

    @Override
    public void addVotedSuggestion(Integer id, UUID uuid, boolean choice) {
        updateVotes(id, uuid, choice);
    }

    @Override
    public void removeVotedSuggestion(Integer id, UUID uuid) {
        removeVotes(id, uuid);
    }

    @Override
    public Map<Integer, SuggestionVote> getVotedSuggestions(Long id) {
        return getUserData(id).orElse(new UserData()).getVotes();
    }

    @Override
    public void addVotedSuggestion(Integer id, Long userId, boolean choice) {
        updateVotes(id, userId, choice);
    }

    @Override
    public void removeVotedSuggestion(Integer id, Long userId) {
        removeVotes(id, userId);
    }

    @Override
    public void setSuggestionStatus(int id, SuggestionStatus newStatus) {
        Optional<SuggestionData> suggestionData = Optional.ofNullable(this.suggestions.get(id));
        suggestionData.ifPresent(data -> {
            data.setStatus(newStatus);
            this.suggestions.put(id, data);
            plugin.getInventoryHandler().reloadSuggestionItem(id, data);
        });
    }

    @Override
    public Map<Integer, SuggestionData> getSuggestions() {
        return suggestions;
    }

    @Override
    public void save() {

    }

    @Override
    public Optional<UserData> getUserData(UUID uuid) {
        return this.playerData.stream().filter(userData -> userData.matchUUID(uuid)).findAny();
    }

    @Override
    public Optional<UserData> getUserData(Long id) {
        return this.playerData.stream().filter(userData -> userData.matchId(id)).findAny();
    }

    @Override
    public void updateUserData(UUID uuid, Consumer<UserData> updater) {
        Optional<UserData> userDataOptional = getUserData(uuid);

        if (userDataOptional.isPresent()) {
            updater.accept(userDataOptional.get());
        } else {
            UserData newUserData = new UserData(null, uuid);
            updater.accept(newUserData);
            this.playerData.add(newUserData);
        }
    }

    @Override
    public void updateUserData(Long id, Consumer<UserData> updatedData) {
        getUserData(id).ifPresent(updatedData);
    }


    public int getNextId() {
        return nextId++;
    }

    private void updateVotes(Integer id, Object userIdentifier, boolean choice) {
        Optional<UserData> userDataOpt;
        if (userIdentifier instanceof UUID uuid) {
            userDataOpt = getUserData(uuid);
        } else {
            userDataOpt = getUserData((Long) userIdentifier);
        }
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        userDataOpt.ifPresent(userData -> {
            previousVote.set(userData.getVotes().get(id));
            this.playerData.remove(userData);
            userData.updateVote(id, choice ? SuggestionVote.UPVOTE : SuggestionVote.DOWNVOTE);
            this.playerData.add(userData);
        });

        Optional<SuggestionData> suggestionDataOpt = Optional.ofNullable(this.suggestions.get(id));
        SuggestionVote vote = previousVote.get();
        if (vote != SuggestionVote.NOVOTE) {
            suggestionDataOpt.ifPresent(suggestionData -> {
                if (vote == SuggestionVote.UPVOTE) {
                    suggestionData.decreaseUpvote();
                } else {
                    suggestionData.decreaseDownvote();
                }
                this.suggestions.put(id, suggestionData);
                plugin.getInventoryHandler().reloadSuggestionItem(id, suggestionData);
            });
        }
    }

    private void removeVotes(Integer id, Object userIdentifier) {
        Optional<UserData> dataOpt;
        if (userIdentifier instanceof UUID uuid) {
            dataOpt = getUserData(uuid);
        } else {
            dataOpt = getUserData((Long) userIdentifier);
        }
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        dataOpt.ifPresent(userData -> {
            previousVote.set(userData.getVotes().get(id));
            this.playerData.remove(userData);
            userData.updateVote(id, SuggestionVote.NOVOTE);
            this.playerData.add(userData);
        });

        Optional<SuggestionData> suggestionDataOpt = Optional.ofNullable(this.suggestions.get(id));
        SuggestionVote vote = previousVote.get();
        if (vote != SuggestionVote.NOVOTE) {
            suggestionDataOpt.ifPresent(suggestionData -> {
                if (vote == SuggestionVote.UPVOTE) {
                    suggestionData.decreaseUpvote();
                } else {
                    suggestionData.decreaseDownvote();
                }
                this.suggestions.put(id, suggestionData);
                plugin.getInventoryHandler().reloadSuggestionItem(id, suggestionData);
            });
        }
    }
}
