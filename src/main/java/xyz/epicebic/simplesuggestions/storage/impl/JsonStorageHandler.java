package xyz.epicebic.simplesuggestions.storage.impl;

import com.google.gson.*;
import lombok.SneakyThrows;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.StorageHandler;
import xyz.epicebic.simplesuggestions.storage.data.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class JsonStorageHandler implements StorageHandler {

    private int nextId = 1;
    private final Map<Integer, SuggestionData> suggestions = new HashMap<>();
    private final List<UserData> playerData = new ArrayList<>();
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final File jsonFile = new File(SimpleSuggestions.getInstance().getDataFolder(), "data.json");
    private final SimpleSuggestions plugin;

    @SneakyThrows
    @SuppressWarnings("null")
    public JsonStorageHandler(SimpleSuggestions plugin) {
        this.plugin = plugin;
        if (jsonFile.exists()) {
            suggestions.putAll(readFile());
        }
    }

    @Override
    public int saveSuggestion(SuggestionData data) {
        Integer newId = getNextId();
        suggestions.put(newId, data);
        return newId;
    }

    @Override
    public SuggestionData readSuggestion(Integer id) {
        return suggestions.get(id);
    }

    @Override
    public Map<Integer, SuggestionVote> getVotedSuggestions(UUID uuid) {
        return getUserData(uuid).orElse(new UserData()).getVotes();
    }

    @Override
    public void addVotedSuggestion(Integer id, UUID uuid, boolean choice) {
        // Player data
        UserData data = getUserData(uuid).orElse(new UserData(null, uuid));
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        SuggestionVote oldVote = data.getVotes().get(id);
        previousVote.set(oldVote == null ? SuggestionVote.NOVOTE : oldVote);
        playerData.remove(data);
        data.updateVote(Origin.MINECRAFT, id, choice ? SuggestionVote.UPVOTE : SuggestionVote.DOWNVOTE);
        playerData.add(data);

        // Suggestion data
        Optional<SuggestionData> suggestionDataOpt = Optional.ofNullable(suggestions.get(id));
        suggestionDataOpt.ifPresent(suggestionData -> {
            SuggestionVote vote = previousVote.get();
            switch (vote) {
                case UPVOTE -> suggestionData.decreaseUpvote();
                case DOWNVOTE -> suggestionData.decreaseDownvote();
            }
            if (choice) {
                suggestionData.increaseUpvote();
            } else {
                suggestionData.increaseDownvote();
            }
            suggestions.put(id, suggestionData);

            // Reload open invs
            plugin.getInventoryHandler().reloadSuggestionItem(id, suggestionData);
        });
    }

    @Override
    public void removeVotedSuggestion(Integer id, UUID uuid) {
        // Player data
        Optional<UserData> dataOpt = getUserData(uuid);
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        dataOpt.ifPresent(userData -> {
            previousVote.set(userData.getVotes().get(id));
            playerData.remove(userData);
            userData.updateVote(Origin.MINECRAFT,id, SuggestionVote.NOVOTE);
            playerData.add(userData);
        });

        // Suggestion data
        Optional<SuggestionData> suggestionDataOpt = Optional.ofNullable(suggestions.get(id));
        SuggestionVote vote = previousVote.get();
        if (vote != SuggestionVote.NOVOTE) {
            suggestionDataOpt.ifPresent(suggestionData -> {
                if (vote == SuggestionVote.UPVOTE) {
                    suggestionData.decreaseUpvote();
                } else {
                    suggestionData.decreaseDownvote();
                }
                suggestions.put(id, suggestionData);

                // Reload open inventories
                plugin.getInventoryHandler().reloadSuggestionItem(id, suggestionData);
            });
        }
    }

    @Override
    public Map<Integer, SuggestionVote> getVotedSuggestions(Long id) {
        return getUserData(id).orElse(new UserData()).getVotes();
    }

    @Override
    public void addVotedSuggestion(Integer id, Long userId, boolean choice) {
        // User data
        UserData data = getUserData(userId).orElse(new UserData(userId, null));
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        SuggestionVote oldVote = data.getVotes().get(id);
        previousVote.set(oldVote == null ? SuggestionVote.NOVOTE : oldVote);
        playerData.remove(data);
        data.updateVote(Origin.DISCORD, id, choice ? SuggestionVote.UPVOTE : SuggestionVote.DOWNVOTE);
        playerData.add(data);

        // Suggestion data
        Optional<SuggestionData> suggestionDataOpt = Optional.ofNullable(suggestions.get(id));
        suggestionDataOpt.ifPresent(suggestionData -> {
            SuggestionVote vote = previousVote.get();
            switch (vote) {
                case UPVOTE -> suggestionData.decreaseUpvote();
                case DOWNVOTE -> suggestionData.decreaseDownvote();
            }
            if (choice) {
                suggestionData.increaseUpvote();
            } else {
                suggestionData.increaseDownvote();
            }
            suggestions.put(id, suggestionData);

            // Reload open invs
            plugin.getInventoryHandler().reloadSuggestionItem(id, suggestionData);
        });
    }

    @Override
    public void removeVotedSuggestion(Integer id, Long userId) {
        // User data
        Optional<UserData> dataOpt = getUserData(userId);
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        dataOpt.ifPresent(userData -> {
            previousVote.set(userData.getVotes().get(id));
            playerData.remove(userData);
            userData.updateVote(Origin.DISCORD,id, SuggestionVote.NOVOTE);
            playerData.add(userData);
        });

        // Suggestion data
        Optional<SuggestionData> suggestionDataOpt = Optional.ofNullable(suggestions.get(id));
        SuggestionVote vote = previousVote.get();
        if (vote != SuggestionVote.NOVOTE) {
            suggestionDataOpt.ifPresent(suggestionData -> {
                if (vote == SuggestionVote.UPVOTE) {
                    suggestionData.decreaseUpvote();
                } else {
                    suggestionData.decreaseDownvote();
                }
                suggestions.put(id, suggestionData);

                // Reload open inventories
                plugin.getInventoryHandler().reloadSuggestionItem(id, suggestionData);
            });
        }
    }

    @Override
    public void setSuggestionStatus(int id, SuggestionStatus newStatus) {
        Optional<SuggestionData> suggestionData = Optional.ofNullable(suggestions.get(id));
        suggestionData.ifPresent(data -> {
            data.setStatus(newStatus);
            suggestions.put(id, data);

            // Reload open inventories
            plugin.getInventoryHandler().reloadSuggestionItem(id, data);
        });
    }

    @Override
    public Map<Integer, SuggestionData> getSuggestions() {
        return suggestions;
    }

    @Override
    public void save() {
        JsonObject jsonObject = new JsonObject();

        // Save suggestions map
        JsonObject suggestionsObject = new JsonObject();
        for (Map.Entry<Integer, SuggestionData> entry : suggestions.entrySet()) {
            suggestionsObject.add(String.valueOf(entry.getKey()), gson.toJsonTree(entry.getValue()));
        }
        jsonObject.add("suggestions", suggestionsObject);

        // Save playerData list
        JsonArray playerDataArray = new JsonArray();
        for (UserData userData : playerData) {
            JsonObject userDataObject = new JsonObject();
            userDataObject.add("discordUserData", gson.toJsonTree(userData.getDiscordUserData()));
            userDataObject.add("playerData", gson.toJsonTree(userData.getPlayerData()));
            playerDataArray.add(userDataObject);
        }
        jsonObject.add("playerData", playerDataArray);

        try {
            Files.writeString(jsonFile.toPath(), gson.toJson(jsonObject));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Optional<UserData> getUserData(UUID uuid) {
        return playerData.stream().filter(userData -> userData.matchUUID(uuid)).findAny();
    }

    @Override
    public Optional<UserData> getUserData(Long id) {
        return playerData.stream().filter(userData -> userData.matchId(id)).findAny();
    }

    public Integer getNextId() {
        return nextId++;
    }

    @SneakyThrows
    private Map<Integer, SuggestionData> readFile() {
        String json = Files.readString(jsonFile.toPath());
        JsonElement element = JsonParser.parseString(json);
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            // Read suggestions map
            Map<Integer, SuggestionData> suggestionDataMap = new HashMap<>();
            if (jsonObject.has("suggestions")) {
                JsonObject suggestionsObject = jsonObject.getAsJsonObject("suggestions");
                for (Map.Entry<String, JsonElement> entry : suggestionsObject.entrySet()) {
                    int id = Integer.parseInt(entry.getKey());
                    SuggestionData data = gson.fromJson(entry.getValue(), SuggestionData.class);
                    nextId = id + 1;
                    suggestionDataMap.put(id, data);
                }
            }

            // Read playerData list
            List<UserData> playerDataList = new ArrayList<>();
            if (jsonObject.has("playerData")) {
                JsonArray playerDataArray = jsonObject.getAsJsonArray("playerData");
                for (JsonElement jsonElement : playerDataArray) {
                    JsonObject userDataObject = jsonElement.getAsJsonObject();
                    DiscordUserData discordUserData = gson.fromJson(userDataObject.get("discordUserData"), DiscordUserData.class);
                    PlayerData playerData = gson.fromJson(userDataObject.get("playerData"), PlayerData.class);
                    playerDataList.add(new UserData(discordUserData, playerData));
                }
            }

            this.playerData.addAll(playerDataList);

            return suggestionDataMap;
        }
        throw new IllegalArgumentException("The json is misformatted. Regenerate the file");
    }
}
