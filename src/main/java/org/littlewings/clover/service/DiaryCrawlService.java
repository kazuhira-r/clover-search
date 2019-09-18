package org.littlewings.clover.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.littlewings.clover.config.CrawlConfig;
import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.repository.DiaryRepository;

@ApplicationScoped
public class DiaryCrawlService {
    Logger logger = Logger.getLogger(DiaryCrawlService.class);

    @Inject
    CrawlConfig crawlConfig;

    TimeUnit sleepTimeUnit = TimeUnit.SECONDS;

    @Inject
    DiaryRepository diaryRepository;

    public List<DiaryEntry> refresh() {
        List<DiaryEntry> diaryEntries = crawl();
        diaryRepository.refresh(diaryEntries);

        return diaryEntries;
    }

    public List<DiaryEntry> crawl() {
        try {
            logger.infof("crawl start base url = %s", crawlConfig.getBaseUrl());

            Document document = Jsoup.connect(crawlConfig.getBaseUrl()).get();

            List<DiaryEntry> diaryEntries = new ArrayList<>();
            crawlWhile(document, diaryEntries);

            return diaryEntries;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void crawlWhile(Element baseElement, List<DiaryEntry> diaryEntries) {
        Elements entrySections = baseElement.select("section.archive-entry");

        List<DiaryEntry> collectedDiaryEntries =
                entrySections
                .stream()
                .map(section -> {
                    String date = section.select(".archive-date time").attr("title");
                    String url = section.select(".entry-title > a").attr("href");
                    String title = section.select(".entry-title > a").text();
                    List<String> categories =
                            section
                                    .select(".categories")
                                    .stream()
                                    .map(Element::text)
                                    .flatMap(text -> Arrays.asList(text.split(" ")).stream())
                                    .collect(Collectors.toList());

                    return DiaryEntry.create(date, url, title, categories);
                })
                .collect(Collectors.toList());

        logger.infof("collected diary entries = %d", collectedDiaryEntries.size());

        diaryEntries.addAll(collectedDiaryEntries);
        diaryRepository.refresh(diaryEntries);

        Elements pagerNext = baseElement.select(".pager-next > a");
        if (!pagerNext.isEmpty()) {
            String pagerNextUrl = pagerNext.select("a").attr("href");

            try {
                logger.infof("sleeping %s sec...", crawlConfig.getCrawlSleepSeconds());

                sleepTimeUnit.sleep(crawlConfig.getCrawlSleepSeconds());

                logger.infof("next crawl url = %s", pagerNextUrl);
                crawlWhile(Jsoup.connect(pagerNextUrl).get(), diaryEntries);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
