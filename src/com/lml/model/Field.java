package com.lml.model;

import java.util.ArrayList;

public class Field
{
    private String fieldName;
    private String typeName;
    private boolean isPrimaryKey;

    public static final String VARCHAR = "varchar";
    public static final String INT = "int";
    public static final String DOUBLE = "double";

    public Field(String fieldName, String typeName)
    {
        this.fieldName = fieldName;
        this.typeName = typeName;
        this.isPrimaryKey = false;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    public boolean isPrimaryKey()
    {
        return isPrimaryKey;
    }

    public boolean typeOk(String str)
    {
        int len = str.length();
        if(typeName.equals("int"))
        {
            for(int i = 0; i < len; i++)
            {
                char tmp = str.charAt(i);
                if(tmp < '0' || tmp > '9')
                    return false;
            }
        }
        else if(typeName.equals("double"))
        {
            for(int i = 0; i < len; i++)
            {
                char tmp = str.charAt(i);
                if((tmp < '0' || tmp > '9') && tmp != '.')
                    return false;
            }
        }
        return true;
    }

    public static boolean havFieldName(String name, ArrayList<Field> fields)
    {
        for(Field field:fields)
        {
            if(field.getFieldName().equals(name))
                return true;
        }
        return false;
    }

    public void setPrimaryKey(boolean primaryKey)
    {
        isPrimaryKey = primaryKey;
    }
}
