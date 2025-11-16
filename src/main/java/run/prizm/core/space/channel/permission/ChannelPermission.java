package run.prizm.core.space.channel.permission;

import lombok.Getter;

@Getter
public enum ChannelPermission {
    NONE(0),
    READ(1),
    WRITE(2),
    MANAGE(3);

    private final int level;

    ChannelPermission(int level) {
        this.level = level;
    }

    public static ChannelPermission max(ChannelPermission a, ChannelPermission b) {
        return a.level >= b.level ? a : b;
    }
}