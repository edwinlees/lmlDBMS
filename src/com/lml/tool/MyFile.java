package com.lml.tool;

import java.io.File;

public class MyFile
{
    public static boolean delFile(File file) //递归删除文件
    {
        if(!file.exists())
            return false;
        if(file.isDirectory())
        {
            File[] files = file.listFiles();
            for(File f: files)
                delFile(f);
        }
        return file.delete();
    }
}

