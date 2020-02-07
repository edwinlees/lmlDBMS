package com.lml.model;

import java.io.Serializable;

public class IndexKey implements Comparable<IndexKey>, Serializable
{
    private String type;
    private String value;

    public IndexKey(String type, String value)
    {
        this.type = type;
        this.value = value;
    }

    @Override
    public int compareTo(IndexKey o)
    {
        if(type.equals("int"))
            return Integer.valueOf(value).compareTo(Integer.valueOf(o.getValue()));
        else
            return Double.valueOf(value).compareTo(Double.valueOf(o.getValue()));
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKey indexKey = (IndexKey) o;
        return type.equals(indexKey.getType()) && value.equals(indexKey.getValue());
    }

    @Override
    public int hashCode()
    {
        return value.hashCode();
    }
}
