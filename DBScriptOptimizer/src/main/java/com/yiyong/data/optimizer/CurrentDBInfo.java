package com.yiyong.data.optimizer;

import com.yiyong.data.dbscriptoptimizer.util.MysqlUtils;
import com.yiyong.data.dbsriptoptimizer.domain.ColumnInfo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrentDBInfo {
    public Map<String,Map<String,List<ColumnInfo>>> getCurrentColumnInfo(String tableSchemas){
        Connection conn = MysqlUtils.getConnection();
        Map<String, Map<String,List<ColumnInfo>>> schemaMap = new HashMap<>();
        for(String tableSchema:tableSchemas.split(",")){
            String sql = String.format("SELECT table_name, " +
                    "column_name, " +
                    "case when column_type = 'int' then 'int(11)' when column_type = 'bigint' then 'bigint(20)' else column_type end column_type, " +
                    "case when is_nullable='NO' then 'NOT NULL' else null end is_nullable, " +
                    "column_default FROM information_schema.columns  " +
                    "WHERE table_schema = '%s'",tableSchema);

//        String.format(sql, tableSchema);
            //Map<schema,Map<table,List<columnInfo>>>
            try(Statement statement = conn.createStatement()){
                ResultSet columnsResult = statement.executeQuery(sql);
                while (columnsResult.next()) {
                    ColumnInfo columnInfo = new ColumnInfo();
                    String tableName = columnsResult.getString("table_name");
                    String columnName = columnsResult.getString("column_name");
                    String columnType = columnsResult.getString("column_type");
                    String isNullable = columnsResult.getString("is_nullable");
                    String defaultValue = columnsResult.getString("column_default");
                    columnInfo.setColumnName(columnName);
                    columnInfo.setDataType(columnType);
                    columnInfo.setIsNullable(isNullable);
                    columnInfo.setDefaultValue(defaultValue);

                    if(schemaMap.containsKey(tableSchema)){
                        Map<String,List<ColumnInfo>> tmpSchemaMap = schemaMap.get(tableSchema);
                        if(tmpSchemaMap.containsKey(tableName)){//是否包含此表
                            List<ColumnInfo> tmpColumns = tmpSchemaMap.get(tableName);
                            tmpColumns.add(columnInfo);
                        }else{//不包含此表
                            List<ColumnInfo> tmpColumns = new ArrayList<>();
                            tmpColumns.add(columnInfo);
                            tmpSchemaMap.put(tableName,tmpColumns);
                        }
                    }else{
                        List<ColumnInfo> columnInfos = new ArrayList<>();
                        columnInfos.add(columnInfo);
                        Map<String,List<ColumnInfo>> tmpTableMap = new HashMap<>();
                        tmpTableMap.put(tableName,columnInfos);
                        schemaMap.put(tableSchema,tmpTableMap);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return schemaMap;
    }

    public static void main(String[] args) {
        new CurrentDBInfo().getCurrentColumnInfo("wotu_ods,wotu_etl_temp,wotu_etl,wotu_cdm");
    }
}
