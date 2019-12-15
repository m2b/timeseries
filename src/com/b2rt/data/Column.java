package com.b2rt.data;

public class Column implements Comparable<Column> {

    String family;
    String name;
    Class type;

    public Column(String name,Class type)
    {
        setName(name);
        setType(type);
    }

    public Column(String family,String name,Class type)
    {
        if(family!=null && family.length()>0)
            setFamily(family);
        setName(name);
        setType(type);
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        if((name==null || name.length()<1) && (family==null || family.length()<1))
            throw(new IllegalArgumentException("name and family cannot both be null or empty"));
        this.family = family==null?"":family;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if((name==null || name.length()<1) && (family==null || family.length()<1))
            throw(new IllegalArgumentException("name and family cannot both be null or empty"));
        this.name = name==null?"":name;
    }

    public String getFullyQualifiedName()
    {
        return family==null?"":family+":"+name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        if(type==null)
            throw(new IllegalArgumentException("type cannot be null"));
        this.type = type;
    }

    @Override
    public int compareTo(Column o) {
        return this.getFullyQualifiedName().compareTo(o.getFullyQualifiedName());
    }

    @Override
    public String toString()
    {
        return getFullyQualifiedName();
    }
}
