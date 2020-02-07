package com.lml.model;

import java.io.Serializable;
import java.util.LinkedList;

public class IndexNode implements Serializable
{
    private LinkedList<String> list;

    public IndexNode()
    {
        this.list = new LinkedList<>();
    }

    public void addIndex(String index)
    {
        list.add(index);
    }

    public LinkedList<String> getList()
    {
        return list;
    }

    public void setList(LinkedList<String> list)
    {
        this.list = list;
    }

    public void deleteIndex(String index)
    {
        list.remove(index);
    }

    public boolean isEmpty()
    {
        return list.isEmpty();
    }
}
