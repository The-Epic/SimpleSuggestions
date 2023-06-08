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
import java.util.function.Consumer;

public class JsonStorageHandler extends StorageHandler {

    private int nextId = 1;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final File jsonFile = new File(SimpleSuggestions.getInstance().getDataFolder(), "data.json");
    private final SimpleSuggestions plugin;

    @SneakyThrows
    @SuppressWarnings("null")
    public JsonStorageHandler(SimpleSuggestions plugin) {
        super(plugin);
        this.plugin = plugin;
        if (jsonFile.exists()) {
            super.suggestions.putAll(readFile());
        }
    }

    @Override
    public int saveSuggestion(SuggestionData data) {
        int newId = getNextId();
        super.suggestions.put(newId, data);
        return newId;
    }

    @Override
    public SuggestionData readSuggestion(Integer id) {
        return super.suggestions.get(id);
    }

    @Override
    public Map<Integer, SuggestionVote> getVotedSuggestions(UUID uuid) {
        return getUserData(uuid).map(UserData::getVotes).orElse(Collections.emptyMap());
    }

    @Override
    public void addVotedSuggestion(Integer id, UUID uuid, boolean choice) {
        updateVotes(id, uuid, choice, Origin.MINECRAFT);
    }

    @Override
    public void removeVotedSuggestion(Integer id, UUID uuid) {
        removeVotes(id, uuid, Origin.MINECRAFT);
    }

    @Override
    public Map<Integer, SuggestionVote> getVotedSuggestions(Long id) {
        return getUserData(id).orElse(new UserData()).getVotes();
    }

    @Override
    public void addVotedSuggestion(Integer id, Long userId, boolean choice) {
        updateVotes(id, userId, choice, Origin.DISCORD);
    }

    @Override
    public void removeVotedSuggestion(Integer id, Long userId) {
        removeVotes(id, userId, Origin.DISCORD);
    }

    @Override
    public void setSuggestionStatus(int id, SuggestionStatus newStatus) {
        Optional<SuggestionData> suggestionData = Optional.ofNullable(super.suggestions.get(id));
        suggestionData.ifPresent(data -> {
            data.setStatus(newStatus);
            super.suggestions.put(id, data);
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
        saveSuggestions(jsonObject);
        savePlayerData(jsonObject);
        try {
            Files.writeString(jsonFile.toPath(), gson.toJson(jsonObject));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Optional<UserData> getUserData(UUID uuid) {
        return super.playerData.stream().filter(userData -> userData.matchUUID(uuid)).findAny();
    }

    @Override
    public Optional<UserData> getUserData(Long id) {
        return super.playerData.stream().filter(userData -> userData.matchId(id)).findAny();
    }

    @Override
    public void updateUserData(UUID uuid, Consumer<UserData> updater) {
        Optional<UserData> userDataOptional = getUserData(uuid);

        if (userDataOptional.isPresent()) {
            updater.accept(userDataOptional.get());
        } else {
            UserData newUserData = new UserData(null, uuid);
            updater.accept(newUserData);
            super.playerData.add(newUserData);
        }
    }

    @Override
    public void updateUserData(Long id, Consumer<UserData> updatedData) {
        getUserData(id).ifPresent(updatedData);
    }

    public int getNextId() {
        return nextId++;
    }

    @SneakyThrows
    private Map<Integer, SuggestionData> readFile() {
        String json = Files.readString(jsonFile.toPath());
        JsonElement element = JsonParser.parseString(json);
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            Map<Integer, SuggestionData> suggestionDataMap = readSuggestions(jsonObject);
            List<UserData> playerDataList = readPlayerData(jsonObject);

            super.playerData.addAll(playerDataList);

            return suggestionDataMap;
        }
        throw new IllegalArgumentException("The json is misformatted. Regenerate the file");
    }

    private void updateVotes(Integer id, Object userIdentifier, boolean choice, Origin origin) {
        Optional<UserData> userDataOpt;
        if (userIdentifier instanceof UUID uuid) {
            userDataOpt = getUserData(uuid);
        } else {
            userDataOpt = getUserData((Long) userIdentifier);
        }
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        userDataOpt.ifPresent(userData -> {
            previousVote.set(userData.getVotes().get(id));
            super.playerData.remove(userData);
            userData.updateVote(origin, id, choice ? SuggestionVote.UPVOTE : SuggestionVote.DOWNVOTE);
            super.playerData.add(userData);
        });

        Optional<SuggestionData> suggestionDataOpt = Optional.ofNullable(super.suggestions.get(id));
        SuggestionVote vote = previousVote.get();
        if (vote != SuggestionVote.NOVOTE) {
            suggestionDataOpt.ifPresent(suggestionData -> {
                if (vote == SuggestionVote.UPVOTE) {
                    suggestionData.decreaseUpvote();
                } else {
                    suggestionData.decreaseDownvote();
                }
                super.suggestions.put(id, suggestionData);
                plugin.getInventoryHandler().reloadSuggestionItem(id, suggestionData);
            });
        }
    }

    private void removeVotes(Integer id, Object userIdentifier, Origin origin) {
        Optional<UserData> dataOpt;
        if (userIdentifier instanceof UUID uuid) {
            dataOpt = getUserData(uuid);
        } else {
            dataOpt = getUserData((Long) userIdentifier);
        }
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        dataOpt.ifPresent(userData -> {
            previousVote.set(userData.getVotes().get(id));
            super.playerData.remove(userData);
            userData.updateVote(origin, id, SuggestionVote.NOVOTE);
            super.playerData.add(userData);
        });

        Optional<SuggestionData> suggestionDataOpt = Optional.ofNullable(super.suggestions.get(id));
        SuggestionVote vote = previousVote.get();
        if (vote != SuggestionVote.NOVOTE) {
            suggestionDataOpt.ifPresent(suggestionData -> {
                if (vote == SuggestionVote.UPVOTE) {
                    suggestionData.decreaseUpvote();
                } else {
                    suggestionData.decreaseDownvote();
                }
                super.suggestions.put(id, suggestionData);
                plugin.getInventoryHandler().reloadSuggestionItem(id, suggestionData);
            });
        }
    }

    private void saveSuggestions(JsonObject jsonObject) {
        JsonObject suggestionsObject = new JsonObject();
        for (Map.Entry<Integer, SuggestionData> entry : super.suggestions.entrySet()) {
            suggestionsObject.add(String.valueOf(entry.getKey()), gson.toJsonTree(entry.getValue()));
        }
        jsonObject.add("suggestions", suggestionsObject);
    }

    private void savePlayerData(JsonObject jsonObject) {
        JsonArray playerDataArray = new JsonArray();
        for (UserData userData : playerData) {
            JsonObject userDataObject = new JsonObject();
            userDataObject.add("discordUserData", gson.toJsonTree(userData.getDiscordUserData()));
            userDataObject.add("playerData", gson.toJsonTree(userData.getPlayerData()));
            playerDataArray.add(userDataObject);
        }
        jsonObject.add("playerData", playerDataArray);
    }


    private Map<Integer, SuggestionData> readSuggestions(JsonObject jsonObject) {
        Map<Integer, SuggestionData> suggestionDataMap = new HashMap<>();
        if (jsonObject.has("suggestions")) {
            JsonObject suggestionsObject = jsonObject.getAsJsonObject("suggestions");
            for (Map.Entry<String, JsonElement> entry : suggestionsObject.entrySet()) {
                int id = Integer.parseInt(entry.getKey());
                SuggestionData data = gson.fromJson(entry.getValue(), SuggestionData.class);
                nextId = Math.max(nextId, id + 1);
                suggestionDataMap.put(id, data);
            }
        }
        return suggestionDataMap;
    }

    private List<UserData> readPlayerData(JsonObject jsonObject) {
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
        return playerDataList;
    }
}