package xyz.epicebic.simplesuggestions.storage.impl;

import com.google.gson.*;
import lombok.SneakyThrows;
import xyz.epicebic.simplesuggestions.SimpleSuggestions;
import xyz.epicebic.simplesuggestions.storage.StorageHandler;
import xyz.epicebic.simplesuggestions.storage.SuggestionData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JsonStorageHandler implements StorageHandler {

    private int nextId = 1;
    private final Map<Integer, SuggestionData> suggestions = new HashMap<>();
    private final Map<UUID, Map<Integer, Boolean>> playerData = new HashMap<>();
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final File jsonFile = new File(SimpleSuggestions.getInstance().getDataFolder(), "data.json");

    @SneakyThrows
    @SuppressWarnings("null")
    public JsonStorageHandler() {
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
        SuggestionData data = suggestions.get(id);
        return data;
    }

    @Override
    public Map<Integer, SuggestionData> readSuggestions(UUID uuid) {
//        return CompletableFuture.supplyAsync(() -> {
//            Map
//            for (Map.Entry<Integer, SuggestionData> entry : suggestions.entrySet()) {
//                if (entry.getValue().getSuggestionType() == SuggestionData.Type.MINECRAFT) {
//
//                }
//            }
//        });
        return null;
    }

    @Override
    public Map<Integer, SuggestionData> readSuggestions(Long id) {
        return null;
    }

    @Override
    public Map<Integer, Boolean> getVotedSuggestions(UUID uuid) {
        return playerData.get(uuid);
    }

    @Override
    public void addVotedSuggestion(Integer id, UUID uuid, boolean choice) {
        Map<Integer, Boolean> data = playerData.getOrDefault(uuid, new HashMap<>());
        data.put(id, choice);
        playerData.put(uuid, data);
    }

    @Override
    public void removeVotedSuggestion(Integer id, UUID uuid) {
        Map<Integer, Boolean> data = playerData.getOrDefault(uuid, new HashMap<>());
        data.remove(id);
        playerData.put(uuid, data);
    }

    @Override
    public Map<Integer, Boolean> getVotedSuggestions(Long id) {
        return null;
    }

    @Override
    public void addVotedSuggestion(Integer id, Long userId, boolean choice) {
        return;
    }

    @Override
    public void removeVotedSuggestion(Integer id, Long userId) {

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

        // Save playerData map
        JsonObject playerDataObject = new JsonObject();
        for (Map.Entry<UUID, Map<Integer, Boolean>> entry : playerData.entrySet()) {
            String uuidString = entry.getKey().toString();
            JsonObject playerObject = new JsonObject();
            for (Map.Entry<Integer, Boolean> innerEntry : entry.getValue().entrySet()) {
                playerObject.addProperty(String.valueOf(innerEntry.getKey()), innerEntry.getValue());
            }
            playerDataObject.add(uuidString, playerObject);
        }
        jsonObject.add("playerData", playerDataObject);

        try {
            Files.writeString(jsonFile.toPath(), gson.toJson(jsonObject));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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

            // Read playerData map
            Map<UUID, Map<Integer, Boolean>> playerDataMap = new HashMap<>();
            if (jsonObject.has("playerData")) {
                JsonObject playerDataObject = jsonObject.getAsJsonObject("playerData");
                for (Map.Entry<String, JsonElement> entry : playerDataObject.entrySet()) {
                    UUID uuid = UUID.fromString(entry.getKey());
                    JsonObject playerObject = entry.getValue().getAsJsonObject();
                    Map<Integer, Boolean> innerMap = new HashMap<>();
                    for (Map.Entry<String, JsonElement> innerEntry : playerObject.entrySet()) {
                        int suggestionId = Integer.parseInt(innerEntry.getKey());
                        boolean voted = innerEntry.getValue().getAsBoolean();
                        innerMap.put(suggestionId, voted);
                    }
                    playerDataMap.put(uuid, innerMap);
                }
            }

            this.playerData.putAll(playerDataMap);

            return suggestionDataMap;
        }
        throw new IllegalArgumentException("The json is misformatted. Regenerate the file");
    }
}
