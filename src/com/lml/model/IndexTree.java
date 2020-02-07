package com.lml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class IndexTree implements Serializable
{
    private TreeMap<IndexKey, IndexNode> indexTree;

    public IndexTree()
    {
        this.indexTree = new TreeMap<>();
    }

    public TreeMap<IndexKey, IndexNode> getIndexTree()
    {
        return indexTree;
    }

    public void setIndexTree(TreeMap<IndexKey, IndexNode> indexTree)
    {
        this.indexTree = indexTree;
    }

    public List<IndexNode> get(IndexKey indexKey, String relation)
    {
        List<IndexNode> ans = new ArrayList<>();
        if(relation.equals("<"))
        {
            Map<IndexKey, IndexNode> maps = indexTree.headMap(indexKey, false);
            for(IndexNode indexNode:maps.values())
                ans.add(indexNode);
        }
        else if(relation.equals("="))
        {
            ans.add(indexTree.get(indexKey));
        }
        else
        {
            Map<IndexKey, IndexNode> maps = indexTree.tailMap(indexKey, false);
            for(IndexNode indexNode:maps.values())
                ans.add(indexNode);
        }
        return ans;
    }
}
