import com.google.common.collect.Lists;
import common.enums.ShelfMarkTypeEnum;
import common.utils.dbMgr.BaseShelfTypeUtils;
import common.utils.dbMgr.DbaUtils;
import common.utils.dbMgr.LayoutShelfUtils;
import common.utils.dbMgr.LevelUtils;
import common.utils.dbMgr.ShopLayoutUtils;
import common.utils.dbMgr.ShopUtils;
import io.fileIo.FileUtil;
import json.JsonUtil;
import model.LayoutShelf;
import model.Level;
import model.ShelfType;
import model.Shop;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 统计现 有大调算法比较依赖货架标记，需要对门店布局图标记情况做核查，将可能有问题的门店提前暴露出来；
 * https://wiki.corp.bianlifeng.com/pages/viewpage.action?pageId=567249810
 * @author: yulong.cao
 * @since: 2021-01-14 16:27
 **/
public class CheckWrongShelfMarkV2 {

    private static final String TARGET = "/Users/bianlifeng/Documents/work/data/countWrongShelfMark.csv";
    private static final String TARGET1 = "/Users/bianlifeng/Documents/work/data/addShelfMark.sql";
    /**
     * 常温货架 二级货架类型
     */
    private static final Integer ROOM_TEMPERATURE = 1;

    public static Map<Integer, ShelfType> shelfTypeMap;

    static {
        List<ShelfType> shelfTypes = BaseShelfTypeUtils.queryAll();
        shelfTypeMap = shelfTypes.stream().collect(Collectors.toMap(ShelfType::getId, Function.identity(), (v1, v2) -> v1));
    }

    public static void main(String[] args) {


        StringBuilder sb = new StringBuilder();
        sb.append("门店code")
                .append(";")
                .append("门店名称")
                .append(";")
                .append("货架名称")
                .append(";")
                .append("货架标记")
                .append("\n");
        FileUtil.appendWriteToFile(sb.toString(), TARGET);
        List<String> shopCodes = Lists.newArrayList("101000115","100000330","110000058","232000003","100000395","100000288","110000002","110000056","100017005","123000091","101000189","100000026","110001011","110001029","110000088","100000609","123000020","100000021","110000139","100001027","111000013","110001027","232000007","123001060","123000109","123000263","123000180","123000195","123001016","109000015","123000353","123000298","109000009","123000167","110000023","123001027","100000212","110000162","123000110","110001003","110000036","100017003","123000138","397000005","110000009","123001039","123000193","123000029","118000025","110000070","100010001","123000102","100078006","110000006","110000026","110001052","110000039","123001013","123000121","109000026","123000250","110000087","123000360","123001022","109000060","110000069","110000071","107000018","110000113","123001077","123000127","111000038","123000361","110000055","123000336","123000006","123000071","100000056","123000095","121000005"

        );
        shopCodes.stream()
                .forEach(it -> {
                    Shop shop = ShopUtils.queryByCode(it);
                    countShop(shop);
                });

//        List<Shop> shops = ShopUtils.queryAllShops();
//        shops.parallelStream()
//                .forEach(shop -> {
//                    countShop(shop);
//                });

    }

    /**
     * @param shop
     */
    private static String countShop(Shop shop) {
        System.out.println("检测门店：" + shop.getCode());

        //获取当前门店最新的发布态的布局图id
        Integer publishedId = ShopLayoutUtils.queryLatestPublishedId(shop.getCode());
        Integer craftId = ShopLayoutUtils.queryCraftId(shop.getCode());

        //获取当前布局图上的所有货架
        List<LayoutShelf> layoutShelves = LayoutShelfUtils.listByShopLayoutId(publishedId);
        List<LayoutShelf> layoutShelves2 = LayoutShelfUtils.listByShopLayoutId(craftId);

        layoutShelves.addAll(layoutShelves2);

        StringBuilder sqlSb = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (LayoutShelf layoutShelf : layoutShelves) {
//            Integer shelfSizeTypeId = layoutShelf.getShelfSizeType();
//            ShelfType shelfSizeType = shelfTypeMap.get(shelfSizeTypeId);
//            //只处理常温货架
//            if (shelfSizeType.getParentId() != ROOM_TEMPERATURE) {
//                continue;
//            }

//            List<TopicProportion> curTopicProportion = topicProportions.get(layoutShelf.getId());


            //缺少pb标记
//            if (shouldMarkPb(layoutShelf) && !hasMarkedPb(layoutShelf)) {
//                sbAppend(sb, layoutShelf, shop);
//                sqlAppend(ShelfMarkTypeEnum.ALL_GROUP_PB_SKU_SHELF_MARK.getCode(), layoutShelf, sqlSb);
//            }


            // 缺少面包标记
            if (shouldMarkBread(layoutShelf) && !hasMarkedBread(layoutShelf)) {
                sbAppend(sb,layoutShelf, shop);
                sqlAppend(ShelfMarkTypeEnum.BREAD_SHELF_MARK.getCode(), layoutShelf, sqlSb);
            }

            //缺少水果标记
//            if(shouldMarkFruit(layoutShelf) && !hasMarkedFruit(layoutShelf)){
//                sbAppend(sb, layoutShelf, shop);
//                sqlAppend(ShelfMarkTypeEnum.FRUIT_SHELF_MARK.getCode(), layoutShelf, sqlSb);
//                continue;
//            }

            //缺少陈列不变
//            if(shouldMarkDisplayUnchange(layoutShelf)){
//                sbAppend(sb, layoutShelf, shop);
//                sqlAppend(ShelfMarkTypeEnum.NO_NEED_AUTO_DISPLAY.getCode(), layoutShelf, sqlSb);
//                continue;
//            }

        }

        FileUtil.appendWriteToFile(sb.toString(), TARGET);
        FileUtil.appendWriteToFile(sqlSb.toString(), TARGET1);
        return sb.toString();

    }

    /**
     * 需要标记为陈列不变
     * 当货架层板用途都为“自行陈列”，且没有标记“蜂特卖、五折货架、陈列不变、不陈列商品”时
     * @param layoutShelf
     * @return
     */
    private static boolean shouldMarkDisplayUnchange(LayoutShelf layoutShelf) {
        List<Level> levels = LevelUtils.listByShelfId(layoutShelf.getShelfId());

        boolean isAutoDisplay = true;
        for(Level level: levels){
            Integer useId = level.getUseId();
            if(13 != useId){
                isAutoDisplay =  false;
            }
        }

        //层板用途不是自行陈列 则不需要标记为陈列不变
        if(!isAutoDisplay){
            return false;
        }

        List<Integer> shelfMarkList = layoutShelf.getShelfMarkList();
        if(CollectionUtils.isEmpty(shelfMarkList)){
            return true;
        }

        List<Integer> markList = Lists.newArrayList(17, 2, 8, 9);
        for(Integer mark: shelfMarkList){
            markList.contains(mark);
            return false;
        }

        return true;

    }

    /**
     * sql 追加
     * @param code
     * @param layoutShelf
     * @param sqlSb
     */
    private static void sqlAppend(int code, LayoutShelf layoutShelf, StringBuilder sqlSb) {
        String sql = "update layout_shelf set shelf_mark_list = '%s', shelf_mark_type = %d where id = %d;";
        String sql2 = "update shelf set mark_type = %d where id = %d";

        List<Integer> shelfMarkList = layoutShelf.getShelfMarkList();
        if (CollectionUtils.isEmpty(shelfMarkList)) {
            shelfMarkList = Lists.newArrayList(code);
        } else {
            shelfMarkList.add(code);
        }

        String format1 = String.format(sql, JsonUtil.toJsonString(shelfMarkList), code, layoutShelf.getId());
        String format2 = String.format(sql2, code, layoutShelf.getShelfId());

        sqlSb.append(format1).append("\n").append(format2).append("\n");
    }


    /**
     *是否已经标记为水果
     * @param layoutShelf
     * @return
     */
    private static boolean hasMarkedFruit(LayoutShelf layoutShelf) {
        List<Integer> shelfMarkList = layoutShelf.getShelfMarkList();
        if (CollectionUtils.isEmpty(shelfMarkList)) {
            return false;
        }

        return shelfMarkList.contains(ShelfMarkTypeEnum.FRUIT_SHELF_MARK.getCode());
    }

    /**
     * 货架类型不是一级“风幕柜”，当货架层板用途都为“生鲜”，需要标记为“水果”
     * @param layoutShelf
     * @return
     */
    private static boolean shouldMarkFruit(LayoutShelf layoutShelf) {
        if(layoutShelf == null){
            return false;
        }

        //货架用类型 是否是风幕柜
        Integer shelfSizeType = layoutShelf.getShelfSizeType();
        ShelfType sizeType = shelfTypeMap.get(shelfSizeType);
        if(sizeType == null){
            return false;
        }
        ShelfType shelfType = shelfTypeMap.get(sizeType.getParentId());
        if(shelfType == null){
            return false;
        }
        ShelfType skuShelfType = shelfTypeMap.get(shelfType.getParentId());
        if(skuShelfType == null){
            return false;
        }
        if(skuShelfType.getId().equals(22)){
            return false;
        }

        List<Level> levels = LevelUtils.listByShelfId(layoutShelf.getShelfId());
        boolean isAllFresh = true;
        for(Level level: levels){
            Integer useId = level.getUseId();
            if(4 != useId){
                isAllFresh = false;
            }
        }

        return isAllFresh;
    }

    /**
     * 追加
     * @param sb
     * @param layoutShelf
     */
    private static void sbAppend(StringBuilder sb, LayoutShelf layoutShelf, Shop shop) {
        sb.append(layoutShelf.getShopCode())
                .append(";")
                .append(shop.getName())
                .append(";")
                .append(layoutShelf.getShelfName())
                .append(";")
                .append(JsonUtil.toJsonString(layoutShelf.getShelfMarkList()))
                .append("\n");
    }

    private static boolean hasMarkedBread(LayoutShelf layoutShelf) {
        List<Integer> shelfMarkList = layoutShelf.getShelfMarkList();
        if (CollectionUtils.isEmpty(shelfMarkList)) {
            return false;
        }

        return shelfMarkList.contains(ShelfMarkTypeEnum.BREAD_SHELF_MARK.getCode());
    }

    /**
     * 应该标记为面包货架
     *
     * @return
     */
    private static boolean shouldMarkBread(LayoutShelf layoutShelf) {
        if(layoutShelf == null){
            return false;
        }
        List<Level> levels = LevelUtils.listByShelfId(layoutShelf.getShelfId());
        for(Level level: levels){
            Integer useId = level.getUseId();
            if(3 == useId){
                return true;
            }
        }

        return false;

    }

    /**
     * @param layoutShelf
     * @return
     */
    private static boolean hasMarkedPb(LayoutShelf layoutShelf) {
        List<Integer> shelfMarkList = layoutShelf.getShelfMarkList();
        if (CollectionUtils.isEmpty(shelfMarkList)) {
            return false;
        }

        return shelfMarkList.contains(ShelfMarkTypeEnum.ALL_GROUP_PB_SKU_SHELF_MARK.getCode());
    }

    /**
     * 是否应该被标记为pb
     *
     * @param layoutShelf
     * @return
     */
    private static boolean shouldMarkPb(LayoutShelf layoutShelf) {

        Integer shelfId = layoutShelf.getShelfId();
        String sql = "select id from inheritance_relation_shelf where child_shelf_id = %d";
        List<Map<String, String>> maps = DbaUtils.processSql(sql, shelfId);
        if(CollectionUtils.isEmpty(maps)){
            return false;
        }


        String shelfName = layoutShelf.getShelfName();
        if (shelfName.contains("Pb")) {
            return true;
        }

        if (shelfName.contains("PB")) {
            return true;
        }

        if (shelfName.contains("pB")) {
            return true;
        }

        return false;
    }
}

