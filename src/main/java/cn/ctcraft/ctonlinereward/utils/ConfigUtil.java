package cn.ctcraft.ctonlinereward.utils;

import cn.ctcraft.ctonlinereward.pojo.annotation.ConfigMapper;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ConfigUtil {
    public static <T> List<T> getObjectList(ConfigurationSection configurationSection, String key, Class<T> c) throws Exception {
        List<?> configList = configurationSection.getList(key);

        List<T> list = new ArrayList<>();
        for (Object o : configList) {
            Object object = c.getConstructor().newInstance();

            LinkedHashMap<String,Object> linkedHashMap = (LinkedHashMap<String, Object>) o;
            Field[] fields = c.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];

                boolean annotationPresent = field.isAnnotationPresent(ConfigMapper.class);
                if (annotationPresent){
                    ConfigMapper annotation = field.getAnnotation(ConfigMapper.class);
                    String value = annotation.value();

                    String name = field.getName();
                    String substring = name.substring(0, 1).toUpperCase();
                    String setMethodName = "set"+substring+name.substring(1);

                    Method setMethod = c.getMethod(setMethodName, field.getType());
                    boolean b = linkedHashMap.containsKey(value);
                    if (b){
                        Object s = linkedHashMap.get(value);
                        setMethod.invoke(object, s);
                    }
                }
            }
            list.add((T) object);
        }
        return list;
    }
}
