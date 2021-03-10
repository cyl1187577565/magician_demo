import com.google.common.collect.Lists;
import common.utils.dbMgr.BaseShelfTypeUtils;
import common.utils.dbMgr.BaseTopicUtils;
import common.utils.dbMgr.LayoutShelfUtils;
import common.utils.dbMgr.ShopLayoutAnalysisUtils;
import common.utils.dbMgr.ShopLayoutUtils;
import common.utils.dbMgr.ShopSnapUtils;
import common.utils.dbMgr.ShopUtils;
import io.fileIo.FileUtil;
import model.BaseTopic;
import model.LayoutShelf;
import model.ShelfTopicProportionCount;
import model.ShelfType;
import model.Shop;
import model.ShopSnap;
import model.TopicProportion;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 导出指定门店的
 * @author: yulong.cao
 * @since: 2020-12-17 18:56
 **/
public class ExportShelfTopicProportion {
    public static Map<Integer, ShelfType> shelfTypeMap;

    public static Map<Integer, BaseTopic> topicMap;

    static {
        List<ShelfType> shelfTypes = BaseShelfTypeUtils.queryAll();
        shelfTypeMap = shelfTypes.stream().collect(Collectors.toMap(ShelfType::getId, Function.identity(), (v1, v2) -> v1));

        List<BaseTopic> baseTopics = BaseTopicUtils.queryAll();
        topicMap = baseTopics.stream().collect(Collectors.toMap(BaseTopic::getId, Function.identity(), (v1, v2) -> v1));
    }

    public static void main(String[] args) {
        ArrayList<String> shopCodes = Lists.newArrayList("123000116","110000057","111000038","110000071","107000016","110000069","123000328","123000121","110000087","100000167","123000361","100000379","123000263","100001099","110000119","123000167","123001022","123000163","107000037","123000250","118000025","123000331","110000006","100000357","100000573","110000111","100076007","123001077","123000369","109000039","110000059","100000335","100000522","123000336","100000516","111000013","111000058","100000623","100000610","100000681","100000311","123000378","123000180","100000593","123000267","123000360","109000025","109000060","110000161","123000238","110000023","123000101","110000051","110000053","110000092","123000102","100001151","110000011","123000110","397000010","612000015","110000165","110001003","100000258","100017003","110000055","110000113","110001019","100000675","123000171","110000088","101000056","110001029","101000181","101000091","101001009","100000620","232000003","109000012","109000007","100000210","100001079","123001060","110000139","110000102","123000109","123000256","101000186","110001061","123000370","123000091","100000587","101001013","101000189","110001027","100071008","110000060","110001011","100079021","100000616","123001026","110000061","100001112","100000228","100000056","100000507","123000011","101001028","100000518"
        );

        writeHead();
        shopCodes.parallelStream()
                .forEach(it -> {
                    exportShelfTopicProportion(it.toString());
                });


//        List<ShelfTopicProportionCount> result = exportShelfTopicProportion("100000558");
//        writeToFile(result);
//
    }

    /**
     * 导出前后两班
     *
     * @param shopCode
     * @return
     */
    public static List<ShelfTopicProportionCount> exportShelfTopicProportion2(String shopCode) {
        Shop shop = ShopUtils.queryByCode(shopCode);
        Integer latestShopLayoutId = ShopLayoutUtils.queryLatestPublishedId(shopCode);
        return buildData2(shop, latestShopLayoutId);
    }

    /**
     * 导出excel
     *
     * @param shopCode
     */
    public static void exportShelfTopicProportion(String shopCode) {
        System.out.println("执行门店code：" + shopCode);

        List<ShelfTopicProportionCount> list = new ArrayList<>();
        //基准数据
        Shop shop = ShopUtils.queryByCode(shopCode);
        ShopSnap shopSnap = ShopSnapUtils.queryLatestShopLayoutIdByShopId(shop.getId());
        List<ShelfTopicProportionCount> list1 = buildData(shop, shopSnap.getLayoutId(), "基准分配结果", shopSnap.getCreateTime());
        list.addAll(list1);


        //最新快照数据
        Integer latestShopLayoutId = ShopLayoutUtils.queryLatestPublishedId(shopCode);
        List<ShelfTopicProportionCount> list2 = buildData(shop, latestShopLayoutId, "当前分配结果", null);
        list.addAll(list2);
        writeToFile(list);
    }

    private static void writeHead(){
        StringBuilder sb = new StringBuilder();
        sb.append("门店Code").append(";")
                .append("门店名称").append(";")
                .append("类型").append(";")
                .append("一级货架类型").append(";")
                .append("二级货架类型").append(";")
                .append("三级货架类型").append(";")
                .append("区域主题").append(";")
                .append("货架主题").append(";")
                .append("货架组数").append("\n");

        FileUtil.appendWriteToFile(sb.toString(), "/Users/bianlifeng/Documents/work/data/shelfTopicProportion.csv");

    }



    private static void writeToFile(List<ShelfTopicProportionCount> list) {

        StringBuilder sb = new StringBuilder();

        for (ShelfTopicProportionCount count : list) {
            sb.append(count.getShopCode()).append(";")
                    .append(count.getShopName()).append(";")
                    .append(count.getType()).append(";")
                    .append(count.getOneShelfTypeName()).append(";")
                    .append(count.getTwoShelfTypeName()).append(";")
                    .append(count.getThreeShelfTypeName()).append(";")
                    .append(count.getSectionTopicName()).append(";")
                    .append(count.getShelfTopicName()).append(";")
                    .append(count.getShelfNum() / 100).append("\n");
        }

        FileUtil.appendWriteToFile(sb.toString(), "/Users/bianlifeng/Documents/work/data/shelfTopicProportion.csv");
    }

    /**
     * 构建数据
     *
     * @param shop
     * @param shopLayoutId
     * @param type
     * @return
     */
    public static List<ShelfTopicProportionCount> buildData(Shop shop, Integer shopLayoutId, String type, LocalDateTime localDateTime) {
        Map<Integer, List<TopicProportion>> baseData = null;
        if(localDateTime != null){
            baseData = ShopLayoutAnalysisUtils.queryByShopLayoutIdAndCreateTime(shopLayoutId,localDateTime);
        }else{
            baseData = ShopLayoutAnalysisUtils.queryByShopLayoutId(shopLayoutId);
        }

        return doBuildData(shop, baseData, type);

    }


    /**
     * 构建数据
     *
     * @param shop
     * @param shopLayoutId
     * @return
     */
    public static List<ShelfTopicProportionCount> buildData2(Shop shop, Integer shopLayoutId) {

        List<Map<Integer, List<TopicProportion>>> maps = ShopLayoutAnalysisUtils.queryByShopLayoutIdLimit(shopLayoutId, 2);
        if(CollectionUtils.isEmpty(maps)){
            return Collections.emptyList();
        }


        Map<Integer, List<TopicProportion>> latest = maps.get(0);


        List<ShelfTopicProportionCount> result = new ArrayList<>();
        if(MapUtils.isNotEmpty(latest)){
            List<ShelfTopicProportionCount> list = doBuildData(shop, latest, "最新分配结果");
            result.addAll(list);
        }

        if (maps.size() <= 1){
            return result;
        }

        Map<Integer, List<TopicProportion>> baseData = maps.get(1);
        if(MapUtils.isNotEmpty(baseData)){
            List<ShelfTopicProportionCount> base = doBuildData(shop, baseData, "基准分配结果");
            result.addAll(base);

        }

        return result;

    }

    private static List<ShelfTopicProportionCount> doBuildData(Shop shop, Map<Integer, List<TopicProportion>> baseData, String type) {
        List<ShelfTopicProportionCount> result = new ArrayList<>();
        for (Map.Entry<Integer, List<TopicProportion>> entry : baseData.entrySet()) {
            Integer layoutShelfId = entry.getKey();
            LayoutShelf layoutShelf = LayoutShelfUtils.queryById(layoutShelfId);
            Integer sizeTypeId = layoutShelf.getShelfSizeType();
            ShelfType sizeType = shelfTypeMap.get(sizeTypeId);
            ShelfType shelfType = shelfTypeMap.get(sizeType.getParentId());
            ShelfType oneShelfType = shelfTypeMap.get(shelfType.getParentId());


            for (TopicProportion topicProportion : entry.getValue()) {
                Integer shelfTopicId = topicProportion.getShelfTopicId();
                BaseTopic shelfTopic = topicMap.get(shelfTopicId);
                BaseTopic sectionTopic = topicMap.get(shelfTopic.getParentId());

                ShelfTopicProportionCount data = new ShelfTopicProportionCount();
                data.setShopCode(shop.getCode());
                data.setShopName(shop.getName());
                data.setType(type);
                data.setOneShelfTypeName(oneShelfType.getName());
                data.setTwoShelfTypeName(shelfType.getName());
                data.setThreeShelfTypeName(sizeType.getName());
                data.setSectionTopicName(sectionTopic.getName());
                data.setShelfTopicName(shelfTopic.getName());
                data.setShelfNum(topicProportion.getProportion());
                result.add(data);
            }
        }

        if (result.isEmpty()) {
            return Collections.emptyList();
        }

        List<ShelfTopicProportionCount> realResult = new ArrayList<>();
        Map<String, List<ShelfTopicProportionCount>> map = result.stream()
                .collect(Collectors.groupingBy(ShelfTopicProportionCount::key));

        for (List<ShelfTopicProportionCount> list : map.values()) {
            double sum = list.stream()
                    .mapToDouble(ShelfTopicProportionCount::getShelfNum)
                    .sum();
            ShelfTopicProportionCount shelfTopicProportionCount = list.get(0);
            shelfTopicProportionCount.setShelfNum(sum);
            realResult.add(shelfTopicProportionCount);
        }

        return realResult;
    }


}
