package run.prizm.core.message.constraint;

public enum MessageType {
    TEXT,
    LINK,
    MEDIA,
    DOCUMENT,
    FILE;

    public static MessageType from(String value) {
        for (MessageType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported message type: " + value);
    }
}
