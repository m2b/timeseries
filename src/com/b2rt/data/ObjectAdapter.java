package com.b2rt.data;

@FunctionalInterface
public interface ObjectAdapter<T,K extends Comparable<K>> {
    T getObject(Row<K> row);
}
