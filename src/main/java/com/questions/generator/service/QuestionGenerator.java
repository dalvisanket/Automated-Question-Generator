package com.questions.generator.service;

import com.questions.generator.domain.AggregateFunction;
import com.questions.generator.domain.ColumnType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class QuestionGenerator {

    public static Map<AggregateFunction, String> selectClauseGenerator(Map<ColumnType, Set<String>> colMap){

        Set<String> columns = colMap.get(ColumnType.NUMERICAL);

        int random = ThreadLocalRandom.current().nextInt(0  , columns.size());

        String selectColumn = (String) columns.toArray()[random];

        AggregateFunction aggregateFunction = AggregateFunction.values()[random%AggregateFunction.values().length];

        return new HashMap<AggregateFunction, String>(){{put(aggregateFunction,selectColumn);}};
    }

    public static Map<String, String> whereClauseGenerator(Map<ColumnType, Set<String>> colMap, Map<String,Set<String>> colDistinctVal){

        Map<String, String> whereClauseStatement = new HashMap<>();
        int whereClauseCount = ThreadLocalRandom.current().nextInt(0, 3);
        Set<String> columns = new HashSet<>(colMap.get(ColumnType.CATEGORICAL));

        for(int i  = 0; i < whereClauseCount; i++) {

            int random = ThreadLocalRandom.current().nextInt(0, columns.size());
            String whereColumn = (String) columns.toArray()[random];
            columns.remove(whereColumn);

            Set<String> distinctVal = new HashSet<>(colDistinctVal.get(whereColumn));
            int randomDistinctVal = ThreadLocalRandom.current().nextInt(0, distinctVal.size());
            String whereColValue = (String) distinctVal.toArray()[randomDistinctVal];
            distinctVal.remove(whereColValue);

            whereClauseStatement.put(whereColumn,whereColValue);
        }


        return whereClauseStatement;
    }
}
