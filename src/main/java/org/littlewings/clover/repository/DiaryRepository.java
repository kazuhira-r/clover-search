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

    public void refresh(List<DiaryEntry> diaries) {
        this.diaries.set(new ArrayList<>(diaries));
    }

    public Uni<List<DiaryEntry>> findAll() {
        return Uni.createFrom().item(diaries.get());
    }
}
