package com.b2rt.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.commons.lang.time.DateUtils;

public class SupportedType<T extends Comparable<T>> {
    T value;

    private static final Map<String, Method> CONVERTERS = new HashMap<String, Method>();

    static {
        // Preload converters.
        Method[] methods = SupportedType.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getParameterTypes().length == 1) {
                // Converter should accept 1 argument. This skips the convert() method.
                CONVERTERS.put(method.getParameterTypes()[0].getName() + "_"
                        + method.getReturnType().getName(), method);
            }
        }
    }

    // Copy constructor
    public SupportedType(SupportedType<T> rtvalue) {
        this.value = rtvalue.getValue();
    }

    // Constructor from value
    public SupportedType(T value) throws InvalidTypeException {
        Class<?> clazz = value.getClass();
        if (isSupported(clazz))
            this.value = value;
    }

    // Constructor from object - DARN java generics Type erasure !!
    public SupportedType(Object value,Class<T> clazz) throws InvalidTypeException {
        if (isSupported(clazz))
            this.value = clazz.cast(value);
    }

    public static boolean isSupported(Class<?> clazz) throws InvalidTypeException
    {
        boolean ret=clazz.isEnum()
                    || clazz.equals(Boolean.class)
                    || clazz.equals(Integer.class)
                    || clazz.equals(Short.class)
                    || clazz.equals(Byte.class)
                    || clazz.equals(Long.class)
                    || clazz.equals(Double.class)
                    || clazz.equals(Float.class)
                    || clazz.equals(Character.class)
                    || clazz.equals(String.class)
                    || clazz.equals(Instant.class);

        if(!ret)
            throw (new InvalidTypeException("Only enum, primitive, string and instant objects supported"));
        return ret;
    }

    public static boolean isInterpolatable(Class<?> clazz)
    {
        return clazz.equals(Integer.class)
            || clazz.equals(Long.class)
            || clazz.equals(Double.class)
            || clazz.equals(Float.class);
    }

    public static <T> T valueOf(String value,Class<T> clazz) throws InvalidTypeException
    {
        if(isSupported(clazz))
            return convert(value,clazz);
        else
            return null;  //
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    static String[] formats = {
            "yyyy-MM-dd",
            "yyyy-MM-dd H:m:s",
            "yyyy-MM-dd H:m:s.S",
            "yyyy-MM-dd h:m:s a",
            "yyyy-MM-dd h:m:s.S a",
            "yyyy/MM/dd H:m:s",
            "yyyy/MM/dd H:m:s.S",
            "yyyy/MM/dd h:m:s a",
            "yyyy/MM/dd h:m:s.S a",
            "MMM d, yyyy",
            "MMM d, yyyy H:m:s",
            "MMM d, yyyy H:m:s.S",
            "MMM d, yyyy H:m:s a",
            "MMM d, yyyy H:m:s.S a"};

    public static Object parse(String s) {
        if (s == null || s.isEmpty())
            return s;
        // Short
        try {
            Short sv = Short.valueOf(s);
            return sv;
        } catch (NumberFormatException be) {
        }
        // Integer
        try {
            Integer iv = Integer.valueOf(s);
            return iv;
        } catch (NumberFormatException be) {
        }
        // Long
        try {
            Long lv = Long.valueOf(s);
            return lv;
        } catch (NumberFormatException be) {
        }
        // Float
        try {
            Float fv = Float.valueOf(s);
            return fv;
        } catch (NumberFormatException be) {
        }
        // Double
        try {
            Double dv = Double.valueOf(s);
            return dv;
        } catch (NumberFormatException be) {
        }
        // Byte
        try {
            Byte bv = Byte.valueOf(s);
            return bv;
        } catch (NumberFormatException be) {
        }
        // Character
        if (s.length() == 1)
            return s.charAt(0);
        // Boolean
        String sl = s.toLowerCase();
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
            return Boolean.valueOf(s);
        // Instant
        try {
            Instant iv = getInstant(s);
            return iv;
        }
        catch(ParseException pe)
        {}
        return s;
    }

    public static Instant getInstant(String s, ZoneId tz,DateTimeFormatter frmt) throws ParseException {
        if(tz==null)
            tz=ZoneId.systemDefault();
        if(frmt==null)
            frmt=DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime ldt=LocalDateTime.parse(s,frmt);
        ZonedDateTime zdt=ZonedDateTime.of(ldt,tz);
        return zdt.toInstant();
    }

    // Assumes string is in local timezone, supports many formats
    public static Instant getInstant(String s) throws ParseException
    {
        Date df=null;
        Instant ist=null;
        try {
            // Deal with milliseconds beyond 3 digits
            int idx=s.indexOf(".");
            String fract="";
            if(idx>0) {
                df = DateUtils.parseDateStrictly(s.substring(0, idx), formats);
                fract = s.substring(idx);
            }
            else
                df = DateUtils.parseDateStrictly(s, formats);
            ist=df.toInstant();
            // Handle fractions
            if(fract.length()>0) {
                String iso=ist.toString();
                idx=iso.toLowerCase().indexOf("z");
                if(idx>0)
                {
                    String[] parts={iso.substring(0,idx),iso.substring(idx)};
                    ist=Instant.parse(parts[0]+fract+parts[1]);
                }
                else
                {
                    idx=iso.indexOf("+");
                    if(idx>0)
                    {
                        String[] parts={iso.substring(0,idx),iso.substring(idx)};
                        ist=Instant.parse(parts[0]+fract+parts[1]);
                    }
                    else
                    {
                        idx=iso.indexOf("-");
                        if(idx>0)
                        {
                            String[] parts={iso.substring(0,idx),iso.substring(idx)};
                            ist=Instant.parse(parts[0]+fract+parts[1]);
                        }
                    }
                }
            }
        }catch(ParseException dfe)
        {
            try {
                // Try ISO format which is not supported by DateUtils
                ZonedDateTime zdt = ZonedDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME);
                ist = zdt.toInstant();
            }
            catch(DateTimeException dte)
            {
                throw(new ParseException("Unable to parse date string: "+s,0));
            }
        }
        return ist;
    }

// Converters ---------------------------------------------------------------------------------

    /**
     * Convert the given object value to the given class.
     * @param from The object value to be converted.
     * @param to The type class which the given object should be converted to.
     * @return The converted object value.
     * @throws NullPointerException If 'to' is null.
     * @throws UnsupportedOperationException If no suitable converter can be found.
     * @throws RuntimeException If conversion failed somehow. This can be caused by at least
     * an ExceptionInInitializerError, IllegalAccessException or InvocationTargetException.
     */
    public static <T> T convert(Object from, Class<T> to){

        // Null is just null.
        if (from == null) {
            return null;
        }

        // If converting to string, just return toString()
        if(to.getName()=="java.lang.String")
            return to.cast(from.toString());

        // Can we cast? Then just do it.
        if (to.isAssignableFrom(from.getClass())) {
            return to.cast(from);
        }

        // If from string...
        if(from.getClass().equals(String.class)) {
            // Enumerations have valueOf method
            if(to.getClass().isEnum())
            {
                try {
                    Method valueof = to.getMethod("valueOf", String.class);
                } catch(Exception e)
                {
                    throw new RuntimeException("Cannot convert "
                            + from
                            +" from "
                            + from.getClass().getName() + " to " + to.getName()
                            + ". Conversion failed with " + e.getMessage(), e);
                }
            }
            // Many common types have constructor
            else {
                try {
                    Constructor constr = to.getConstructor(String.class);
                    return to.cast(constr.newInstance(from));
                } catch (NoSuchMethodException nsm) {
                    // Do nothing and try to find it further down if explicitly defined
                    // System.out.print("why");
                } catch (Exception e) {
                    throw new RuntimeException("Cannot convert "
                            + from
                            +" from "
                            + from.getClass().getName() + " to " + to.getName()
                            + ". Conversion failed with " + e.getMessage(), e);
                }
            }
        }
        // Lookup the suitable converter.
        String converterId = from.getClass().getName() + "_" + to.getName();
        Method converter = CONVERTERS.get(converterId);
        if (converter == null) {
            throw new UnsupportedOperationException("Cannot convert "
                    + from
                    +" from "
                    + from.getClass().getName() + " to " + to.getName()
                    + ". Requested converter does not exist.");
        }

        // Convert the value.
        try {
            return to.cast(converter.invoke(to, from));
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert from "
                    + from.getClass().getName() + " to " + to.getName()
                    + ". Conversion failed with " + e.getMessage(), e);
        }
    }

    /**
     * Converts Integer to Boolean. If integer value is 0, then return FALSE, else return TRUE.
     * @param value The Integer to be converted.
     * @return The converted Boolean value.
     */
    public static Boolean integerToBoolean(Integer value) {
        return value == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts Boolean to Integer. If boolean value is TRUE, then return 1, else return 0.
     * @param value The Boolean to be converted.
     * @return The converted Integer value.
     */
    public static Integer booleanToInteger(Boolean value) {
        return value ? Integer.valueOf(1) : Integer.valueOf(0);
    }

    /**
     * Converts Short to Boolean. If long value is 0, then return FALSE, else return TRUE.
     * @param value The Short to be converted.
     * @return The converted Boolean value.
     */
    public static Boolean shortToBoolean(Short value) {
        return value == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts Boolean to Short. If boolean value is TRUE, then return 1, else return 0.
     * @param value The Boolean to be converted.
     * @return The converted Short value.
     */
    public static Short booleanToShort(Boolean value) {
        return value ? (short)1 : (short)0;
    }

    /**
     * Converts Byte to Boolean. If byte value is 0, then return FALSE, else return TRUE.
     * @param value The Byte to be converted.
     * @return The converted Boolean value.
     */
    public static Boolean byteToBoolean(Byte value) {
        return value == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts Boolean to Byte. If boolean value is TRUE, then return 1, else return 0.
     * @param value The Boolean to be converted.
     * @return The converted Byte value.
     */
    public static Byte booleanToByte(Boolean value) {
        return value ? (byte)1 : (byte)0;
    }

    /**
     * Converts Long to Boolean. If long value is 0, then return FALSE, else return TRUE.
     * @param value The Long to be converted.
     * @return The converted Boolean value.
     */
    public static Boolean longToBoolean(Long value) {
        return value == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts Boolean to Long. If boolean value is TRUE, then return 1, else return 0.
     * @param value The Boolean to be converted.
     * @return The converted Long value.
     */
    public static Long booleanToLong(Boolean value) {
        return value? Long.valueOf(1) : Long.valueOf(0);
    }

    /**
     * Converts Float to Boolean. If Float value is 0, then return FALSE, else return TRUE.
     * @param value The Float to be converted.
     * @return The converted Boolean value.
     */
    public static Boolean floatToBoolean(Float value) {
        return value == 0f ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts Boolean to Float. If boolean value is TRUE, then return 1, else return 0.
     * @param value The Boolean to be converted.
     * @return The converted Float value.
     */
    public static Float booleanToFloat(Boolean value) {
        return value ? Float.valueOf(1.0f) : Float.valueOf(0.0f);
    }

    /**
     * Converts Double to Boolean. If Float value is 0, then return FALSE, else return TRUE.
     * @param value The Float to be converted.
     * @return The converted Boolean value.
     */
    public static Boolean doubleToBoolean(Double value) {
        return value == 0d ? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts Boolean to Double. If boolean value is TRUE, then return 1, else return 0.
     * @param value The Boolean to be converted.
     * @return The converted Double value.
     */
    public static Double booleanToDouble(Boolean value) {
        return value ? Double.valueOf(1.0d) : Double.valueOf(0.0d);
    }

    /**
     * Converts String to Boolean. If string value is "false" then return FALSE, else return TRUE.
     * @param value The String to be converted.
     * @return The converted Boolean value.
     */
    public static Boolean stringToBoolean(String value) {
        return value.toLowerCase().equals("false")? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts Boolean to String. If boolean value is TRUE, then return "true", else return "false".
     * @param value The Boolean to be converted.
     * @return The converted String value.
     */
    public static String booleanToString(Boolean value) {
        return value ? "true" : "false";
    }

    /**
     * Converts Character to Boolean. If char value is '0' then return FALSE, else return TRUE.
     * @param value The Character to be converted.
     * @return The converted Boolean value.
     */
    public static Boolean characterToBoolean(Character value) {
        return value == '0'? Boolean.FALSE : Boolean.TRUE;
    }

    /**
     * Converts Boolean to Character. If boolean value is TRUE, then return '1', else return '0'.
     * @param value The Boolean to be converted.
     * @return The converted Character value.
     */
    public static Character booleanToCharacter(Boolean value) {
        return value ? '1' : '0';
    }


    /**
     * Converts String to Character, only if String is one character long.
     * @param value The String to be converted.
     * @return The converted Character value.
     */
    public static Character stringToCharacter(String value) {
        if(value.length()>1)
            throw(new UnsupportedOperationException("String to be converted must be no longer than 1 character"));
        return value.charAt(0);
    }

    /**
     * Converts Character to String
     * @param value The Character to be converted.
     * @return The converted String value.
     */
    public static String characterToString(Character value) {
        return new String(new char[]{value});
    }

    // Implicit numeric conversions --------------------------------
    /* byte –> short –> int –> long –> float –> double*/
    /**
     * Converts Byte to Short
     * @param value The Byte to be converted.
     * @return The converted Short value.
     */
    public static Short byteToShort(Byte value) {
        return value.shortValue();
    }

    /**
     * Converts Byte to Integer
     * @param value The Byte to be converted.
     * @return The converted Integer value.
     */
    public static Integer byteToInteger(Byte value) {
        return value.intValue();
    }

    /**
     * Converts Byte to Long
     * @param value The Byte to be converted.
     * @return The converted Long value.
     */
    public static Long byteToLong(Byte value) {
        return value.longValue();
    }

    /**
     * Converts Byte to Float
     * @param value The Byte to be converted.
     * @return The converted Float value.
     */
    public static Float byteToFloat(Byte value) {
        return value.floatValue();
    }

    /**
     * Converts Byte to Double
     * @param value The Byte to be converted.
     * @return The converted Double value.
     */
    public static Double byteToDouble(Byte value) {
        return value.doubleValue();
    }

    /**
     * Converts Short to Integer
     * @param value The Short to be converted.
     * @return The converted Integer value.
     */
    public static Integer shortToInteger(Short value) {
        return value.intValue();
    }

    /**
     * Converts Short to Long
     * @param value The Short to be converted.
     * @return The converted Long value.
     */
    public static Long shortToLong(Short value) {
        return value.longValue();
    }

    /**
     * Converts Short to Float
     * @param value The Short to be converted.
     * @return The converted Float value.
     */
    public static Float shortToFloat(Short value) {
        return value.floatValue();
    }

    /**
     * Converts Short to Double
     * @param value The Short to be converted.
     * @return The converted Double value.
     */
    public static Double shortToDouble(Short value) {
        return value.doubleValue();
    }

    /**
     * Converts Integer to Long
     * @param value The Integer to be converted.
     * @return The converted Long value.
     */
    public static Long integerToLong(Integer value) {
        return value.longValue();
    }

    /**
     * Converts Integer to Float
     * @param value The Integer to be converted.
     * @return The converted Float value.
     */
    public static Float integerToFloat(Integer value) {
        return value.floatValue();
    }

    /**
     * Converts Integer to Double
     * @param value The Integer to be converted.
     * @return The converted Double value.
     */
    public static Double integerToDouble(Integer value) {
        return value.doubleValue();
    }

    /**
     * Converts Long to Float
     * @param value The Long to be converted.
     * @return The converted Float value.
     */
    public static Float longToFloat(Long value) {
        return value.floatValue();
    }

    /**
     * Converts Long to Double
     * @param value The Long to be converted.
     * @return The converted Double value.
     */
    public static Double longToDouble(Long value) {
        return value.doubleValue();
    }

    /**
     * Converts Float to Double.
     * @param value The Float to be converted.
     * @return The converted Double value.
     */
    public static Double floatToDouble(Float value) {
        return Double.parseDouble(value.toString());
    }

     /**
     * Converts Double to Float.
     * @param value The Double to be converted.
     * @return The converted Float value.
     */
    public static Float doubleToFloat(Double value) {
        return value.floatValue();
    }

    /**
     * Converts Double to Long.
     * @param value The Double to be converted.
     * @return The converted Long value.
     */
    public static Long doubleToLong(Double value) {
        return Math.round(value);
    }

    /**
     * Converts Double to Integer.
     * @param value The Double to be converted.
     * @return The converted Integer value.
     */
    public static Integer doubleToInteger(Double value) {
        return Math.round(value.floatValue());
    }

    /**
     * Converts Float to Long.
     * @param value The Float to be converted.
     * @return The converted Long value.
     */
    public static Long floatToLong(Float value) {
        return new Integer(Math.round(value)).longValue();
    }

    /**
     * Converts Float to Integer.
     * @param value The Float to be converted.
     * @return The converted Integer value.
     */
    public static Integer floatToInteger(Float value) {
        return Math.round(value);
    }

    //  Instant ---------------------------------------
    /**
     * Converts String to Instant.
     * @param value The String to be converted.
     * @return The converted Instant value.
     */
    public static Instant stringToInstant(String value) throws ParseException {
        return SupportedType.getInstant(value);
    }
}
