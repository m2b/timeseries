package com.b2rt.data;

import java.time.Instant;

public class ColumnValue<T extends Comparable<T>>
{
    Column column;
    SupportedType<T> value;
    Instant timestamp;

    public ColumnValue(Column column,SupportedType<T> value,Instant timestamp)
    {
        setColumn(column);
        this.value=value;
        this.timestamp=timestamp;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        if(column==null)
            throw(new IllegalArgumentException("column cannot be null"));
        this.column = column;
    }

    public SupportedType<T> getValue() {
        return value;
    }

    public void setValue(SupportedType<T> value)
    {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}