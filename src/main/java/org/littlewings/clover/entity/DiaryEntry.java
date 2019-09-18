package org.littlewings.clover.entity;

import java.util.ArrayList;
import java.util.List;

public class DiaryEntry implements Searchable {
    String date;
    String url;
    String title;
    List<String> categories;

    public static DiaryEntry create(String date, String url, String title, List<String> categories) {
        DiaryEntry entry = new DiaryEntry();

        entry.date = date;
        entry.url = url;
        entry.title = title;
        entry.categories = new ArrayList<>(categories);

        return entry;
    }

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
                            .filter(c -> c.contains(wordIgnoreCase))
                            .count() > 0;
                })
                .count() == words.size();
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getCategories() {
        return categories;
    }
}
