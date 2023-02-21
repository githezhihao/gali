package com.yiyong.data.optimizer;

import com.yiyong.data.dbsriptoptimizer.domain.ColumnInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GeneratorUpradeSript {

//    String tableSchemas = "wotu_ods,wotu_etl_temp,wotu_etl,wotu_cdm";
    static String filePath = "/Users/hezhihao/Desktop/3.6大禹治水相关";
    static String tableSchemas = "wotu_ods";

    public static void main(String[] args) {
        //获取当前系统的table 元数据信息
        CurrentDBInfo currentDBInfo = new CurrentDBInfo();
        Map<String, Map<String, List<ColumnInfo>>> curTableMetaInfo = currentDBInfo.getCurrentColumnInfo(tableSchemas);
        //获取要升级的最新元数据信息
        ParserLastVersion parserLastVersion = new ParserLastVersion();
        Map<String,Object> retMap = parserLastVersion.parser(tableSchemas);
        //每个表建表信息
        Map<String,Map<String,String>> createTableMap = (Map<String,Map<String,String>>)retMap.get("create");
        //列信息
        Map<String, Map<String, List<ColumnInfo>>> schemaMataMap = (Map<String, Map<String, List<ColumnInfo>>>)retMap.get("meta");
        //列元数据信息
        Map<String,String> columnDDLMap = (Map<String,String>)retMap.get("columnDDL");

        new GeneratorUpradeSript().CompareAndGeneratorUpradeSript(curTableMetaInfo,schemaMataMap,createTableMap,columnDDLMap);
    }

    /**
     * 比较元数据信息并生成自动升级脚本
     * 1.以最新的版本为准，找到要创建新的表
     * 2.以最新的版本为准，对比相同的数据库实例中，相同的表 要增加新的列
     * 3.以最新的版本为准，对比相同的数据库实例中，相同的表，要更新列的信息
     * @param curTableMetaInfo 当前系统版本表元数据信息
     * @param latestVersionMetaInfo 最新版本表元数据信息
     * @param createTableMap 表创建脚本
     */
    public void CompareAndGeneratorUpradeSript(Map<String, Map<String, List<ColumnInfo>>> curTableMetaInfo,
                                               Map<String, Map<String, List<ColumnInfo>>> latestVersionMetaInfo,
                                               Map<String,Map<String,String>> createTableMap,
                                               Map<String,String> columnDDLMap) {
        File file = new File(filePath+"filename.txt");
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            for (String tableSchema:tableSchemas.split(",")){
//                writer.write(String.format("USE s%;\n", tableSchema));
                System.out.println(String.format("对比的数据库:{%s}",tableSchema));
                Map<String, List<ColumnInfo>> latestTableMeta = latestVersionMetaInfo.get(tableSchema);
                Map<String, List<ColumnInfo>> curTableMeto = curTableMetaInfo.get(tableSchema);
                //最新版本表数量 > 当前系统中表梳理，需要创建新的表

                List<String> addTableList = latestTableMeta.keySet().stream().filter(e ->!curTableMeto.keySet().contains(e)).collect(Collectors.toList());
                if(addTableList.size()>0){
                    System.out.println(addTableList.size());
                    //生成建表语句 creat table *** () ;s
                    for(String createTbName : addTableList){
//                        System.out.println(createTableMap.get(tableSchema).get(createTbName));
                        writer.write(createTableMap.get(tableSchema).get(createTbName));
                    }
                }

                //以最新的版本为准，对比相同的数据库实例中，相同的表 要增加新的列
                for(Map.Entry<String,List<ColumnInfo>> entry : latestTableMeta.entrySet()){
                    String tableName = entry.getKey();
                    System.out.println("tableName:"+tableName);
                    List<ColumnInfo> lastColumnList = entry.getValue();
                    //最新版本相同的表列数 > 当前系统版本表中的列数，需要生成新增列脚本 alter table ** add column **** *** comment ''
                    List<ColumnInfo> curColumnList = curTableMeto.get(tableName);
                    //当前系统版本存在此表
                    if(curColumnList!=null){
                        Map<String,ColumnInfo> columnInfoMap = curColumnList.stream().collect(Collectors.toMap(ColumnInfo::getColumnName,v->v));
                        //找出最新版本存在字段且当前系统版本不存在的列
                        lastColumnList.forEach(e->{
                            if(!columnInfoMap.keySet().contains(e.getColumnName())){//当前版本缺少列
                                System.out.println("缺少列："+e.getColumnName());
//                                System.out.println(String.format("ALTER TABLE %s ADD COLUMN %s",tableSchema+"."+tableName,columnDDLMap.get(tableSchema+"|"+tableName+"|"+e.getColumnName())));
//                                writer.write(String.format("ALTER TABLE %s ADD COLUMN %s",tableSchema+"."+tableName,columnDDLMap.get(tableSchema+"|"+tableName+"|"+e.getColumnName())));
                                try {
                                    writer.write(String.format("ALTER TABLE %s ADD COLUMN %s",tableSchema+"."+tableName,columnDDLMap.get(tableSchema+"|"+tableName+"|"+e.getColumnName()))+"\n");
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }else{//列存在，对比列的元数据信息
                                if(!e.equals(columnInfoMap.get(e.getColumnName()))){
                                    System.out.println("列元信息不一致："+e.getColumnName());
//                                    System.out.println("## cur信息："+columnInfoMap.get(e.getColumnName()).toString());
//                                    System.out.println("## last信息："+e.toString());
//                                    System.out.println(String.format("ALTER TABLE %s MODIFY COLUMN %s",tableSchema+"."+tableName,columnDDLMap.get(tableSchema+"|"+tableName+"|"+e.getColumnName())));
                                    try {
                                        writer.write(String.format("ALTER TABLE %s MODIFY COLUMN %s",tableSchema+"."+tableName,columnDDLMap.get(tableSchema+"|"+tableName+"|"+e.getColumnName()))+"\n");
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
