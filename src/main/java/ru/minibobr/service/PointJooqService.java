package ru.minibobr.service;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PointJooqService {
    private final DSLContext dsl;

    public PointJooqService(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Map<String, Long> getUserStats(Long userId) {
        // SELECT hit, count(*) FROM points WHERE user_id = ? GROUP BY hit
        return dsl.select(
                        org.jooq.impl.DSL.field("hit", Boolean.class),
                        org.jooq.impl.DSL.count()
                )
                .from(org.jooq.impl.DSL.table("points"))
                .where(org.jooq.impl.DSL.field("user_id").eq(userId))
                .groupBy(org.jooq.impl.DSL.field("hit"))
                .fetch()
                .intoMap(
                        record -> record.value1() ? "hits" : "misses",
                        record -> record.value2().longValue()
                );
    }
}