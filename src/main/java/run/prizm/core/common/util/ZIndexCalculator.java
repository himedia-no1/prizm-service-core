package run.prizm.core.common.util;

import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;

import java.math.BigDecimal;

public final class ZIndexCalculator {

    private ZIndexCalculator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static BigDecimal calculateFirst(BigDecimal firstZIndex) {
        return firstZIndex.divide(BigDecimal.valueOf(2));
    }

    public static BigDecimal calculateLast(BigDecimal lastZIndex) {
        return lastZIndex.add(BigDecimal.ONE);
    }

    public static BigDecimal calculateBetween(BigDecimal beforeZIndex, BigDecimal afterZIndex) {
        return beforeZIndex.add(afterZIndex)
                           .divide(BigDecimal.valueOf(2));
    }

    public static BigDecimal calculateNewPosition(String position, BigDecimal firstZIndex, BigDecimal lastZIndex,
                                                  BigDecimal beforeZIndex, BigDecimal afterZIndex) {
        return switch (position) {
            case "FIRST" -> calculateFirst(firstZIndex);
            case "LAST" -> calculateLast(lastZIndex);
            case "BETWEEN" -> calculateBetween(beforeZIndex, afterZIndex);
            default -> throw new BusinessException(ErrorCode.INVALID_POSITION);
        };
    }
}