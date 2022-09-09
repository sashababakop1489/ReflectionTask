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
        if (type instanceof ParameterizedType parameterizedType) {
            Type incomeRawType = parameterizedType.getRawType();

            if ((List.class.isAssignableFrom((Class<?>) incomeRawType))){
                Type[] types = parameterizedType.getActualTypeArguments();
                List<Object> resultList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    resultList.add(populate(types[0]));
                }
                return resultList;
            }

            if (Map.class.isAssignableFrom((Class<?>) incomeRawType)) {
                Map<Object, Object> resultMap = new LinkedHashMap();
                Type[] nestedMapTypes = parameterizedType.getActualTypeArguments();
                for (int i = 0; i < 5; i++) {
                    resultMap.put(populate(nestedMapTypes[0]), populate(nestedMapTypes[1]));
                }
                return resultMap;
            }

            if (Set.class.isAssignableFrom((Class<?>) incomeRawType)){
                Type[] types = parameterizedType.getActualTypeArguments();
                Set<Object> resultSet = new HashSet<>();
                for (int i = 0; i < 5; i++) {
                    resultSet.add(populate(types[0]));
                }
                return resultSet;
            }
            if (Queue.class.isAssignableFrom((Class<?>) incomeRawType)){
                Type[] types = parameterizedType.getActualTypeArguments();
                Queue<Object> resultQueue = new PriorityQueue<>();
                for (int i = 0; i < 5; i++) {
                    resultQueue.add(populate(types[0]));
                }
                return resultQueue;
            }
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (Type t : types) {
                  populate(t);
            }
            return true;
        }

        if (isSimpleType(type)) {
            return generator.get(type).get();
        }
        else {
            Class<?> cls = Class.forName(((Class<?>) type).getTypeName());
            Field[] fields = cls.getDeclaredFields();
            Object instance = cls.getDeclaredConstructor().newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(instance, populate(field.getGenericType()));
            }
            return instance.toString();
        }
    }

    private static Type unpackGenericClass(Type type) {
        ParameterizedType params = (ParameterizedType) type;
        return params.getRawType().equals(GenericClass.class) ? params.getActualTypeArguments()[0] : type;
    }

    private static boolean isSimpleType(Object x) {
        return generator.containsKey(x);
    }
}
