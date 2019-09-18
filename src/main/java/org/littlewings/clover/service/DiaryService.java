package org.littlewings.clover.service;

import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.repository.DiaryRepository;

@ApplicationScoped
public class DiaryService {
    @Inject
    DiaryRepository diaryRepository;

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
                .collect(Collectors.toList());
    }
}
