package org.littlewings.clover.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.enterprise.context.ApplicationScoped;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.littlewings.clover.entity.DiaryEntry;

@ApplicationScoped
public class DiaryRepository {
    AtomicReference<List<DiaryEntry>> diaries = new AtomicReference<>(new ArrayList<>());

    public Uni<Void> clear() {
        return Uni
                .createFrom()
                .item(() -> {
                    diaries.set(new ArrayList<>());
                    return null;
                });
    }

    public Uni<Void> append(List<DiaryEntry> diaries) {
        return Uni
                .createFrom()
                .item(() -> {
                    this.diaries.get().addAll(diaries);
                    return null;
                });
    }

    public Uni<Integer> count() {
        return Uni.createFrom().item(diaries.get().size());
    }

    public Uni<List<DiaryEntry>> findAll() {
        return Uni.createFrom().item(diaries.get());
    }
}
