package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    public static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T of(String json, Class<T> clazz){
        if(Strings.isNullOrEmpty(json)){
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> T of(String json, TypeReference<T> reference) {
        if(Strings.isNullOrEmpty(json)){
            return null;
        }

        try {
            return objectMapper.readerFor(reference).readValue(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public static <T> List<T> ofList(String json, Class<T> tClass) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        } else {
            JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(ArrayList.class, ArrayList.class, new Class[]{tClass});
            try {
                return (List)objectMapper.readValue(json, javaType);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public static <K, V> Map<K, V> ofMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (Strings.isNullOrEmpty(json)) {
            return null;
        } else {
            MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
            try {
                return (Map)objectMapper.readValue(json, mapType);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String toJson(Object obj) {
        try {
            return null == obj ? null : objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}