package manager;

import java.util.HashSet;
import java.util.Set;

public class IdManager {

    private static final Set<Long> usedIds = new HashSet<>();

    public static Long generateId() {
        for (Long i = 1L; i < Long.MAX_VALUE; i++) {
            if (!usedIds.contains(i)) {
                usedIds.add(i);
                return i;
            }
        }
        return null;
    }

    public static void removeFromUsedIds(Long id) {
        usedIds.remove(id);
    }
}
