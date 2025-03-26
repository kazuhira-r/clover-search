package org.littlewings.clover.repository;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.littlewings.clover.entity.DiaryEntry;

@ApplicationScoped
public class DiaryRepository {
    private AtomicReference<List<DiaryEntry>> diaries = new AtomicReference<>(new ArrayList<>());

    public void clear() {
        diaries.set(new ArrayList<>());
    }

    public void append(List<DiaryEntry> diaries) {
        this.diaries.get().addAll(diaries);
    }

    public int count() {
        return diaries.get().size();
    }

    public List<DiaryEntry> findAll() {
        return diaries.get();
    }
}
