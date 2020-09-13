package org.littlewings.clover.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.mutiny.Uni;
import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.repository.DiaryRepository;

@ApplicationScoped
public class DiaryService {
    @Inject
    DiaryRepository diaryRepository;

    public Uni<Integer> count() {
        return diaryRepository.findAll().onItem().transform(diaries -> diaries.size());
    }

    public Uni<List<DiaryEntry>> findAll() {
        return diaryRepository.findAll();
    }

    public Uni<List<DiaryEntry>> search(List<String> words) {
        return diaryRepository
                .findAll()
                .onItem()
                .transform(diaries ->
                        diaries
                                .stream()
                                .filter(d -> d.match(words))
                                .collect(Collectors.toList())
                );
    }
}
