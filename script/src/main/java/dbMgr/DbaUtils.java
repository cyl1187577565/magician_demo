package dbMgr;

import com.fasterxml.jackson.core.type.TypeReference;
import common.constants.CylHttpConstants;
import json.JsonUtil;
import model.ShelfType;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-03 23:16
 **/
public class DbaUtils {

    private static final   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static OkHttpClient client = new OkHttpClient();

    private static String url = "https://dba.corp.bianlifeng.com/sqlexchange/v1/task/query/commit";


    private static String sql_pattern = "select id from shop_layout where shop_code = %s and layout_status = 1 order by version desc, id desc limit 1";



    public static void main(String[] args) throws Exception {
        String sql = "select * from base_shelf_type where id = 1";
        List<ShelfType> shopPos = processSql(ShelfType.class, sql);
        System.out.println(JsonUtil.toJsonString(shopPos));
    }

    public static <T> T processAndGetOne(Class<T> clazz, String sql_pattern, Object... params){
        List<T> ts = processSql(clazz, sql_pattern, params);
        if(ts == null || ts.isEmpty()){
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ts.get(0);
    }

    public  static <T> List<T> processSql(Class<T> clazz, String sql_pattern, Object... params){
        Map<String, Object> requestBody = getRequestBody(sql_pattern,params);
        String response = post(url, JsonUtil.toJsonString(requestBody));
        if (StringUtils.isBlank(response)){
            return Collections.emptyList();
        }
        return getRealData(clazz, response);
    }

    private static <T> List<T> getRealData(Class<T> clazz, String responseStr) {
        Class<String> stringClass = String.class;
        Class<Double> doubleClass = Double.class;
        Class<Integer> integerClass = Integer.class;
        Class<Long> longClass = Long.class;
        Class<Boolean> booleanClass = Boolean.class;
        Class<LocalDateTime> localDateTimeClass = LocalDateTime.class;
        Class<LocalDate> localDateClass = LocalDate.class;
        Class<List> listClass = List.class;


        Map response = (Map) JsonUtil.of(responseStr, new TypeReference<Map<String, Object>>(){});
        Map data = (Map) response.get("data");
        List<T> result = new ArrayList<>();
        try {
            List<Map<String, String>> columns = (List<Map<String, String>>) data.get("column");
            if(CollectionUtils.isEmpty(columns)){
                return Collections.emptyList();
            }

            Map<String, String> fieldMap = new HashMap<>();
            for (Map<String,String> column: columns){
                String key = column.get("column");
                String value = column.get("index");
                fieldMap.put(toSmallCamel(key),value);
            }

            List<Map<String,String>> realData = (List<Map<String, String>>) data.get("data");


            Field[] declaredFields = clazz.getDeclaredFields();
            for (Map<String,String> map : realData){
                T t = clazz.newInstance();
                for (Field field: declaredFields){
                    String key = fieldMap.get(field.getName());
                    if(StringUtils.isBlank(key)){
                        continue;
                    }
                    String value = map.get(key);
                    if ("null".equalsIgnoreCase(value)){
                        continue;
                    }
                    field.setAccessible(true);
                    if (field.getType() == stringClass){
                        field.set(t, value);
                    }else if(field.getType() == integerClass){
                        field.set(t, Integer.parseInt(value));
                    }else if(field.getType() == longClass){
                        field.set(t, Long.parseLong(value));
                    }
                    else if(field.getType() == doubleClass){
                        field.set(t, Double.parseDouble(value));
                    }else if(field.getType() == localDateTimeClass){
                        field.set(t, LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                    else if(field.getType() == booleanClass){
                        field.set(t, Boolean.parseBoolean(value));
                    }else if(field.getType() == listClass){
                        field.set(t, JsonUtil.ofList(value, getParameterizedType(field)));
                    }else if(field.getType() == localDateTimeClass){
                        field.set(t,LocalDateTime.parse(value,formatter));
                    }else if(field.getType() == localDateClass){
                        field.set(t,LocalDate.parse(value,formatter));
                    }
                }
                result.add(t);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static String toSmallCamel(String str){
        String[] s = str.split("_");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String cur: s){
            i++;
            if(i == 1){
                sb.append(cur);
                continue;
            }
            sb.append(captureName(cur));

        }

        return sb.toString();
    }

    public static String captureName(String name) {
        char[] cs=name.toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);

    }




    public static List<Map<String, String>> processSql(String sql_pattern, Object... params) {
        List<Map<String, String>> realData = null;
        try {
            Map<String, Object> requestBody = getRequestBody(sql_pattern,params);
            String response = post(url, JsonUtil.toJsonString(requestBody));
            realData = getRealData(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (realData == null){
            return Collections.emptyList();
        }
        return realData;
    }

    private static List<Map<String,String>>  getRealData(String responseStr){
        Map response = (Map) JsonUtil.of(responseStr, Map.class);
        Map data = (Map) response.get("data");
        List<Map<String,String>> realData = (List<Map<String, String>>) data.get("data");
        return realData;
    }

    /**\
     * 发送请求
     * @param url
     * @param json
     * @return
     * @throws IOException
     */
    private  static String post(String url, String json) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .header("Cookie", CylHttpConstants.COOKIE)
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static Class getParameterizedType(Field field){
        Type genericType = field.getGenericType();
        if(genericType == null){
            return String.class;
        }
        if(genericType instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType) genericType;
            //得到泛型里的class类型对象
            return (Class<?>)pt.getActualTypeArguments()[0];
        }
        return String.class;
    }


    private static Map<String, Object> getRequestBody(String pattern, Object... params){
        if(CylHttpConstants.DBA_PROFILE.equals("prod")){
            return doGetRequestBody4Prod(pattern, params);
        }
        return doGetRequestBody4Gray(pattern,params);
    }

    private static Map<String, Object> doGetRequestBody4Prod(String pattern, Object... params){
        Map<String, Object> map = new HashMap<>();
        map.put("description", "查询工单-cvs_product_display-20201210132351");
        map.put("charset", "utf8mb4");
        map.put("clusterId", 67);
        map.put("databaseName", "cvs_product_display");
        map.put("operateType", 9);
        map.put("type", 2);
        String sql = concatSql(pattern, params);
        map.put("sql", sql);

        return map;
    }

    private static Map<String, Object> doGetRequestBody4Gray(String pattern, Object... params){
        Map<String, Object> map = new HashMap<>();
        map.put("description", "查询工单-cvs_product_display_gray-20201223113808");
        map.put("charset", "utf8mb4");
        map.put("clusterId", 108);
        map.put("databaseName", "cvs_product_display_gray");
        map.put("operateType", 9);
        map.put("type", 2);
        String sql = concatSql(pattern, params);
        map.put("sql", sql);

        return map;
    }

    private static String concatSql(String pattern, Object... params){
        return String.format(pattern, params);
    }

}
