package com.questions.generator.domain;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public enum ColumnDataType {

    INTEGER("(?<![\\.\\d])\\b[0-9]+\\b(?![\\.\\d])", DataTypes.IntegerType),
    FLOAT("[+-]?([0-9]*[.])?[0-9]+",DataTypes.FloatType),
    STRING(".*",DataTypes.StringType);


    private final DataType dataTypes;
    private final String regex;
    ColumnDataType(String regex, DataType dataType){
        this.regex = regex;
        this.dataTypes = dataType;
    }

    public String getRegex(){
        return this.regex;
    }

    public DataType getSparkDataType(){
        return this.dataTypes;
    }

    public static Optional<ColumnDataType> getDataType(String s){
        return Arrays.stream(ColumnDataType.values())
                .filter( dt ->{
                    return Pattern.compile(dt.getRegex()).matcher(s).matches();
                })
                .findFirst();
    }


}
