package com.b2rt.timeseries;

import com.b2rt.data.SupportedType;

import java.net.URI;

public class Measurement<T extends  Comparable<T>> {
    URI uri; // example phd://hostname:1000/tag1
    String description;
    String uom; // TODO: Make its own class
    Class type;
    private T maximum;
    private T minimum;

    public Measurement(URI uri, String description, String uom, SupportedType<T> minimum, SupportedType<T> maximum)
    {
        setType(minimum.getValue().getClass());
        setUri(uri);
        setDescription(description);
        setUom(uom);
        if(type!=String.class && type!=Boolean.class) {
            setMaximum(maximum.getValue());
            setMinimum(minimum.getValue());
        }
    }

    public URI getUri()
    {
        return uri;
    }

    public void setUri(URI uri)
    {
        if(uri==null)
            throw(new NullPointerException("Uri cannot be null"));
        this.uri=uri;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description=description;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public void setType(Class type) {
         this.type = type;
    }

    public Class<T> getType() {
        return type;
    }


    public T getMaximum() {
        return maximum;
    }

    public void setMaximum(T maximum) {
        if(minimum!=null && maximum.compareTo(minimum)<=0)
        {
            throw(new IllegalArgumentException("Maximum must be less than minimum for measurement: "+uri));
        }
        this.maximum = maximum;
    }

    public T getMinimum() {
        return minimum;
    }

    public void setMinimum(T minimum) {
        if(maximum!=null && minimum.compareTo(maximum)>=0)
        {
            throw(new IllegalArgumentException("Minimum must be less than maximum for measurement: "+uri));
        }        this.minimum = minimum;
    }

    @Override
    public String toString()
    {
        StringBuilder builder=new StringBuilder();
        builder.append("uri=");
        builder.append(uri);
        builder.append(", description=");
        builder.append(description);
        builder.append(", class=");
        builder.append(type.getName());
        builder.append(", min=");
        builder.append(minimum);
        builder.append(", maximum=");
        builder.append(maximum);
        return builder.toString();
    }
}
