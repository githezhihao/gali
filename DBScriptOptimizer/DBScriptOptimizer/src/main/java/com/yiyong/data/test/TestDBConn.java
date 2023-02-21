package com.yiyong.data.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class TestDBConn {
    public static void main(String[] args) {
        // database connection information
        String url = "jdbc:mysql://localhost:3306/wotu_ods";
        String user = "root";
        String password = "abcd1234";

        // load the MySQL driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to load driver.");
            e.printStackTrace();
            return;
        }

        // connect to the database
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // create a statement
            Statement statement = connection.createStatement();

            // query the information_schema.columns table
            ResultSet columnsResult = statement.executeQuery("SELECT table_name, column_name, data_type, character_maximum_length, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = 'wotu_ods'");

            // store the column information in a map
            Map<String, Map<String, String>> columnsMap = new HashMap<>();
            while (columnsResult.next()) {
                String tableName = columnsResult.getString("table_name");
                String columnName = columnsResult.getString("column_name");
                String dataType = columnsResult.getString("data_type");
                String characterMaxLength = columnsResult.getString("character_maximum_length");
                String isNullable = columnsResult.getString("is_nullable");

                Map<String, String> columnInfo = new HashMap<>();
                columnInfo.put("data_type", dataType);
                columnInfo.put("character_maximum_length", characterMaxLength);
                columnInfo.put("is_nullable", isNullable);

                columnsMap.put(tableName + "." + columnName, columnInfo);
            }
            System.out.println(columnsMap.size());

            // read the DDL script and compare with the column information
            // ...

        } catch (Exception e) {
            System.out.println("Error connecting to the database.");
            e.printStackTrace();
        }
    }
}
