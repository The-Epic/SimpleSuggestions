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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class JsonStorageHandler implements StorageHandler {

    private int nextId = 1;
    private final Map<Integer, SuggestionData> suggestions = new HashMap<>();
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final File jsonFile = new File(SimpleSuggestions.getInstance().getDataFolder(), "suggestionData.json");

    @SneakyThrows
    @SuppressWarnings("null")
    public JsonStorageHandler() {
        if (jsonFile.exists()) {
            suggestions.putAll(readFile());
        }
    }

    @Override
    public CompletableFuture<Integer> saveSuggestion(SuggestionData data) {
        return CompletableFuture.supplyAsync(() -> {
            Integer newId = getNextId();
            suggestions.put(newId, data);

            return newId;
        });
    }

    @Override
    public CompletableFuture<SuggestionData> readSuggestion(Integer id) {
        SuggestionData data = suggestions.get(id);
        return CompletableFuture.supplyAsync(() -> {
            if (data != null) return data;
            return readFile().get(id);
        });
    }

    @Override
    public CompletableFuture<List<SuggestionData>> readSuggestions(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<List<SuggestionData>> readSuggestions(Long id) {
        return null;
    }

    @Override
    public CompletableFuture<Map<Integer, Boolean>> getVotedSuggestions(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Void> addVotedSuggestiono(Integer id, UUID uuid) {
        return null;
    }

    @Override
    public void save() {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<Integer, SuggestionData> entry : suggestions.entrySet()) {
            jsonObject.add(String.valueOf(entry.getKey()), gson.toJsonTree(entry.getValue()));
        }
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
        JsonElement element = new JsonParser().parse(json);
        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            Map<Integer, SuggestionData> suggestionDataMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                int id = Integer.parseInt(entry.getKey());
                SuggestionData data = gson.fromJson(entry.getValue(), SuggestionData.class);
                nextId = id + 1;
                suggestionDataMap.put(id, data);
            }
            return suggestionDataMap;
        }
        throw new IllegalArgumentException("The json is misformatted. Regenerate the file");
    }
}
