package xyz.epicebic.simplesuggestions.storage;


import java.util.UUID;

public class SuggestionData {
    private Type suggestionType;
    private Status status;

    private UUID ownerUUID;
    private Long ownerID;
    private String suggestion;


    public enum Type {
        MINECRAFT,
        DISCORD
    }

    public enum Status {
        APPROVED,
        DENIED,
        CONSIDERED,
        IMPLEMENTED,
        WAITING
    }
}
