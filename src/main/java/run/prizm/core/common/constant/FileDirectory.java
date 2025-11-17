package run.prizm.core.common.constant;

import lombok.Getter;

@Getter
public enum FileDirectory {
    WORKSPACE_PROFILES("workspace-profiles"),
    WORKSPACES("workspaces"),
    USER_PROFILES("profiles");

    private final String path;

    FileDirectory(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }
}