package com.yiyong.data.optimizer;

import com.yiyong.data.dbsriptoptimizer.domain.ColumnInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserLastVersion {

    /**
     * 读取最新的ddl脚本，解析生成table 元数据信息和建表信息
     * 建表信息
     * @param tableSchemas 检查的数据库实例名称字符串
     * @return
     * 返回信息，1.建表信息 Map<String,String> 其中key为 "create" value为建表sql字符串
     *     2.数据库实例表元数据信息 Map<String,Map<String,Map<String,List<ColumnInfo>>>> 其中key为"meta" value为数据库实例表元数据信息 例:<wotu_ods,Map<b01_1,List<ColumnInfo>>>
     *         3.表字段 元数据信息 Map<String,String>  wotu_ods|b13_1_other|visit_times ->   `visit_times` varchar(100) DEFAULT NULL COMMENT '就诊次数'
     */
    //Map<String,Map<String,List<ColumnInfo>>>
    public Map<String,Object> parser(String tableSchemas){
        Map<String,Object> retMap = new HashMap<>();
        Map<String, Map<String,List<ColumnInfo>>> schemaMap = new HashMap<>();
        Map<String, Map<String,String>> schemaCreatTbMap = new HashMap<>();
        Map<String,String> columnDDLMap = new HashMap<>();
        for(String tableSchema:tableSchemas.split(",")){
            String fileName = String.format("/Users/hezhihao/Desktop/3.6大禹治水相关/1.数据库初始化及升级脚本/1.1初始化脚本/V1.5.2/%s_init_V1.5.2.sql",tableSchema);
            // regular expression pattern for a table column
//            String columnPattern = "`(\\w+)` (\\w+)(\\((\\d+|(?:\\d+,\\d+))\\))? (NULL|NOT NULL)?(?: )?(?:DEFAULT (\\w+) )?(?:AUTO_INCREMENT )?(?:COMMENT '(\\S+)')?";
            String columnPattern = "`(\\w+)` ((?:\\w+)(?:\\((?:\\d+|(?:\\d+,\\d+))\\))?) (NULL|NOT NULL)?(?: )?(?:DEFAULT (\\w+) )?(?:AUTO_INCREMENT )?(?:COMMENT '(\\S+)')?";
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                String tableName = null;
                List<ColumnInfo> columnInfos = null;
                StringBuilder stringBuilder = null;
                while ((line = reader.readLine()) != null) {
                    // check if the line is a CREATE TABLE statement
                    if (line.startsWith("CREATE TABLE")) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(line+"\n");
                        columnInfos = new ArrayList<>();
                        // extract the table name
                        int start = line.indexOf("`") + 1;
                        int end = line.indexOf("`", start);
                        tableName = line.substring(start, end);

                    } else if (line.startsWith("  `")) {
                        // extract the column information
                        stringBuilder.append(line+"\n");
                        Pattern pattern = Pattern.compile(columnPattern);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String columnName = matcher.group(1);
                            String dataType = matcher.group(2);
                            String nullable = matcher.group(3);
                            String defaultValue = matcher.group(4);
                            String comment = matcher.group(5);
                            ColumnInfo tmpColumn = new ColumnInfo();
                            tmpColumn.setColumnName(columnName);
                            tmpColumn.setDataType(dataType);
                            tmpColumn.setIsNullable(nullable);
                            tmpColumn.setDefaultValue(defaultValue);
                            tmpColumn.setComment(comment);
                            // store the column information
                            columnInfos.add(tmpColumn);
                            columnDDLMap.put(tableSchema+"|"+tableName+"|"+columnName,line.replace(",",";"));
                        }
                    } else if (line.startsWith(")")) {
                        // print the table and column information
//                        System.out.println("Table: " + tableName);
                        stringBuilder.append(line+"\n");
                        //创建表信息组装
                        if(schemaCreatTbMap.containsKey(tableSchema)){
                            Map<String,String> tableCreateMap = schemaCreatTbMap.get(tableSchema);
                            tableCreateMap.put(tableName,stringBuilder.toString());
                        }else{
                            Map<String,String> tableCreateMap = new HashMap<>();
                            tableCreateMap.put(tableName,stringBuilder.toString());
                            schemaCreatTbMap.put(tableSchema,tableCreateMap);
                        }

                        //表元数据信息组装
                        if(schemaMap.containsKey(tableSchema)){
                            Map<String,List<ColumnInfo>> tmpTableMap = schemaMap.get(tableSchema);
                            if(tmpTableMap.containsKey(tableName)){//包含此表
                                tmpTableMap.put(tableName,columnInfos);
                            }else{
                                tmpTableMap.put(tableName,columnInfos);
                            }
                        }else{
                            Map<String,List<ColumnInfo>> tableMap = new HashMap<>();
                            tableMap.put(tableName,columnInfos);
                            schemaMap.put(tableSchema,tableMap);
                        }
                    }else if (!line.startsWith("--") && !line.startsWith("/*")  && !line.startsWith("DROP") && line.trim().length()>0){
                        stringBuilder.append(line+"\n");
                    }
                }
            } catch (Exception e) {
                System.out.println("Error reading the DDL script.");
                e.printStackTrace();
            }
        }
        retMap.put("create",schemaCreatTbMap);
        retMap.put("meta",schemaMap);
        retMap.put("columnDDL",columnDDLMap);
        return retMap;
    }
    public static void main(String[] args) {
        new ParserLastVersion().parser("wotu_ods,wotu_etl_temp,wotu_etl,wotu_cdm");
    }
}
