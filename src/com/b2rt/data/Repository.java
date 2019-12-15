package com.b2rt.data;

import java.util.Optional;
import java.util.Set;

public interface Repository {

    <T> Optional<T> get(T key);

    <T> Optional<T> getMostRecent(T startKey, T endKey);

    <T> Set<T> get(T startKey, T endKey, boolean includeStart, boolean includEnd);

    <T> void persist(T entity);

    <T> void remove(T entity);
}
