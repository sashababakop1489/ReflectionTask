package com.knubisoft;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
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
        generator.put(Integer.class, () -> 1);
        generator.put(Boolean.class, () -> true);
        generator.put(String.class, () -> "Hello");
    }

    public static void main(String[] args) {
        Map<String, List<Integer>> result = new LinkedHashMap<>();
        Integer integer = null;
        String str = null;
        List<List<Integer>> list = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        X x = new X();
        System.out.println(populate(unpackGenericClass(new GenericClass<>(map) {}.getType())));

    }

    @SneakyThrows
    private static Object populate(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type t : types) {
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
