package run.prizm.core.space.category.dto;

public record CategoryZIndexUpdateRequest(
        String position,
        Long beforeId,
        Long afterId
) {}
