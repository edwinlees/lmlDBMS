package com.lml.model;


public class WhereFilter
{
    private Field field;
    private String relation;
    private String conditionValue;

    public WhereFilter(Field field, String relation, String conditionValue)
    {
        this.field = field;
        this.relation = relation;
        this.conditionValue = conditionValue;
    }

    public Field getField()
    {
        return field;
    }

    public void setField(Field field)
    {
        this.field = field;
    }

    public String getRelation()
    {
        return relation;
    }

    public void setRelation(String relation)
    {
        this.relation = relation;
    }

    public String getConditionValue()
    {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue)
    {
        this.conditionValue = conditionValue;
    }
}
