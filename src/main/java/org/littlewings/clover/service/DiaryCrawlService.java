package org.littlewings.clover.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.littlewings.clover.config.CrawlConfig;
import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.repository.DiaryRepository;

@ApplicationScoped
public class DiaryCrawlService {
    private Logger logger = Logger.getLogger(DiaryCrawlService.class);

    @Inject
    private CrawlConfig crawlConfig;

    private TimeUnit sleepTimeUnit = TimeUnit.SECONDS;

    @Inject
    private DiaryRepository diaryRepository;

    public void refresh() {
        logger.infof("start scheduled crawling job...");

        diaryRepository.clear();

        crawl();

        logger.infof("end initialize crawling, entries count = %d", diaryRepository.count());
    }

    private void crawl() {
        logger.infof("crawl start base url = %s", crawlConfig.getArchiveBaseUrl());

        long sleepSeconds = crawlConfig.getCrawlSleepSeconds();
        int currentExecutionCount = 1;

        try {
            Element baseElement = Jsoup.connect(crawlConfig.getArchiveBaseUrl()).get();

            while (true) {
                Elements entrySections = baseElement.select("section.archive-entry");
                List<DiaryEntry> collectedDiaryEntries =
                        entrySections.stream().map(section -> {
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

                            return new DiaryEntry(date, url, title, categories);
                        }).toList();

                diaryRepository.append(collectedDiaryEntries);

                logger.infof("collected diary entries = %d", collectedDiaryEntries.size());

                Elements pagerNext = baseElement.select(".pager-next > a");

                if (!pagerNext.isEmpty()) {
                    String pagerNextUrl = pagerNext.select("a").attr("href");

                    while (true) {
                        try {
                            logger.infof("sleeping %s sec...", sleepSeconds);

                            sleepTimeUnit.sleep(sleepSeconds);

                            logger.infof("next crawl url = %s", pagerNextUrl);
                            baseElement = Jsoup.connect(pagerNextUrl).get();
                            break;
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            if (currentExecutionCount < crawlConfig.getCrawRetryLimit()) {
                                logger.infof(e, "current execution count = %d, refresh failed reason = %s, retry next loop", currentExecutionCount, e.getMessage());

                                sleepSeconds += crawlConfig.getCrawRetryBackoffSeconds();
                                currentExecutionCount++;
                                break;
                            }

                            throw e;
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
