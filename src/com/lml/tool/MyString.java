package com.lml.tool;

import com.lml.model.Field;
import com.lml.model.Join;
import com.lml.model.WhereFilter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyString
{
    public static final Pattern PATTERN_WHERE = Pattern.compile("(\\w+(?:\\.\\w+)?)\\s*([<=>])\\s*([^\\.\\s]+)[\\s;]");
    public static final Pattern PATTERN_CONCAT = Pattern.compile("(\\w+\\.\\w+)\\s*=\\s*(\\w+\\.\\w+)");

    public static ArrayList<Field> parseCreateTable(String str)
    {
        String[] fields = str.split(",");
        ArrayList<Field> ans = new ArrayList<>();
        for(String field:fields)
        {
            String[] str2 = field.trim().split("\\s+");
            ans.add(new Field(str2[0], str2[1]));
        }
        return ans;
    }

    public static Map<String, String> parseInsert(String fieldStr, String valueStr)
    {
        String[] fields = fieldStr.trim().split(",");
        String[] values = valueStr.trim().split(",");
        int len = fields.length;
        if(fields.length != values.length)
            return null;
        Map<String, String> ans = new HashMap<>();
        for(int i = 0; i < len; i++)
        {
            ans.put(fields[i].trim(), values[i].trim());
        }
        return ans;
    }

    public static boolean judgeType(ArrayList<Field> fields, Map<String, String> dataStr)
    {
        for(Field field:fields)
        {
            if(!field.typeOk(dataStr.get(field.getFieldName())))
                return false;
        }
        return true;
    }

    public static List<String> parseFrom(String str)
    {
        List<String> ans = new ArrayList<>();
        String[] strings = str.trim().split(",");
        for(String tmp:strings)
        {
            ans.add(tmp.trim());
        }
        return ans;
    }

    public static ArrayList<WhereFilter> parseWhere(String str, ArrayList<Field> fields, String tableName)
    {
        if(str == null)return null;
        ArrayList<WhereFilter> ans = new ArrayList<>();
        Matcher matcher = PATTERN_WHERE.matcher(str);
        while(matcher.find())
        {
            String fieldName = matcher.group(1);
            String relation = matcher.group(2);
            String conditionValue = matcher.group(3);

            if(fieldName.contains("."))
            {
                String[] strings = fieldName.split("\\.");
                if(!tableName.equals(strings[0]))
                    continue;
                fieldName = strings[1];
            }
            for(Field field:fields)
            {
                if(field.getFieldName().equals(fieldName))
                {
                    ans.add(new WhereFilter(new Field(fieldName, field.getTypeName()), relation, conditionValue));
                    break;
                }
            }
        }
        if(ans.size() == 0)return null;
        return ans;
    }

    public static List<String> parseSelect(String str, String tableName, ArrayList<Field> fields)
    {
        List<String> ans = new ArrayList<>();
        //要全部的属性
        if(str.trim().equals("*"))
        {
            for(Field field:fields)
                ans.add(field.getFieldName());
            return ans;
        }

        String[] strings = str.trim().split(",");
        Set<String> fieldName = new HashSet<>();
        for(Field field:fields)
            fieldName.add(field.getFieldName());
        for(String string:strings)
        {
            string = string.trim();
            if(string.contains("."))
            {
                String[] tmp = string.split("\\.");
                if(tmp[0].equals(tableName))
                    string = tmp[1];
            }
            if(fieldName.contains(string))
                ans.add(string);
        }
        return ans;
    }

    public static List<Join> parseConcat(String str, Map<String, ArrayList<Field>> allField, Map<String, List<String>> selectUseField)
    {
        List<Join> ans = new ArrayList<>();
        Matcher matcher = PATTERN_CONCAT.matcher(str);
        while(matcher.find())
        {
            String[] tableStr1 = matcher.group(1).split("\\.");
            String[] tableStr2 = matcher.group(2).split("\\.");
            if(!allField.containsKey(tableStr1[0]) || !allField.containsKey(tableStr2[0]))
                return null;
            if(!Field.havFieldName(tableStr1[1], allField.get(tableStr1[0])))
                return null;
            if(!Field.havFieldName(tableStr2[1], allField.get(tableStr2[0])))
                return null;
            ans.add(new Join(tableStr1[0], tableStr2[0], tableStr1[1], tableStr2[1]));
            selectUseField.get(tableStr1[0]).add(tableStr1[1]);
            selectUseField.get(tableStr2[0]).add(tableStr2[1]);
        }
        return ans;
    }
}
