package com.knubisoft;

import lombok.SneakyThrows;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Main {
    static Map<Class<?>, Supplier<Object>> generator = new LinkedHashMap<>();
    static {
        Random random = new Random();
        generator.put(Integer.class, () -> random.nextInt(100));
        generator.put(Boolean.class, random::nextBoolean);
        generator.put(String.class, () -> UUID.randomUUID().toString());
        generator.put(Long.class, random::nextLong);
        generator.put(Float.class, random::nextFloat);
        generator.put(Double.class, random::nextDouble);
        generator.put(Character.class,() -> random.nextInt(65535));
    }

    public static void main(String[] args) {
        Map<String, List<Integer>> result = new LinkedHashMap<>();
        Integer integer = null;
        String str = null;
        List<List<Integer>> list = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        X x = new X();
        System.out.println(populate(unpackGenericClass(new GenericClass<>(x) {}.getType())));

    }

    @SneakyThrows
    private static Object populate(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type t : types) {
                t.getClass().newInstance();
                  populate(t);
            }
            return true;
        }
        if (isSimpleType(type)) {
            System.out.println(generator.get(type).get());
            return generator.get(type).get();
        } else {
            Field[] fields = Class.forName(((Class<?>) type).getTypeName()).getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(type.getClass(), field);
                populate(field.getGenericType());
            }
        }
        return true;
    }

    private static Type unpackGenericClass(Type type) {
        ParameterizedType params = (ParameterizedType) type;
        return params.getRawType().equals(GenericClass.class) ? params.getActualTypeArguments()[0] : type;
    }

    private static boolean isSimpleType(Object x) {
        return generator.containsKey(x);
    }
}
