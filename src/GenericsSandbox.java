import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GenericsSandbox {


    public static void main(String[] args) {
        // Try a few examples and print them
        List<String> strings = new ArrayList<>();
        Class clazz=getClass(strings);
        System.out.printf("Expected 'java.lang,String', got %s%n",clazz.getName());
    }

    public static <T> Class<T> getClass(List<T> object)
    {
        Class clazz=null;
        object.getClass().getTypeParameters();
        try {
            Method method = GenericsSandbox.class.getMethod("getClass",List.class);
            Type[] genericParameterTypes=method.getGenericParameterTypes();
            for(Type genericParameterType : genericParameterTypes){
                if(genericParameterType instanceof ParameterizedType){
                    ParameterizedType aType = (ParameterizedType) genericParameterType;
                    Type[] parameterArgTypes = aType.getActualTypeArguments();
                    for(Type parameterArgType : parameterArgTypes){
                        clazz=(Class)parameterArgType;  // Should be only one
                    }
                }
            }
        }
        catch(NoSuchMethodException nsm)
        {
            return null;
        }
        return clazz;
    }
}
