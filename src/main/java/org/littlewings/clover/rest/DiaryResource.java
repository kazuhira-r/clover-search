package org.littlewings.clover.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.smallrye.mutiny.Uni;
import org.littlewings.clover.entity.DiaryEntry;
import org.littlewings.clover.service.DiaryCrawlService;
import org.littlewings.clover.service.DiaryService;

@ApplicationScoped
@Path("diary")
public class DiaryResource {
    @Inject
    DiaryService diaryService;

    @GET
    @Path("count")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Map<String, Object>> count() {
        Map<String, Object> response = new HashMap<>();
        response.put("count", diaryService.count().subscribe().asCompletionStage().join());

        return Uni.createFrom().item(response);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<DiaryEntry>> get(@QueryParam("limit") Integer limit) {
        return diaryService
                .findAll()
                .onItem()
                .transform(allDiaries -> {
                    int allDiariesCount = allDiaries.size();

                    int actualLimit;

                    if (limit != null) {
                        actualLimit = limit;
                    } else {
                        actualLimit = allDiariesCount;
                    }

                    return allDiaries.subList(0, (actualLimit > allDiariesCount ? allDiariesCount : actualLimit));
                });
    }

    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<DiaryEntry>> search(@QueryParam("query") String query) {
        if (query != null && !query.isEmpty()) {
            return diaryService.search(Arrays.asList(query.split("( |　)+")));
        } else {
            return Uni.createFrom().item(Collections.emptyList());
        }
    }

    @GET
    @Path("refresh")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> refresh() {
        DiaryCrawlService diaryCrawlService = CDI.current().select(DiaryCrawlService.class).get();
        diaryCrawlService.refresh();

        return Uni.createFrom().item("OK");
    }
}
