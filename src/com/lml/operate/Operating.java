package com.lml.operate;

import com.lml.model.*;
import com.lml.tool.MyFile;
import com.lml.tool.MyString;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Operating
{

    private static File dataDir = new File("data"); //数据存储根目录
    private static Scanner in = new Scanner(System.in);
    private User curUser;                             //当前用户
    private File curDatabase;                       //当前选择的数据库
    private boolean alreadyUse = false;         //已选择数据库标志
    private String command;

    //各种操作的pattern

    //DCL DDL
    private static final Pattern PATTERN_LOGIN = Pattern.compile("\\s*mysql\\s+-u\\s*(\\w+)\\s+-p\\s*(\\w+)\\s*");
    private static final Pattern PATTERN_USE_DATABASE = Pattern.compile("\\s*use\\s+(\\w+)\\s*");
    private static final Pattern PATTERN_CREATE_USER = Pattern.compile("\\s*create\\s+user\\s+(\\w+)\\s+(\\w+)\\s*;");
    private static final Pattern PATTERN_CREATE_DATABASE = Pattern.compile("\\s*create\\s+database\\s+(\\w+)\\s*;");
    private static final Pattern PATTERN_GRANT = Pattern.compile("\\s*grant\\s+(\\w+)\\s+to\\s+(\\w+)\\s*;");
    private static final Pattern PATTERN_DROP_DATABASE = Pattern.compile("\\s*drop\\s+database\\s+(\\w+)\\s*;");
    private static final Pattern PATTERN_DROP_USER = Pattern.compile("\\s*drop\\s+user\\s+(\\w+)\\s*;");
    private static final Pattern PATTERN_SHOW = Pattern.compile("\\s*show\\s+(databases|tables)\\s*;");
    private static final Pattern PATTERN_CREATE_TABLE = Pattern.compile("\\s*create\\s+table\\s+(\\w+)\\s*\\(((?:\\s*\\w+\\s+\\w+\\s*,)*(?:\\s*\\w+\\s+\\w+\\s*))\\)\\s*;");
    private static final Pattern PATTERN_DROP_TABLE = Pattern.compile("\\s*drop\\s+table\\s+(\\w+)\\s*;");
    private static final Pattern PATTERN_DESC = Pattern.compile("\\s*desc\\s+(\\w+)");

    //DML DQL
    private static final Pattern PATTERN_INSERT = Pattern.compile("\\s*insert\\s+into\\s+(\\w+)(\\(((?:\\s*\\w+\\s*,)*(?:\\s*\\w+\\s*))\\))?\\s+values\\(((?:\\s*.+\\s*,)*(?:\\s*.+\\s*))\\)\\s*;");
    private static final Pattern PATTERN_DELETE = Pattern.compile("\\s*delete\\s+from\\s+(\\w+)(?:\\s+where\\s+(\\w+\\s*[<=>]\\s*.+(?:\\s+and\\s+\\w+\\s*[<=>]\\s*.+)*\\s*;))?");
    private static final Pattern PATTERN_SELECT = Pattern.compile("\\s*select\\s+(\\*|(?:\\w+(?:\\.\\w+)?)(?:\\s*,\\s*(?:\\w+(?:\\.\\w+)?))*)\\s+from\\s+(\\w+(?:\\s*,\\s*\\w+)*)(?:\\s+where\\s+(\\w+(?:\\.\\w+)?\\s*[<=>]\\s*.+(?:\\s+and\\s+\\w+(?:\\.\\w+)?\\s*[<=>]\\s*.+)*\\s*;))?");
    private static final Pattern PATTERN_UPDATE = Pattern.compile("\\s*update\\s+(\\w+)\\s+set\\s+(\\w+)\\s*=\\s*([^\\s]+)\\s+(?:\\s*where\\s+(\\w+\\s*[<=>]\\s*.+(?:\\s+and\\s+\\w+\\s*[<=>]\\s*.+)*\\s*;))?");



    public void start()
    {
        //初始化
        init();
        //第一步：登录验证
        login();
        //第二步：其余操作
        otherOperating();
        System.out.println("已退出，再见！！");
    }

    public void init()
    {
        File file = new File("data");
        if(file.exists())return;
        file.mkdir();
        File databases = new File(file, "databases");
        databases.mkdir();
        File users = new File(file, "users");
        users.mkdir();
        User.createUser("root", "123456");
    }

    private void login()
    {
        while(true)
        {
            command = in.nextLine();
            Matcher matLogin = PATTERN_LOGIN.matcher(command);

            boolean loginOK = false;
            while(matLogin.find())
            {
                String userName = matLogin.group(1);
                String password = matLogin.group(2);
                if(!User.havUser(userName))
                {
                    System.out.println("无此用户");
                    continue;
                }
                try
                 (
                      ObjectInputStream inUser = new ObjectInputStream(new FileInputStream(new File(dataDir, "users/" + userName + "/" + "userInformation")));
                 )
                {
                    User judUser = (User) inUser.readObject();
                    if(password.equals(judUser.getPassword()))
                    {
                        curUser = judUser;
                        loginOK = true;
                    }
                    else
                        System.out.println("密码错误");
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            if(loginOK)
            {
                System.out.println("登录成功");
                break;
            }
            System.out.println("请先登录");
        }
    }

    private void otherOperating()
    {

        while(!(command = in.nextLine()).equals("exit"))
        {
            boolean identifyOK = false;
            Matcher matCreateDatabase = PATTERN_CREATE_DATABASE.matcher(command);
            Matcher matCreateUser = PATTERN_CREATE_USER.matcher(command);
            Matcher matUseDatabase = PATTERN_USE_DATABASE.matcher(command);
            Matcher matGrant = PATTERN_GRANT.matcher(command);
            Matcher matDropDatabase = PATTERN_DROP_DATABASE.matcher(command);
            Matcher matDropUser = PATTERN_DROP_USER.matcher(command);
            Matcher matShow = PATTERN_SHOW.matcher(command);
            Matcher matCreateTable = PATTERN_CREATE_TABLE.matcher(command);
            Matcher matDropTable = PATTERN_DROP_TABLE.matcher(command);
            Matcher matDesc = PATTERN_DESC.matcher(command);
            Matcher matInsert = PATTERN_INSERT.matcher(command);
            Matcher matSelect = PATTERN_SELECT.matcher(command);
            Matcher matDelete = PATTERN_DELETE.matcher(command);
            Matcher matUpdate = PATTERN_UPDATE.matcher(command);


            while(matCreateDatabase.find())
            {
                identifyOK = true;
                if(curUser.getLevel() == User.ALL_AUTH)
                    createDatabase(matCreateDatabase);
                else
                    System.out.println("权限不够");
            }

            while(matCreateUser.find())
            {
                identifyOK = true;
                if(curUser.getLevel() == User.ALL_AUTH)
                {
                    String userName = matCreateUser.group(1);
                    String password = matCreateUser.group(2);
                    System.out.print(User.createUser(userName, password));
                }
                else
                    System.out.println("权限不够");
            }

            while(matUseDatabase.find())
            {
                identifyOK = true;
                useDatabase(matUseDatabase);
            }

            while(matGrant.find())
            {
                identifyOK = true;
                grant(matGrant);
            }

            while(matDropDatabase.find())
            {
                identifyOK = true;
                if(curUser.getLevel() == User.ALL_AUTH)
                    dropDatabase(matDropDatabase);
                else
                    System.out.println("权限不够");
            }

            while(matDropUser.find())
            {
                identifyOK = true;
                if(curUser.getLevel() == User.ALL_AUTH)
                    dropUser(matDropUser);
                else
                    System.out.println("权限不够");
            }

            while(matShow.find())
            {
                identifyOK = true;
                show(matShow);
            }

            while(matCreateTable.find())
            {
                identifyOK = true;
                if(!alreadyUse)
                {
                    System.out.println("请先选择数据库");
                    break;
                }
                if(curUser.getLevel() == User.ALL_AUTH)
                    createTable(matCreateTable);
                else
                    System.out.println("权限不够");
            }

            while(matDropTable.find())
            {
                identifyOK = true;
                if(curUser.getLevel() == User.ALL_AUTH)
                {
                    if(alreadyUse)
                        dropTable(matDropTable);
                    else
                        System.out.println("请先选择数据库");
                }
                else
                    System.out.println("权限不够");
            }

            while(matDesc.find())
            {
                identifyOK = true;
                if(!alreadyUse)
                {
                    System.out.println("请先选择数据库");
                    break;
                }
                desc(matDesc);
            }

            while(matInsert.find())
            {
                identifyOK = true;
                if(!alreadyUse)
                {
                    System.out.println("请先选择数据库");
                    break;
                }
                if(curUser.getLevel() == User.ALL_AUTH)
                    insert(matInsert);
                else
                    System.out.println("权限不够");
            }

            while(matSelect.find())
            {
                identifyOK = true;
                if(!alreadyUse)
                {
                    System.out.println("请先选择数据库");
                    break;
                }
                select(matSelect);
            }

            while(matDelete.find())
            {
                identifyOK = true;
                if(!alreadyUse)
                {
                    System.out.println("请先选择数据库");
                    break;
                }
                if(curUser.getLevel() != User.ALL_AUTH)
                    System.out.println("权限不够");
                else
                    delete(matDelete);
            }

            while(matUpdate.find())
            {
                identifyOK = true;
                if(!alreadyUse)
                {
                    System.out.println("请先选择数据库");
                    break;
                }
                if(curUser.getLevel() != User.ALL_AUTH)
                    System.out.println("权限不够");
                else
                    update(matUpdate);
            }

            if(!identifyOK) System.out.println("错误指令");
        }
    }

    private void createDatabase(Matcher matCreateDatabase)
    {
        String newDatabase = matCreateDatabase.group(1);
        if(User.havDatabase(newDatabase))
        {
            System.out.println("该数据库以存在");
            return;
        }
        File create = new File(dataDir, "databases" + "/" + newDatabase);
        create.mkdir();
        System.out.println("成功创建数据库：" + newDatabase);
    }

    private void useDatabase(Matcher matUseDatabase)
    {
        String useDatabaseName = matUseDatabase.group(1);
        if(User.havDatabase(useDatabaseName))
        {
            curDatabase = new File(dataDir, "databases" + "/" + useDatabaseName);
            alreadyUse = true;
            System.out.println("成功选择数据库：" + useDatabaseName);
        }
        else
            System.out.println("没有此数据库");
    }

    private void grant(Matcher matGrant)
    {
        String auth = matGrant.group(1);
        String userName = matGrant.group(2);
        int authNum = 0;
        if(auth.equals("all"))
            authNum = 1;
        else if(auth.equals("insert"))
            authNum = 2;
        else if(auth.equals("select"))
            authNum = 3;
        else
        {
            System.out.println("无此权限");
            return;
        }
        if(User.havUser(userName))
        {
            if(curUser.getLevel() == User.ALL_AUTH)
            {
                File file = new File(dataDir, "users/" + userName + "/" + "userInformation");
                try
                {
                    ObjectInputStream inUser = new ObjectInputStream(new FileInputStream(file));
                    User user = (User) inUser.readObject();
                    user.setLevel(authNum);
                    inUser.close();
                    ObjectOutputStream outUser = new ObjectOutputStream(new FileOutputStream(file));
                    outUser.writeObject(user);
                    outUser.close();
                    System.out.println("修改成功");
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
            else
                System.out.println("权限不够");
        }
        else
            System.out.println("无此用户");
    }

    private void dropDatabase(Matcher matDropDatabase)
    {
        String databaseName = matDropDatabase.group(1);
        if(User.havDatabase(databaseName))
        {
            File file = new File(dataDir, "databases" + "/" + databaseName);
            MyFile.delFile(file);
            System.out.println("成功删除");
        }
        else
            System.out.println("不存在此数据库");
    }

    private void dropUser(Matcher matDropUser)
    {
        String userName = matDropUser.group(1);
        if(User.havUser(userName))
        {
            File file = new File(dataDir, userName);
            MyFile.delFile(file);
            System.out.println("成功删除用户：" + userName);
        }
        else
            System.out.println("没有这个用户");
    }

    private void show(Matcher matShow)
    {
        String jud = matShow.group(1);
        if(jud.equals("databases"))
        {
            File file = new File(dataDir, "databases");
            File[] files = file.listFiles();
            boolean havFlag = false;
            for(File f:files)
            {
                if(f.isDirectory())
                {
                    if(!havFlag) System.out.println("数据库如下：");
                    havFlag = true;
                    System.out.println(f.getName());
                }
            }
            if(!havFlag) System.out.println("未创建数据库");
        }
        else
        {
            if(alreadyUse)
            {
                File[] files = curDatabase.listFiles();
                boolean havFlag = false;
                for(File f:files)
                {
                    if(f.isDirectory())
                    {
                        if(!havFlag) System.out.println("数据库" + curDatabase.getName() + "的表如下：");
                        havFlag = true;
                        System.out.println(f.getName());
                    }
                }
                if(!havFlag) System.out.println("数据库" + curDatabase.getName() + "还未创建表");
            }
            else
            {
                System.out.println("请先选择数据库");
            }
        }
    }

    private void createTable(Matcher matCreateTable)
    {
        String tableName = matCreateTable.group(1);
        if(Table.havTable(curDatabase, tableName))
            System.out.println("该表已存在");
        else
        {
            //创建表文件夹、创建存放数据文件夹、创建数据字典文件、索引文件
            File tableFile = new File(curDatabase, tableName);
            tableFile.mkdir();
            File dataFile = new File(tableFile, "data");
            File indexNumFile = new File(tableFile, "indexNum");
            File indexStrFile = new File(tableFile, "indexStr");
            dataFile.mkdir();
            File dataDictFile = new File(tableFile, "dataDict");
            try
            {
                dataDictFile.createNewFile();
                indexNumFile.createNewFile();
                indexStrFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            //分析创建语句的属性
            ArrayList<Field> dataDict = MyString.parseCreateTable(matCreateTable.group(2));

            //将dataDict写入文件
            Table.writeDataDict(dataDictFile, dataDict, false);

            //创建并写入索引
            HashMap<String, IndexTree> indexNum = new HashMap<>();
            HashMap<String, HashMap<String, IndexNode>> indexStr = new HashMap<>();
            for(Field field:dataDict)
            {
                if(field.getTypeName().equals("int") || field.getTypeName().equals("double"))
                    indexNum.put(field.getFieldName(), new IndexTree());
                else if(field.getTypeName().equals("varchar"))
                    indexStr.put(field.getFieldName(), new HashMap<String, IndexNode>());
                else
                {
                    try
                    {
                        throw new Exception("不支持此类型："+field.getTypeName());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            Table.writeIndexFile(indexNumFile, indexStrFile, indexNum, indexStr);
            System.out.println("成功创建表：" + tableName);
        }
    }

    private void dropTable(Matcher matDropTable)
    {
        String tableName = matDropTable.group(1);
        if(Table.havTable(curDatabase, tableName))
        {
            File file = new File(curDatabase, tableName);
            MyFile.delFile(file);
            System.out.println("成功删除表：" + tableName);
        }
        else
            System.out.println("没有此表");
    }

    private void insert(Matcher matInsert)
    {
        String tableName = matInsert.group(1);
        if(Table.havTable(curDatabase, tableName))
        {
            ArrayList<Field> fields = Table.getDataDict(curDatabase, tableName);
            String judStr = matInsert.group(3);
            Map<String, String> dataStr;
            if(judStr == null)
            {
                String[] values = matInsert.group(4).trim().split(",");
                dataStr = new HashMap<>();
                for(int i = 0; i < values.length; i++)
                {
                    dataStr.put(fields.get(i).getFieldName(), values[i].trim());
                }
            }
            else
            {
                dataStr = MyString.parseInsert(matInsert.group(3), matInsert.group(4));
                if(dataStr == null)
                {
                    System.out.println("插入属性项不全");
                    return;
                }
            }
            if(MyString.judgeType(fields, dataStr))
            {
                Table.writeInsertData(new File(curDatabase, tableName), fields, dataStr);
                System.out.println("插入数据成功");
            }
            else
                System.out.println("插入数据类型出错");
        }
        else
            System.out.println("没有此表：" + tableName);
    }

    private void select(Matcher matSelect)
    {
        String selectStr = matSelect.group(1);
        String fromStr = matSelect.group(2);
        String whereStr = matSelect.group(3);
        boolean whereHav = true;
        if(whereStr == null)
            whereHav = false;


        List<String> tableNames = MyString.parseFrom(fromStr); //获取所有表名
        Map<String, List<Map<String, String>>> allData = new HashMap<>(); //所有表选择后的数据
        Map<String, List<String>> selectField = new LinkedHashMap<>();  //所有表投影后的属性名
        Map<String, ArrayList<Field>> allField = new HashMap<>();

        //对每个表相关条件的选择，得到满足条件的记录
        for(String tableName:tableNames)
        {
            File file = new File(curDatabase, tableName);
            if(!file.exists())
            {
                System.out.println("不存在表：" + tableName);
                return;
            }
            ArrayList<Field> fields = Table.getDataDict(curDatabase, tableName);
            ArrayList<WhereFilter> filters = MyString.parseWhere(whereStr, fields, tableName);
            List<Map<String, String>> data = Table.filterWhere(filters, file, fields);
            allData.put(tableName, data);
            //该表需要投影的属性
            selectField.put(tableName, MyString.parseSelect(selectStr, tableName, fields));

            allField.put(tableName, fields);
        }

        //连接操作，同时添加连接所需要的属性
        Map<String, List<String>> selectUseField = new HashMap<>(selectField);
        List<Join> joins;
        if(whereHav)
        {
            joins = MyString.parseConcat(whereStr, allField, selectUseField);
            if(joins == null)
            {
                System.out.println("连接条件不对");
                return;
            }
        }
        else
            joins = new ArrayList<>();
        //去除不用的数据
        for(String key:allData.keySet())
        {
            List<Map<String, String>> data = allData.get(key);
            for(int i = 0; i < data.size(); i++)
            {
                Map<String, String> row = data.get(i);
                Set<String>delKey = new HashSet<>();
                for(String fieldName:row.keySet())
                {
                    if(!selectUseField.get(key).contains(fieldName))
                        delKey.add(fieldName);
                }
                for(String del:delKey)
                    row.remove(del);
            }
        }

        //连接操作，获得最终的数据
        List<Map<String, String>> resultData = null;
        if(joins.size() > 0)
            resultData = Join.tableConcat(joins, allData);
        else
        {
            resultData = new LinkedList<>();
            int i = 0;
            for(List<Map<String, String>> data:allData.values())
            {
                String tableName = tableNames.get(i);
                for(Map<String, String>row:data)
                {
                    Map<String, String> addMap = new HashMap<>();
                    for(String str:row.keySet())
                    {
                        if(str.contains(tableName+"."))
                        {
                            addMap.put(str, row.get(str));
                        }
                        else
                            addMap.put(tableName + "." + str, row.get(str));
                    }
                    resultData.add(addMap);
                }
                i++;
            }
        }

        //按格式输出 resultData
        List<String> tmp = null;
        if(selectStr.equals("*"))
        {
            tmp = new ArrayList<>();
            for(String tableName:tableNames)
            {
                List<Field> fields = allField.get(tableName);
                for(Field field:fields)
                    tmp.add(tableName + "." + field.getFieldName());
            }
        }
        else
            tmp = MyString.parseFrom(selectStr);
        List<String> outField = new ArrayList<>();
        for(String str:tmp)
        {
            if(str.contains("."))
                outField.add(str);
            else
            {
                for(String tableName:tableNames)
                {
                    if(Field.havFieldName(str, allField.get(tableName)))
                    {
                        outField.add(new String(tableName + "." + str));
                        break;
                    }
                }
            }
        }
        Table.outSelectData(resultData, outField);
    }

    private void delete(Matcher matDelete)
    {
        String tableName = matDelete.group(1);
        String whereStr = matDelete.group(2);
        if(!Table.havTable(curDatabase, tableName))
        {
            System.out.println("没有表：" + tableName);
            return;
        }
        ArrayList<Field> fields = Table.getDataDict(curDatabase, tableName);        //所有属性
        ArrayList<WhereFilter> filters = MyString.parseWhere(whereStr, fields, tableName); //所有filter
        Set<File> okFiles = Table.getFilterFiles(filters, new File(curDatabase, tableName)); //满足条件的file
        for(File file:okFiles) //对筛选到的文件进行处理
        {
            List<Map<String, String>> curData = Table.getFileData(file, fields);
            try(
                    FileWriter out = new FileWriter(file);
                    )
            {
                for(Map<String, String> hang:curData)
                {
                    boolean ok = true;
                    if(filters != null)
                    {
                        for(WhereFilter filter:filters)
                        {
                            if(!Table.rowFilter(filter, hang))
                            {
                                ok = false;
                                break;
                            }
                        }
                    }
                    if(ok)
                    {
                        //索引维护
                        Table.deleteIndexModify(new File(curDatabase, tableName), hang, fields, file.getName());
                    }
                    else
                    {
                        //写入数据
                        Table.writeModifyData(out, hang, fields);
                    }
                }
                out.flush();
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
        System.out.println("成功删除");
    }

    private void update(Matcher matUpdate)
    {
        String tableName = matUpdate.group(1);
        String setFieldName = matUpdate.group(2);
        String setValue = matUpdate.group(3);
        String whereStr = matUpdate.group(4);
        ArrayList<Field> fields = Table.getDataDict(curDatabase, tableName);        //所有属性
        Field setField = null;
        for(Field field:fields)
        {
            if(field.getFieldName().equals(setFieldName))
            {
                setField = field;
                break;
            }
        }
        ArrayList<WhereFilter> filters = MyString.parseWhere(whereStr, fields, tableName); //所有filter
        Set<File> okFiles = Table.getFilterFiles(filters, new File(curDatabase, tableName)); //满足条件的file
        for(File file:okFiles) //对筛选到的文件进行处理
        {
            List<Map<String, String>> curData = Table.getFileData(file, fields);
            try(
                    FileWriter out = new FileWriter(file);
            )
            {
                for(Map<String, String> hang:curData)
                {
                    boolean ok = true;
                    if(filters != null)
                    {
                        for(WhereFilter filter:filters)
                        {
                            if(!Table.rowFilter(filter, hang))
                            {
                                ok = false;
                                break;
                            }
                        }
                    }
                    if(ok)
                    {
                        //索引维护
                        Table.updateIndexModify(new File(curDatabase, tableName), setField, hang.get(setFieldName), setValue, file.getName());
                        hang.put(setFieldName, setValue);
                    }
                    //写入数据
                    Table.writeModifyData(out, hang, fields);
                }
                out.flush();
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
        System.out.println("成功更新");
    }

    private void desc(Matcher matDesc)
    {
        String tableName = matDesc.group(1);
        ArrayList<Field> fields = Table.getDataDict(curDatabase, tableName);
        int maxLenField = 5, maxLenType = 4;
        for(Field field:fields)
        {
            maxLenField = Math.max(maxLenField, field.getFieldName().length());
            maxLenType = Math.max(maxLenType, field.getTypeName().length());
        }
        System.out.print("+");
        for(int i = 0; i < maxLenField + 2; i++)
            System.out.print("-");
        System.out.print("+");
        for(int i = 0; i < maxLenType + 2; i++)
            System.out.print("-");
        System.out.print("+\n");

        System.out.print("| Field");
        for(int i = 0; i < maxLenField - 4; i++)
            System.out.print(" ");
        System.out.print("|");
        System.out.print(" Type");
        for(int i = 0; i < maxLenType - 3; i++)
            System.out.print(" ");
        System.out.print("|\n");

        System.out.print("+");
        for(int i = 0; i < maxLenField + 2; i++)
            System.out.print("-");
        System.out.print("+");
        for(int i = 0; i < maxLenType + 2; i++)
            System.out.print("-");
        System.out.print("+\n");
        for(Field field:fields)
        {
            System.out.print("| ");
            System.out.print(field.getFieldName());
            for(int i = 0; i < maxLenField - field.getFieldName().length() + 1; i++)
                System.out.print(" ");
            System.out.print("| ");
            System.out.print(field.getTypeName());
            for(int i = 0; i < maxLenType - field.getTypeName().length() + 1; i++)
                System.out.print(" ");
            System.out.print("|\n");
        }
        System.out.print("+");
        for(int i = 0; i < maxLenField + 2; i++)
            System.out.print("-");
        System.out.print("+");
        for(int i = 0; i < maxLenType + 2; i++)
            System.out.print("-");
        System.out.print("+\n");
    }
}
