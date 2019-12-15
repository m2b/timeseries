package com.b2rt.data;

import java.util.HashMap;
import java.util.List;

public class Row<K extends Comparable<K>> implements Comparable<Row<K>>{
    K key;
    HashMap<String,ColumnValue> values=new HashMap<>();

    public Row(K key, List<ColumnValue> values)
    {
        setKey(key);
        setValues(values);
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key)
    {
        if(key==null)
            throw(new IllegalArgumentException("Key cannot be null"));
        this.key=key;
    }

    public HashMap<String,ColumnValue> getValues() {
        return values;
    }

    public void setValues(List<ColumnValue> values) {
        if(values==null || values.isEmpty())
            throw(new IllegalArgumentException("Values cannot be null or empty"));
        for(ColumnValue val:values)
            this.values.put(val.getColumn().getFullyQualifiedName(),val);
    }

    public void setValues(HashMap<String,ColumnValue> values) {
        if(values==null || values.isEmpty())
            throw(new IllegalArgumentException("Values cannot be null or empty"));
        this.values=values;
    }
    @Override
    public int compareTo(Row<K> o) {
        return getKey().compareTo(o.getKey());
    }
}
