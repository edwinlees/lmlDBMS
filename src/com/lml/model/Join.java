package com.lml.model;

import java.util.*;

public class Join
{
    private String tableName1;
    private String tableName2;
    private String field1;
    private String field2;

    public Join(String tableName1, String tableName2, String field1, String field2)
    {
        this.tableName1 = tableName1;
        this.tableName2 = tableName2;
        this.field1 = field1;
        this.field2 = field2;
    }

    public static List<Map<String, String>> tableConcat(List<Join> joins, Map<String, List<Map<String, String>>> allData)
    {
        String firstTableName = joins.get(0).getTableName1();
        List<Map<String, String>> first = allData.get(firstTableName);
        List<Map<String, String>> ans = new ArrayList<>();
        for(Map<String, String> row:first)
        {
            Map<String, String> map = new HashMap<>();
            for(String key:row.keySet())
            {
                map.put(firstTableName + "." + key, row.get(key));
            }
            ans.add(map);
        }
        Set<String>tableNames = new HashSet<>();
        tableNames.add(firstTableName);

        while(!joins.isEmpty())
        {
            Set<Join>delJoin = new HashSet<>();
            for(Join join:joins)
            {
                String tableLef = null; //以操作的表名
                String tableRig = null; //待操作的表名
                String fieldLef = null;
                String fieldRig = null;
                boolean flag = false;
                if(tableNames.contains(join.getTableName1()))
                {
                    flag = true;
                    tableLef = join.getTableName1();
                    tableRig = join.getTableName2();
                    fieldLef = join.getField1();
                    fieldRig = join.getField2();
                    tableNames.add(tableRig);
                    delJoin.add(join);
                }
                else if(tableNames.contains(join.getTableName2()))
                {
                    flag = true;
                    tableLef = join.getTableName2();
                    tableRig = join.getTableName1();
                    fieldLef = join.getField2();
                    fieldRig = join.getField1();
                    tableNames.add(tableRig);
                    delJoin.add(join);
                }
                if(flag)
                {
                    //具体的连接操作
                    Set<Map<String, String>>delMap = new HashSet<>();
                    Set<Map<String, String>>addMaps = new HashSet<>();
                    for(Map<String, String>map:ans)
                    {
                        String value = null; //左表属性的值
                        for(String key:map.keySet())
                        {
                            String[] strings = key.split("\\.");
                            if(strings[0].equals(tableLef) && strings[1].equals(fieldLef))
                            {
                                value = map.get(key);
                                break;
                            }
                        }
                        List<Map<String, String>> judData = allData.get(tableRig);
                        for(Map<String, String> judMap:judData)
                        {
                            String judValue = judMap.get(fieldRig); //右表属性的值
                            if(judValue.equals(value))
                            {
                                Map<String, String> addMap = new HashMap<>(map);  //创建新的行
                                for(String key:judMap.keySet())
                                {
                                    addMap.put(tableRig + "." + key, judMap.get(key));
                                }
                                addMaps.add(addMap);
                            }
                        }
                        delMap.add(map);      //最后要删除这一行
                    }
                    ans.removeAll(delMap); //删除原先的数据
                    ans.addAll(addMaps);  //增加新的数据
                }
            }
            for(Join join:delJoin)
                joins.remove(join);
        }
        return ans;
    }

    public String getTableName1()
    {
        return tableName1;
    }

    public void setTableName1(String tableName1)
    {
        this.tableName1 = tableName1;
    }

    public String getTableName2()
    {
        return tableName2;
    }

    public void setTableName2(String tableName2)
    {
        this.tableName2 = tableName2;
    }

    public String getField1()
    {
        return field1;
    }

    public void setField1(String field1)
    {
        this.field1 = field1;
    }

    public String getField2()
    {
        return field2;
    }

    public void setField2(String field2)
    {
        this.field2 = field2;
    }
}
