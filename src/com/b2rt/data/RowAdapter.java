package com.b2rt.data;

@FunctionalInterface
public interface RowAdapter<T,K extends Comparable<K>> {
    Row<K> getRow(T object);
}
