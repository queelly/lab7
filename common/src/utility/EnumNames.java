package utility;

import java.util.Arrays;

public class EnumNames {
    public static <E extends Enum<E>> String names(Class<E> enumClass) {
        return Arrays.toString(enumClass.getEnumConstants());
    }
}
