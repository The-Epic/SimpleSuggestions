package xyz.epicebic.simplesuggestions.storage.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import xyz.epicebic.simplesuggestions.SimpleSuggestionsPlugin;
import xyz.epicebic.simplesuggestions.storage.StorageHandler;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionData;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionStatus;
import xyz.epicebic.simplesuggestions.storage.data.SuggestionVote;
import xyz.epicebic.simplesuggestions.storage.data.UserData;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class JsonStorageHandler extends StorageHandler {

    private int nextId = 1;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final File jsonFile = new File(SimpleSuggestionsPlugin.getInstance().getDataFolder(), "data.json");

    private final Map<Integer, SuggestionData> suggestions = new HashMap<>();
    private final List<UserData> playerData = new ArrayList<>();

    private final SimpleSuggestionsPlugin plugin;

    @SneakyThrows
    @SuppressWarnings("null")
    public JsonStorageHandler(SimpleSuggestionsPlugin plugin) {
        this.plugin = plugin;
        if (jsonFile.exists()) {
            this.suggestions.putAll(readFile());
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
        return this.playerData.stream().filter(userData -> userData.matchUUID(uuid)).findAny();
    }

    @Override
    public Optional<UserData> getUserData(Long id) {
        return Optional.of(this.playerData.stream().filter(userData -> userData.matchId(id)).findAny().orElse(new UserData(id, null)));
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

    @SneakyThrows
    private Map<Integer, SuggestionData> readFile() {
        String json = Files.readString(jsonFile.toPath());
        JsonElement element = JsonParser.parseString(json);
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();

            Map<Integer, SuggestionData> suggestionDataMap = readSuggestions(jsonObject);
            List<UserData> playerDataList = readPlayerData(jsonObject);

            this.playerData.addAll(playerDataList);

            return suggestionDataMap;
        }
        throw new IllegalArgumentException("The json is misformatted. Regenerate the file");
    }

    private void updateVotes(Integer id, Object userIdentifier, boolean choice) {
        UserData data;
        if (userIdentifier instanceof UUID uuid) {
            data = getUserData(uuid).orElse(new UserData(null, uuid));
        } else {
            data = getUserData((Long) userIdentifier).orElse(new UserData((Long) userIdentifier, null));
        }
        AtomicReference<SuggestionVote> previousVote = new AtomicReference<>(SuggestionVote.NOVOTE);
        SuggestionVote oldVote = data.getVotes().get(id);
        previousVote.set(oldVote == null ? SuggestionVote.NOVOTE : oldVote);
        playerData.remove(data);
        data.updateVote(id, choice ? SuggestionVote.UPVOTE : SuggestionVote.DOWNVOTE);
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
            playerData.remove(userData);
            userData.updateVote(id, SuggestionVote.NOVOTE);
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

    private void saveSuggestions(JsonObject jsonObject) {
        JsonObject suggestionsObject = new JsonObject();
        for (Map.Entry<Integer, SuggestionData> entry : this.suggestions.entrySet()) {
            suggestionsObject.add(String.valueOf(entry.getKey()), gson.toJsonTree(entry.getValue()));
        }
        jsonObject.add("suggestions", suggestionsObject);
    }

    private void savePlayerData(JsonObject jsonObject) {
        JsonArray playerDataArray = new JsonArray();
        for (UserData userData : playerData) {
            JsonObject userDataObject = new JsonObject();
            UUID minecraftUID = userData.getMinecraftUID();
            Long discordUID = userData.getDiscordUID();
            if (minecraftUID != null) userDataObject.addProperty("minecraftUID", minecraftUID.toString());
            if (discordUID != null) userDataObject.addProperty("discordUID", discordUID.toString());

            userDataObject.addProperty("banned", userData.isBanned());
            userDataObject.add("votes", gson.toJsonTree(userData.getVotes()));
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
                UUID minecraftUID = null;
                if (userDataObject.has("minecraftUID")) minecraftUID = UUID.fromString(userDataObject.getAsJsonPrimitive("minecraftUID").getAsString());
                Long discordUID = null;
                if (userDataObject.has("discordUID")) discordUID = Long.valueOf(userDataObject.getAsJsonPrimitive("discordUID").getAsString());

                Type voteMapType = new TypeToken<Map<Integer, SuggestionVote>>() {}.getType();
                JsonObject votesObject = userDataObject.getAsJsonObject("votes");
                Map<Integer, SuggestionVote> votes = gson.fromJson(votesObject, voteMapType);
                playerDataList.add(new UserData(discordUID, minecraftUID, userDataObject.getAsJsonPrimitive("banned").getAsBoolean(), votes));
            }
        }
        return playerDataList;
    }
}