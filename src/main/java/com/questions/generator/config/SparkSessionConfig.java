package com.questions.generator.config;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;


import java.io.IOException;

@Configuration
public class SparkSessionConfig {

    @Autowired
    ResourceLoader resourceLoader;

    @Bean
    public SparkSession sparkSession(){
        return SparkSession.builder().master("local").appName("Data101").getOrCreate();
    }

    @Bean
    public Dataset<Row> dataset() throws IOException {

        return sparkSession().read().format("com.databricks.spark.csv")
                .option("header",true)
                .option("inferschema",true)
                .load("src/main/resources/airbnb.csv");
    }

}
