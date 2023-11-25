package cz.muni.ics.kypo.training.utils;

import java.util.Set;

/**
 * Prefixes which should be included with the command. Example: sudo ls
 */
public abstract class AbstractCommandPrefixes {
    private static final Set<String> PREFIXES = Set.of("sudo");

    public static boolean isPrefix(String prefix) {
        return PREFIXES.contains(prefix);
    }
}
