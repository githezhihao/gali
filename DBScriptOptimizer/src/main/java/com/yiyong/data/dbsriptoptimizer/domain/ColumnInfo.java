package com.yiyong.data.dbsriptoptimizer.domain;

public class ColumnInfo {
    private String columnName;
    private String columnType;
    private String isNullable;
    private String defaultValue;
    private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        if(defaultValue==null || defaultValue.equals("NULL")){
            this.defaultValue="";
        }else{
            this.defaultValue = defaultValue;
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setDataType(String columnType) {
        this.columnType = columnType;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        if(isNullable==null || isNullable.equals("NULL")){
            this.isNullable = "";
        }else{
            this.isNullable = isNullable;
        }
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "columnName='" + columnName + '\'' +
                ", columnType='" + columnType + '\'' +
                ", isNullable='" + isNullable + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ColumnInfo columnInfo = (ColumnInfo) obj;
        return this.toString().equals(columnInfo.toString());
    }
}
