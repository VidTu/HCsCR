package ru.femboypve.hcscr;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Batching mode.
 *
 * @author VidTu
 */
public enum Batching {
    DISABLED("disabled"),
    CONTAINING("containing"),
    CONTAINING_CONTAINED("containingContained"),
    INTERSECTING("intersecting");

    private static final Map<String, Batching> BY_ID = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(Batching::id, Function.identity()));
    private final String id;

    Batching(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Batching byId(String id) {
        if (id == null) return null;
        return BY_ID.get(id.toLowerCase(Locale.ROOT));
    }
}
