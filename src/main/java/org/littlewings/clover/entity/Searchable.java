package org.littlewings.clover.entity;

import java.util.Arrays;
import java.util.List;

@FunctionalInterface
public interface Searchable {
    default boolean match(String... words) {
        return match(Arrays.asList(words));
    }

    boolean match(List<String> words);
}
