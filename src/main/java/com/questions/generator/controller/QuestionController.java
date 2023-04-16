package com.questions.generator.controller;

import com.questions.generator.domain.AggregateFunction;
import com.questions.generator.domain.ColumnDataType;

import com.questions.generator.domain.ColumnType;
import com.questions.generator.service.QuestionGenerator;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.PrintWriter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Controller
public class QuestionController {

    @Autowired
    SparkSession sparkSession;

    @Autowired
    Dataset<Row> dataset;

    Map<ColumnType, Set<String>> colMap;

    Map<String, Set<String>> colDistinctVal;

    public QuestionController(){
        this.colMap = new HashMap<>();
        colMap.put(ColumnType.NUMERICAL, new HashSet<String>());
        colMap.put(ColumnType.CATEGORICAL, new HashSet<String>());

        colDistinctVal = new HashMap<>();
    }


    @GetMapping("/")
    public ModelAndView welcome() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index.html");
        return modelAndView;
    }

    @GetMapping("/get-questions/{count}")
    public ResponseEntity<InputStreamResource> getQuestions( @PathVariable(name = "count") int count) throws AnalysisException, FileNotFoundException {

        dataset.printSchema();

        Row row = dataset.first();

        for(int i = 0; i < dataset.columns().length; i++){
            String rowStr = row.get(i).toString();
            ColumnDataType datatype = ColumnDataType.getDataType(rowStr).get();

            dataset = dataset.withColumn(dataset.columns()[i], dataset.col(dataset.columns()[i]).cast(datatype.getSparkDataType()));
            if(datatype.getSparkDataType().equals(DataTypes.IntegerType) || datatype.getSparkDataType().equals(DataTypes.FloatType)){
                Set<String> columns = colMap.get(ColumnType.NUMERICAL);
                columns.add(dataset.columns()[i]);
                colMap.replace(ColumnType.NUMERICAL,columns );
            }
            else {
                Set<String> columns = colMap.get(ColumnType.CATEGORICAL);
                columns.add(dataset.columns()[i]);
                colMap.replace(ColumnType.CATEGORICAL,columns);

                Dataset<Row> distinctVal = dataset.select(dataset.columns()[i]).distinct();
                distinctVal.show();

                Set<String> distinctValSet = new HashSet<>();
                for(Iterator<Row> iter = distinctVal.toLocalIterator(); iter.hasNext();){
                    String element = (iter.next()).getString(0);
                    distinctValSet.add(element);
                }

                colDistinctVal.put(dataset.columns()[i], distinctValSet);

            }

        }

        dataset.printSchema();


        List<String> questions = new ArrayList<>();


        for(int i = 0; i< count; i++){
            Map<AggregateFunction, String> selectClause = QuestionGenerator.selectClauseGenerator(colMap);

            Map<String, String> whereClause = QuestionGenerator.whereClauseGenerator(colMap, colDistinctVal);

            if (whereClause.size() == 0) {
                for (Map.Entry<AggregateFunction, String> select : selectClause.entrySet()) {
                    questions.add("What is the " + select.getKey() + " " + select.getValue() + " from the whole dataset");
                }
            } else {
                for (Map.Entry<AggregateFunction, String> select : selectClause.entrySet()) {
                    StringBuilder whereBuilder = new StringBuilder("What is the " + select.getKey() + " " + select.getValue() + " where ");
                    for (Map.Entry<String, String> where : whereClause.entrySet())
                        whereBuilder.append(where.getKey() + " = \"" + where.getValue() + "\" and ");

                    whereBuilder.delete(whereBuilder.length()-4,whereBuilder.length());
                    questions.add(whereBuilder.toString());
                }
            }
        }

        File csvOutputFile = new File("questions.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            questions.stream()
                    .forEach(pw::println);
        }

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=questions.csv");

        InputStreamResource resource = new InputStreamResource(new FileInputStream(csvOutputFile));


        return ResponseEntity.ok()
                .headers(header)
                .contentLength(csvOutputFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);


    }

}
