package com.lml.model;


import java.io.*;
import java.util.*;

public class Table
{
    private static int groupMaxNum = 10; //每组的最大个数

    public static boolean havTable(File file, String tableName)
    {
        File[] files = file.listFiles();
        for(File f:files)
        {
            if(f.isDirectory() && f.getName().equals(tableName))
                return true;
        }
        return false;
    }

    public static void writeDataDict(File dataDictFile, ArrayList<Field> fields, boolean isExtend)
    {
        try(
                FileWriter out = new FileWriter(dataDictFile, isExtend);
           )
        {
            for(Field field:fields)
            {
                out.write(field.getFieldName());
                out.write(" ");
                out.write(field.getTypeName());
                out.write(" ");
                if(field.isPrimaryKey())
                    out.write("*\n");
                else
                    out.write("^\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static ArrayList<Field> getDataDict(File file, String tableName)
    {
        ArrayList<Field> ans = new ArrayList<>();
        try
        {
            Scanner in = new Scanner(new FileInputStream(new File(file, tableName + "/dataDict")));
            String str = null;
            while(in.hasNextLine())
            {
                str = in.nextLine();
                String[] strings = str.split(" ");
                ans.add(new Field(strings[0], strings[1]));
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return ans;
    }

    public static void writeIndexFile(File indexNumFile, File indexStrFile, HashMap<String, IndexTree> indexNum, HashMap<String, HashMap<String, IndexNode>> indexStr)
    {
        try(
                ObjectOutputStream out1 = new ObjectOutputStream(new FileOutputStream(indexNumFile));
                ObjectOutputStream out2 = new ObjectOutputStream(new FileOutputStream(indexStrFile));
                )
        {
            out1.writeObject(indexNum);
            out2.writeObject(indexStr);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void writeInsertData(File table, ArrayList<Field> fields, Map<String, String> dataStr)
    {
        File dataFile = new File(table, "data");
        File[] files = dataFile.listFiles();
        File lastFile = null;
        File canFile = null;
        int fileNum = 0;
        for(File file:files)
        {
            fileNum++;
            lastFile = file;
        }
        if(lastFile == null)
        {
            canFile = new File(dataFile, "1.data");
            try
            {
                canFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try(
                    Scanner in = new Scanner(new FileInputStream(lastFile));
                    )
            {

                int jud = 0;
                while(in.hasNextLine())
                {
                    in.nextLine();
                    jud++;
                }
                if(jud >= groupMaxNum)
                {
                    int tmp = fileNum+1;
                    canFile = new File(table, "data/"+ tmp + ".data");
                    canFile.createNewFile();
                }
                else
                    canFile = lastFile;
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try(
                BufferedWriter out = new BufferedWriter(new FileWriter(canFile, true));
                )
        {
            for(Field field:fields)
            {
                out.write(dataStr.get(field.getFieldName()));
                out.write(" ");
            }
            out.write("\n");
            out.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        //更新索引
        indexAdd(table, fields, dataStr, canFile.getName());
    }

    private static void indexAdd(File table, ArrayList<Field> fields, Map<String, String> dataStr, String addName)
    {
        try(
                ObjectInputStream indexNum = new ObjectInputStream(new FileInputStream(new File(table, "indexNum")));
                ObjectInputStream indexStr = new ObjectInputStream(new FileInputStream(new File(table, "indexStr")));
                )
        {
            HashMap<String, IndexTree> indexNumTrees = (HashMap<String, IndexTree>) indexNum.readObject();
            HashMap<String, HashMap<String, IndexNode>> indexStrTrees = (HashMap<String, HashMap<String, IndexNode>>) indexStr.readObject();
            for(Field field:fields)
            {
                String fieldName = field.getFieldName();
                String value = dataStr.get(fieldName);
                if(field.getTypeName().equals(Field.VARCHAR))
                {
                    if(!indexStrTrees.containsKey(fieldName))
                        indexStrTrees.put(fieldName, new HashMap<String, IndexNode>());
                    HashMap<String, IndexNode> tmp = indexStrTrees.get(fieldName);
                    if(!tmp.containsKey(value))
                        tmp.put(value, new IndexNode());
                    IndexNode indexNode = tmp.get(value); //获取到了该值所在的indexNode
                    indexNode.addIndex(addName); //添加

                    //重新写回更新
                    tmp.put(value, indexNode);
                    indexStrTrees.put(fieldName, tmp);
                }
                else
                {
                    if(!indexNumTrees.containsKey(fieldName))
                        indexNumTrees.put(fieldName, new IndexTree());
                    IndexTree indexNumTree = indexNumTrees.get(fieldName);

                    //获取indexKey
                    IndexKey indexKey = null;
                    if(field.getTypeName().equals(Field.INT))
                        indexKey = new IndexKey(Field.INT, value);
                    else
                        indexKey = new IndexKey(Field.DOUBLE, value);


                    TreeMap<IndexKey, IndexNode> indexTree = indexNumTree.getIndexTree();
                    if(!indexTree.containsKey(indexKey))
                        indexTree.put(indexKey, new IndexNode());
                    IndexNode indexNode = indexTree.get(indexKey);
                    indexNode.addIndex(addName);

                    //重新写回
                    indexTree.put(indexKey, indexNode);
                    indexNumTrees.put(fieldName, indexNumTree);
                }
            }
            writeIndexFile(new File(table, "indexNum"), new File(table, "indexStr"), indexNumTrees, indexStrTrees);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static HashMap<String, IndexTree> getIndexNum(File file)
    {
        try(
                ObjectInputStream ans = new ObjectInputStream(new FileInputStream(new File(file, "indexNum")));
                )
        {
            return (HashMap<String, IndexTree>) ans.readObject();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, HashMap<String, IndexNode>> getIndexStr(File file)
    {
        try(
                ObjectInputStream ans = new ObjectInputStream(new FileInputStream(new File(file, "indexStr")));
        )
        {
            return (HashMap<String, HashMap<String, IndexNode>>) ans.readObject();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Map<String, String>> getFileData(File file, ArrayList<Field> fields)
    {
        List<Map<String, String>> ans = new ArrayList<>();
        try(
                Scanner in = new Scanner(new FileInputStream(file));
                )
        {
            String curStr;
            while(in.hasNextLine())
            {
                Map<String, String>an = new HashMap<>();
                curStr = in.nextLine();
                String[] values = curStr.trim().split(" ");
                int len = values.length;
                for(int i = 0; i < len; i++)
                {
                    an.put(fields.get(i).getFieldName(), values[i]);
                }
                ans.add(an);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return ans;
    }

    public static boolean rowFilter(WhereFilter filter, Map<String, String> data)
    {
        Field field = filter.getField();
        String value = data.get(field.getFieldName());
        String relation = filter.getRelation();
        String judValue = filter.getConditionValue();
        String type = field.getTypeName();
        double val;
        double judVal;
        if(type.equals(Field.VARCHAR))
            return value.equals(judValue);
        else if(type.equals(Field.INT))
        {
            val = Integer.valueOf(value);
            judVal = Integer.valueOf(judValue);
        }
        else
        {
            val = Double.valueOf(value);
            judVal = Double.valueOf(judValue);
        }
        switch (relation)
        {
            case "<":
                return val < judVal;
            case ">":
                return val > judVal;
            case "=":
                return Math.abs(val-judVal) <= 0.000001;
            default:
                return false;
        }
    }

    public static List<Map<String, String>> filterWhere(ArrayList<WhereFilter> filters, File file, ArrayList<Field> fields)
    {
        List<Map<String, String>> ans = new ArrayList<>();
        Set<File> allFile = getFilterFiles(filters, file);

        //进一步筛选具体的每个File满足条件的记录
        for(File curFile:allFile)
        {
            List<Map<String, String>> curData = getFileData(curFile, fields);
            for(Map<String, String> hang:curData)
            {
                boolean ok = true;
                if(filters != null)
                {
                    for(WhereFilter filter:filters)
                    {
                        if(!rowFilter(filter, hang))
                        {
                            ok = false;
                            break;
                        }
                    }
                }
                if(ok)
                    ans.add(hang);
            }
        }
        return ans;
    }

    public static void outSelectData(List<Map<String, String>> resultData, List<String> outField)
    {
        int len = outField.size();
        int[] maxLen = new int[len];
        for(int i = 0; i < len; i++)
        {
            if(outField.get(i).length() > maxLen[i])
                maxLen[i] = outField.get(i).length();
        }
        for(Map<String, String> row:resultData)
        {
            for(int i = 0; i < len; i++)
            {
                int tmpLen = row.get(outField.get(i)).length();
                if( tmpLen> maxLen[i])
                    maxLen[i] = tmpLen;
            }
        }
        //分界线
        System.out.print("+");
        for(int i = 0; i < len; i++)
        {
            for(int j = 0; j < maxLen[i]+2; j++)
                System.out.print("-");
            System.out.print("+");
        }
        System.out.println();

        //属性名
        System.out.print("|");
        for(int i = 0; i < len; i++)
        {
            int kong = maxLen[i]-outField.get(i).length() + 1;
            while(kong > 0)
            {
                System.out.print(" ");
                kong--;
            }
            System.out.print(outField.get(i));
            System.out.print(" |");
        }
        System.out.println();

        //分界线
        System.out.print("+");
        for(int i = 0; i < len; i++)
        {
            for(int j = 0; j < maxLen[i]+2; j++)
                System.out.print("-");
            System.out.print("+");
        }
        System.out.println();

        //值
        for(Map<String, String>row:resultData)
        {
            System.out.print("|");
            for(int i = 0; i < len; i++)
            {
                String outStr = row.get(outField.get(i));
                int kong = maxLen[i]- outStr.length() + 1;
                while(kong > 0)
                {
                    System.out.print(" ");
                    kong--;
                }
                System.out.print(outStr);
                System.out.print(" |");
            }
            System.out.println();
        }
        //分界线
        System.out.print("+");
        for(int i = 0; i < len; i++)
        {
            for(int j = 0; j < maxLen[i]+2; j++)
                System.out.print("-");
            System.out.print("+");
        }
        System.out.println();
    }

    public static Set<File> getFilterFiles(ArrayList<WhereFilter> filters, File file)
    {
        Set<File> allFile = null;

        //获取索引树
        HashMap<String, IndexTree> allIndexNum = getIndexNum(file);
        HashMap<String, HashMap<String, IndexNode>> allIndexStr = getIndexStr(file);

        //筛选出所有的File
        if(filters == null)
        {
            allFile = new HashSet<>();
            File dataFile = new File(file, "data");
            String[] strings = dataFile.list();
            for(String string:strings)
            {
                allFile.add(new File(dataFile, string));
            }
        }
        else
        {
            boolean flag = true;
            for(WhereFilter filter:filters)
            {
                Set<File> files = new HashSet<>();
                Field field = filter.getField();

                if(field.getTypeName().equals(Field.VARCHAR))
                {
                    HashMap<String, IndexNode> indexTree = allIndexStr.get(field.getFieldName());
                    IndexNode indexNode  = indexTree.get(filter.getConditionValue());
                    if(indexNode != null)
                    {
                        for(String str:indexNode.getList())
                            files.add(new File(file, "data/" + str));
                    }
                }
                else
                {
                    IndexTree indexTree = allIndexNum.get(field.getFieldName());
                    IndexKey indexKey = new IndexKey(field.getTypeName(), filter.getConditionValue());
                    List<IndexNode> indexNodes = indexTree.get(indexKey, filter.getRelation());
                    for(IndexNode indexNode:indexNodes)
                    {
                        if(indexNode != null)
                        {
                            for(String str:indexNode.getList())
                                files.add(new File(file, "data/" + str));
                        }
                    }
                }
                if(flag)
                {
                    flag = false;
                    allFile = files;
                }
                else
                    allFile.retainAll(files);
            }
        }
        return allFile;
    }

    public static void writeModifyData(FileWriter out, Map<String, String> row, ArrayList<Field> fields) throws IOException
    {
        for(Field field:fields)
        {
            out.write(row.get(field.getFieldName()));
            out.write(" ");
        }
        out.write("\n");
    }

    public static void deleteIndexModify(File table, Map<String, String> row, ArrayList<Field> fields, String deleteFileName)
    {
        try(
                ObjectInputStream indexNum = new ObjectInputStream(new FileInputStream(new File(table, "indexNum")));
                ObjectInputStream indexStr = new ObjectInputStream(new FileInputStream(new File(table, "indexStr")));
        )
        {
            HashMap<String, IndexTree> indexNumTrees = (HashMap<String, IndexTree>) indexNum.readObject();
            HashMap<String, HashMap<String, IndexNode>> indexStrTrees = (HashMap<String, HashMap<String, IndexNode>>) indexStr.readObject();
            for(Field field:fields)
            {
                String fieldName = field.getFieldName();
                String value = row.get(fieldName);
                if(field.getTypeName().equals(Field.VARCHAR))
                {
                    HashMap<String, IndexNode> tmp = indexStrTrees.get(fieldName);
                    IndexNode indexNode = tmp.get(value); //获取到了该值所在的indexNode
                    indexNode.deleteIndex(deleteFileName); //删除
                    if(indexNode.isEmpty())
                        tmp.remove(value);
                }
                else
                {
                    IndexTree indexNumTree = indexNumTrees.get(fieldName);
                    //获取indexKey
                    IndexKey indexKey = null;
                    if(field.getTypeName().equals(Field.INT))
                        indexKey = new IndexKey(Field.INT, value);
                    else
                        indexKey = new IndexKey(Field.DOUBLE, value);

                    TreeMap<IndexKey, IndexNode> indexTree = indexNumTree.getIndexTree();
                    IndexNode indexNode = indexTree.get(indexKey);
                    indexNode.deleteIndex(deleteFileName);
                    if(indexNode.isEmpty())
                        indexTree.remove(indexKey);
                }
            }
            writeIndexFile(new File(table, "indexNum"), new File(table, "indexStr"), indexNumTrees, indexStrTrees);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void updateIndexModify(File table, Field field, String oldValue, String newValue, String oldFileName)
    {
        try(
                ObjectInputStream indexNum = new ObjectInputStream(new FileInputStream(new File(table, "indexNum")));
                ObjectInputStream indexStr = new ObjectInputStream(new FileInputStream(new File(table, "indexStr")));
        )
        {
            HashMap<String, IndexTree> indexNumTrees = (HashMap<String, IndexTree>) indexNum.readObject();
            HashMap<String, HashMap<String, IndexNode>> indexStrTrees = (HashMap<String, HashMap<String, IndexNode>>) indexStr.readObject();
            if(field.getTypeName().equals(Field.VARCHAR))
            {
                HashMap<String, IndexNode> tmp = indexStrTrees.get(field.getFieldName());
                IndexNode indexNode = tmp.get(oldValue); //获取到了该值所在的indexNode
                indexNode.deleteIndex(oldFileName); //删除
                if(indexNode.isEmpty())
                    tmp.remove(oldValue);

                if(!tmp.containsKey(newValue))
                    tmp.put(newValue, new IndexNode());
                IndexNode indexNode2 = tmp.get(newValue); //获取到了该值所在的indexNode
                indexNode2.addIndex(oldFileName); //添加
            }
            else
            {
                IndexTree indexNumTree = indexNumTrees.get(field.getFieldName());
                //获取indexKey
                IndexKey indexKey = null;
                IndexKey indexKey2 = null;
                if(field.getTypeName().equals(Field.INT))
                {
                    indexKey = new IndexKey(Field.INT, oldValue);
                    indexKey2 = new IndexKey(Field.INT, newValue);
                }
                else
                {
                    indexKey = new IndexKey(Field.DOUBLE, oldValue);
                    indexKey2 = new IndexKey(Field.DOUBLE, newValue);
                }

                TreeMap<IndexKey, IndexNode> indexTree = indexNumTree.getIndexTree();
                IndexNode indexNode = indexTree.get(indexKey);
                indexNode.deleteIndex(oldFileName);                 //删除之前的
                if(indexNode.isEmpty())
                    indexTree.remove(indexKey);

                //添加新的
                if(!indexTree.containsKey(indexKey2))
                    indexTree.put(indexKey2, new IndexNode());
                IndexNode newIndexNode = indexTree.get(indexKey2);
                newIndexNode.addIndex(oldFileName);

            }
            writeIndexFile(new File(table, "indexNum"), new File(table, "indexStr"), indexNumTrees, indexStrTrees);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
