package run.prizm.core.common.constraint;

public enum Language {
    KO,
    EN,
    JA,
    FR;

    public static Language from(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Language value is empty");
        }
        for (Language language : values()) {
            if (language.name().equalsIgnoreCase(value)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unsupported language: " + value);
    }
}
