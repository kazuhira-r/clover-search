package org.littlewings.clover.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
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

    ExecutorService crawExecutorService = Executors.newFixedThreadPool(2);

    @Inject
    CrawlConfig crawlConfig;

    TimeUnit sleepTimeUnit = TimeUnit.SECONDS;

    @Inject
    DiaryRepository diaryRepository;

    public void refresh() {
        logger.infof("start scheduled crawling job...");

        diaryRepository
                .clear()
                .runSubscriptionOn(crawExecutorService)
                .subscribe()
                .with(v -> {
                });

        Multi<List<DiaryEntry>> multi = Multi
                .createFrom()
                .emitter(emitter -> crawl(emitter));

        multi
                .runSubscriptionOn(crawExecutorService)
                .onItem()
                .invoke(diaryEntries -> diaryRepository.append(diaryEntries).subscribe()
                        .with(v -> {
                        }))
                .onCompletion()
                .invoke(() -> logger.infof("end initialize crawling, entries count = %d", diaryRepository.count().await().indefinitely()))
                .subscribe()
                .with(v -> {
                });
    }

    public void crawl(MultiEmitter<? super List<DiaryEntry>> emitter) {
        try {
            logger.infof("crawl start base url = %s", crawlConfig.getBaseUrl());

            Document document = Jsoup.connect(crawlConfig.getBaseUrl()).get();

            crawlWhile(document, emitter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void crawlWhile(Element baseElement, MultiEmitter<? super List<DiaryEntry>> emitter) {
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

        emitter.emit(collectedDiaryEntries);

        Elements pagerNext = baseElement.select(".pager-next > a");
        if (!pagerNext.isEmpty()) {
            String pagerNextUrl = pagerNext.select("a").attr("href");

            try {
                logger.infof("sleeping %s sec...", crawlConfig.getCrawlSleepSeconds());

                sleepTimeUnit.sleep(crawlConfig.getCrawlSleepSeconds());

                logger.infof("next crawl url = %s", pagerNextUrl);
                crawlWhile(Jsoup.connect(pagerNextUrl).get(), emitter);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        emitter.complete();
    }
}
