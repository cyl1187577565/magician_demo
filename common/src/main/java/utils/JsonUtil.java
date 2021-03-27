package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.base.Strings;
import model.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson是一个可以轻松的将Java对象转换成json对象和xml文档，同样也可以将json、xml转换成Java对象的框架。非常方便，同时也很高效。
 *
 * 最近在使用时，将前台传递的JSON 串转成Java实体对象时，出现了Unrecognized field, not marked as ignorable 错误。该错误的意思是说，不能够识别的字段没有标示为可忽略。出现该问题的原因就是JSON中包含了目标Java对象没有的属性。
 *
 * 解决方法有如下几种：
 *
 * 格式化输入内容，保证传入的JSON串不包含目标对象的没有的属性。
 *
 * @JsonIgnoreProperties(ignoreUnknown = true) 在目标对象的类级别上加上该注解，并配置ignoreUnknown = true，则Jackson在反序列化的时候，会忽略该目标对象不存在的属性。
 *
 * 全局DeserializationFeature配置
 * objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);配置该objectMapper在反序列化时，忽略目标对象没有的属性。凡是使用该objectMapper反序列化时，都会拥有该特性。
 *
 */
public class JsonUtil {
    public static void main(String[] args) {
        String json = "[{\"name\":\"cyl\",\"age\":5,\"hh\":19}]";
        List<Person> people = JsonUtil.ofList(json, Person.class);
        System.out.println(people);
    }

    public static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    }

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
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, tClass);
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