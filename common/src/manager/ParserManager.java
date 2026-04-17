package manager;

import java.math.BigInteger;
import java.util.function.Function;

public class ParserManager {

    public static Function<String, String> parseString = s -> s;

    public static <T extends Enum<T>>  Function<String, T> parseEnum(Class<T> enumClass) {
        return s -> {
            for (T value : enumClass.getEnumConstants()) {
                if (value.toString().equalsIgnoreCase(s)) {
                    return value;
                }
            }
            return null;
        };
    }

    public static Function<String, Long> parseLong = s -> {
        BigInteger bigInteger = new BigInteger(s);
        if (bigInteger.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            return Long.MAX_VALUE;
        }
        if (bigInteger.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
            return Long.MIN_VALUE;
        }
        return bigInteger.longValue();
    };

    public static Function<String, Integer> parseInteger = s -> {
        BigInteger bigInteger = new BigInteger(s);
        if (bigInteger.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            return Integer.MAX_VALUE;
        }
        if (bigInteger.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
            return Integer.MIN_VALUE;
        }
        return bigInteger.intValue();
    };

    public static Function<String, Double> parseDouble = s -> {
        Double parsedDouble = Double.parseDouble(s);
        if (parsedDouble.compareTo(Double.MAX_VALUE) > 0) {
            return Double.MAX_VALUE;
        }
        if (parsedDouble.compareTo(-Double.MAX_VALUE) < 0) {
            return -Double.MAX_VALUE;
        }
        return parsedDouble;
    };

    public static Function<String, Float> parseFloat = s -> {
        Float parsedFloat = Float.parseFloat(s);
        if (parsedFloat.compareTo(Float.MAX_VALUE) > 0) {
            return Float.MAX_VALUE;
        }
        if (parsedFloat.compareTo(-Float.MAX_VALUE) < 0) {
            return -Float.MAX_VALUE;
        }
        return parsedFloat;
    };
}
