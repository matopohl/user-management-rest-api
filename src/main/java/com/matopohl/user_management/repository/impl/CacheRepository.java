package com.matopohl.user_management.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import redis.clients.jedis.Jedis;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class CacheRepository<S> {

    private final Jedis jedis;

    public S save(S entity) {
        String id = Objects.requireNonNull(runGetter(Arrays.stream(entity.getClass().getDeclaredFields()).filter(f -> f.getAnnotation(Transient.class) == null).filter(f -> f.getAnnotation(Id.class) != null).findFirst().orElseThrow(), entity)).toString();
        Optional<Field> ttl = Arrays.stream(entity.getClass().getDeclaredFields()).filter(f -> f.getAnnotation(Transient.class) == null).filter(f -> f.getAnnotation(TimeToLive.class) != null).findFirst();
        String key = getKeyName(id);

        Map<String, String> map = Arrays.stream(entity.getClass().getDeclaredFields()).filter(f -> f.getAnnotation(Transient.class) == null).collect(Collectors.toMap(
                Field::getName,
                f -> {
                    Object o = runGetter(f, entity);
                    return o == null ? "null" : o.toString();
                }));

        jedis.hmset(key, map);

        if(ttl.isPresent()) {
            Object ttlO = runGetter(ttl.get(), entity);
            if (ttlO != null) {
                jedis.expire(key, (Long) ttlO);
            }
        }

        return entity;
    }

    @SuppressWarnings("all")
    public boolean exists(String id) {
        return jedis.exists(getKeyName(id));
    }

    public S findById(String id) {
        if(!exists(id)) {
            return null;
        }

        List<Field> fields = getFields();

        List<String> result = jedis.hmget(getKeyName(id), fields.stream().map(Field::getName).toArray(String[]::new));

        S o;

        try {
            o = getGenericClass().getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < fields.size(); i++) {
            runSetter(fields.get(i), o, result.get(i));
        }

        return o;
    }

    public Long getRemainingTTL(String id) {
        return jedis.ttl(getKeyName(id));
    }

    public Long delete(String id) {
        return jedis.del(getKeyName(id));
    }

    private List<Field> getFields() {
        return Arrays.stream(getGenericClass().getDeclaredFields()).filter(f -> f.getAnnotation(Transient.class) == null).toList();
    }

    private String getKeyName(String id) {
        return getGenericClass().getDeclaredAnnotation(RedisHash.class).value() + ": " + id;
    }

    private Object runGetter(Field field, Object o) {
        for (Method method : o.getClass().getDeclaredMethods()) {
            if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3))) {
                if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
                    try {
                        return method.invoke(o);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return null;
    }

    private void runSetter(Field field, Object o, String param) {
        for (Method method : o.getClass().getDeclaredMethods()) {
            if ((method.getName().startsWith("set")) && (method.getName().length() == (field.getName().length() + 3))) {
                if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
                    try {
                        String setterParam = Arrays.stream(method.getParameters()).findFirst().orElseThrow().getParameterizedType().getTypeName();

                        if(setterParam.equals(String.class.getTypeName())) {
                            method.invoke(o, param);
                        }
                        else if(setterParam.equals(UUID.class.getTypeName())) {
                            method.invoke(o, UUID.fromString(param));
                        }
                        else if(setterParam.equals(Long.class.getTypeName())) {
                            method.invoke(o, Long.parseLong(param));
                        }
                        else if(setterParam.equals(Integer.class.getTypeName())) {
                            method.invoke(o, Integer.parseInt(param));
                        }
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("all")
    private Class<S> getGenericClass() {
        String generic = ((Class<S>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getTypeName();

        try {
            return (Class<S>) Class.forName(generic.replace("class ", ""));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
