package com.b2rt.data;

import java.util.HashMap;
import java.util.Set;

public interface Metadata {
    HashMap<String,Class> getColumns(String tableName);
    long getRowCount(String tableName);
    Set<String> getTables();
}
