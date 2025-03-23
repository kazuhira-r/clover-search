package org.littlewings.clover.entity;

import java.util.List;

public record DiaryEntry(String date, String url, String title, List<String> categories) implements Searchable {
    @Override
    public boolean match(List<String> words) {
        return words
                .stream()
                .filter(w -> {
                    String wordIgnoreCase = w.toLowerCase();

                    if (title.toLowerCase().contains(wordIgnoreCase)) {
                        return true;
                    }

                    return categories
                            .stream()
                            .map(String::toLowerCase)
                            .anyMatch(c -> c.contains(wordIgnoreCase));
                })
                .count() == words.size();
    }
}
