package dbMgr;

import com.fasterxml.jackson.core.type.TypeReference;
import json.JsonUtil;
import model.TopicProportion;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

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
 * @since: 2020-12-16 20:23
 **/
public class ShopLayoutAnalysisUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static void main(String[] args) {
        Map<Integer, List<TopicProportion>> integerListMap = queryByShopLayoutId(298395);
        System.out.println(JsonUtil.toJsonString(integerListMap));
    }

    /**
     * 查询指定布局的主题分配比例
     * @param shopLayoutId
     * @return
     */
    public static Map<Integer, List<TopicProportion>> queryByShopLayoutIdAndCreateTime(Integer shopLayoutId, LocalDateTime createTime){
        String sql = "select data from  layout_analysis  where type = 1 and status = 1 and shop_layout_id = %d and create_time < '%s' order by id desc limit 1";
        List<Map<String, String>> maps = DbaUtils.processSql(sql, shopLayoutId, formatter.format(createTime));
        if(CollectionUtils.isEmpty(maps)){
            return Collections.emptyMap();
        }
        String json = maps.get(0).get("0");
        return  buildData(json);

    }

    /**
     * 查询指定布局的主题分配比例
     * @param shopLayoutId
     * @return
     */
    public static Map<Integer, List<TopicProportion>> queryByShopLayoutId(Integer shopLayoutId){
        String sql = "select data from  layout_analysis  where type = 1 and status = 1 and shop_layout_id = %d order by id desc limit 1";
        List<Map<String, String>> maps = DbaUtils.processSql(sql, shopLayoutId);
        if(CollectionUtils.isEmpty(maps)){
            return Collections.emptyMap();
        }

        String json = maps.get(0).get("0");
        return  buildData(json);

    }

    private static Map<Integer, List<TopicProportion>> buildData(String json) {
        if(StringUtils.isBlank(json)){
            return Collections.emptyMap();
        }
        Map<Integer, List<TopicProportion>> result = new HashMap<>();
        Map<Integer, Map<Integer, TopicProportion>> map = JsonUtil.of(json, new TypeReference<Map<Integer, Map<Integer, TopicProportion>>>() {});

        if (MapUtils.isNotEmpty(map)){
            for(Map.Entry<Integer, Map<Integer, TopicProportion>> entry: map.entrySet()){
                Integer layoutShelfId = entry.getKey();
                List<TopicProportion> list = new ArrayList<>();
                Map<Integer, TopicProportion> data = entry.getValue();
                for (Map.Entry<Integer, TopicProportion> innerEntry: data.entrySet()){
                    TopicProportion value = innerEntry.getValue();
                    int shelfTopicId = innerEntry.getKey();
                    value.setShelfTopicId(shelfTopicId);
                    list.add(value);
                }
                result.put(layoutShelfId, list);
            }
        }

        return result;
    }


    /**
     * 查询指定布局的主题分配比例
     * @param shopLayoutId
     * @return
     */
    public static List<Map<Integer, List<TopicProportion>>> queryByShopLayoutIdLimit(Integer shopLayoutId, Integer limit){
        String sql = "select data from  layout_analysis  where type = 1 and status = 1 and shop_layout_id = %d order by id desc limit " + limit;
        List<Map<String, String>> maps = DbaUtils.processSql(sql, shopLayoutId);


        List<Map<Integer, List<TopicProportion>>> result = new ArrayList<>();
        for(Map<String, String>  map: maps){
            Map<Integer, List<TopicProportion>> tempResult = new HashMap<>();
            String json = map.get("0");
            Map<Integer, Map<Integer, TopicProportion>> temp = JsonUtil.of(json, new TypeReference<Map<Integer, Map<Integer, TopicProportion>>>() {});

            if (temp != null || !temp.isEmpty()){
                for(Map.Entry<Integer, Map<Integer, TopicProportion>> entry: temp.entrySet()){
                    Integer layoutShelfId = entry.getKey();
                    List<TopicProportion> list = new ArrayList<>();
                    Map<Integer, TopicProportion> data = entry.getValue();
                    for (Map.Entry<Integer, TopicProportion> innerEntry: data.entrySet()){
                        TopicProportion value = innerEntry.getValue();
                        int shelfTopicId = innerEntry.getKey();
                        value.setShelfTopicId(shelfTopicId);
                        list.add(value);
                    }
                    tempResult.put(layoutShelfId, list);
                }
            }

            result.add(tempResult);

        }


        return result;
    }

    /**
     * 查询指定布局的主题分配比例
     * @param shopLayoutId
     * @return
     */
    public static Map<Integer, List<TopicProportion>> queryByShoqueryByShopLayoutIdpLayoutId(Integer shopLayoutId){
        String sql = "select data from  layout_analysis  where type = 2 and status = 1 and shop_layout_id = %d order by id desc limit 1";
        List<Map<String, String>> maps = DbaUtils.processSql(sql, shopLayoutId);

        Map<Integer, List<TopicProportion>> result = new HashMap<>();


        String json = maps.get(0).get("0");
        Map<Integer, Map<Integer, TopicProportion>> map = JsonUtil.of(json, new TypeReference<Map<Integer, Map<Integer, TopicProportion>>>() {});

        if (map != null || !map.isEmpty()){
            for(Map.Entry<Integer, Map<Integer, TopicProportion>> entry: map.entrySet()){
                Integer layoutShelfId = entry.getKey();
                List<TopicProportion> list = new ArrayList<>();
                Map<Integer, TopicProportion> data = entry.getValue();
                for (Map.Entry<Integer, TopicProportion> innerEntry: data.entrySet()){
                    TopicProportion value = innerEntry.getValue();
                    int shelfTopicId = innerEntry.getKey();
                    value.setShelfTopicId(shelfTopicId);
                    list.add(value);
                }
                result.put(layoutShelfId, list);
            }
        }

        return result;
    }
}
