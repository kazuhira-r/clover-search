package org.littlewings.clover.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.repository.DiaryRepository;

@ApplicationScoped
public class DiaryService {
    @Inject
    private DiaryRepository diaryRepository;

    public int count() {
        return diaryRepository.findAll().size();
    }

    public List<DiaryEntry> findAll() {
        return diaryRepository.findAll();
    }

    public List<DiaryEntry> search(List<String> words) {
        return diaryRepository
                .findAll()
                .stream()
                .filter(d -> d.match(words))
                .toList();
    }
}
