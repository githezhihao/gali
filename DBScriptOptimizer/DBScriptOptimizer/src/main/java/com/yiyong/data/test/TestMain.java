package com.yiyong.data.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMain {
    public static void main(String[] args) {
        //`hospital_code` varchar(50) NOT NULL COMMENT '组织机构代码',
        //`yy_collection_datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据采集时间',
        //`patient_id_old` varchar(200) DEFAULT NULL COMMENT '原始患者ID',
        //`yy_record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据唯一标识',
        //`K` decimal(12,2) NOT NULL COMMENT '护理',
        //`yy_record_md5` varchar(100) NOT NULL
        String columnPattern = "`(\\w+)` ((?:\\w+)(?:\\((?:\\d+|(?:\\d+,\\d+))\\))?) (NULL|NOT NULL)?(?: )?(?:DEFAULT (\\w+) )?(?:AUTO_INCREMENT )?(?:COMMENT '(\\S+)')?";
        String line = "`yy_record_md5` varchar(100) NOT NULL";
        Pattern pattern = Pattern.compile(columnPattern);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String columnName = matcher.group(1);
            String dataType = matcher.group(2);
            String nullable = matcher.group(3);
            String defaultValue = matcher.group(4);
            String comment = matcher.group(5);
            // store the column information
            System.out.println("columnName:"+columnName+"  dataType:"+dataType+"  nullable:"+nullable + "  defaultValue:"+defaultValue + "   comment:"+comment);
        }
    }
}
