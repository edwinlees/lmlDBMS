package com.lml.model;

import java.io.*;

public class User implements Serializable
{
    public final static int ALL_AUTH = 1;
    public final static int MODIFY_AUTH = 2;
    public final static int READ_AUTH = 3;

    private String name;
    private String password;
    private int level;

    public static String createUser(String userName, String pass)
    {
        if(havUser(userName))return "创建失败，用户以存在\n";
        File newFile = new File("data/users", userName);
        newFile.mkdir();
        User newUser = new User();
        newUser.setName(userName);

        //默认所有权限
        newUser.setLevel(ALL_AUTH);
        newUser.setPassword(pass);

        File file = new File(newFile, "userInformation");
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        (
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        )
        {
            out.writeObject(newUser);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "创建成功\n";
    }

    public static boolean havUser(String userName)
    {
        File dataDir = new File("data/users");
        String[] allUser = dataDir.list();
        for(String tmp:allUser)
        {
            if(tmp.equals(userName))
                return true;
        }
        return false;
    }

    public static boolean havDatabase(String databaseName)
    {
        File father = new File("data", "databases");
        String[] allDir = father.list();
        for(String tmp:allDir)
        {
            if(tmp.equals(databaseName))
            {
                return true;
            }
        }
        return false;
    }

    public String getName()
    {
        return name;
    }

    public String getPassword()
    {
        return password;
    }

    public int getLevel()
    {
        return level;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }
}
