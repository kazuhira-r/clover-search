package org.littlewings.clover.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.service.ConcurrentService;
import org.littlewings.clover.service.DiaryCrawlService;
import org.littlewings.clover.service.DiaryService;

@ApplicationScoped
@Path("/diaries")
public class DiariesResource {
    @Inject
    private DiaryService diaryService;

    @Inject
    private ConcurrentService concurrentService;

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> count() {
        int diaryCount = diaryService.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", diaryCount);
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DiaryEntry> get(@QueryParam("limit") Integer limit) {
        List<DiaryEntry> allDiaries = diaryService.findAll();

        int allDiariesCount = allDiaries.size();

        int actualLimit;

        if (limit != null) {
            actualLimit = limit;
        } else {
            actualLimit = allDiariesCount;
        }

        return allDiaries.subList(0, (Math.min(actualLimit, allDiariesCount)));
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DiaryEntry> search(@QueryParam("query") String query) {
        if (query != null && !query.isEmpty()) {
            return diaryService.search(Arrays.asList(query.split("( |　)+")));
        } else {
            return Collections.emptyList();
        }
    }

    @GET
    @Path("/refresh")
    @Produces(MediaType.TEXT_PLAIN)
    public void refresh() {
        concurrentService.submit(() -> {
            DiaryCrawlService diaryCrawlService = CDI.current().select(DiaryCrawlService.class).get();
            diaryCrawlService.refresh();
        });
    }
}
