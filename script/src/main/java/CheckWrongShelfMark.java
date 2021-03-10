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
public class CheckWrongShelfMark {

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
        List<Integer> shopCodes = Lists.newArrayList(100000208,101000231,232000001,101000115,100000522,100022005,100000095,110000119,110000111,100000330,101000091,100017002,100001151,110000058,232000003,100033002,118000006,100000395,100000686,100000288,100022001,101000181,100000351,101000159,101000232,110000002,110000056,110000015,100017005,101001028,109000073,110000156,100001079,123000091,101000189,101001009,100001086,100000026,100000335,101000229,110001011,110001029,100000357,123000238,100000507,111000058,188001005,100000697,101000050,110000088,100000587,100000609,123000370,176000011,100001077,123001057,123000020,110000032,100076007,100000021,100001112,110000139,100001071,123000075,100001027,111000013,110000059,100000620,109000002,109000039,123000073,101000056,110000161,123000191,110001027,100000610,232000007,101001013,123001060,123000109,123000328,110000053,123001026,123000263,100000311,123000085,110000080,100075007,110001053,109000068,123001035,100001096,123000180,100000270,397000010,612000011,109000025,123000195,612000013,110000012,101000225,110001025,123001016,110001019,176000007,100000661,176000003,110000011,109000015,110000138,110000061,110000081,110000158,110001061,110000102,123000150,110000153,100000667,100000290,123000353,123000171,123000011,100070001,110000062,110001005,612000015,100000516,110000092,110000097,100000379,123000298,109000009,123000167,110000023,115000012,123001027,100000196,101000075,100000212,123000256,100001065,100000518,110000162,100071008,107000002,123000110,110001003,110000057,110000036,100017003,123000163,100000623,123000138,100002002,123000378,397000005,110000009,123001039,110000086,123000193,100000228,100073007,100000088,123000029,100000675,110000021,107000037,123000090,101000186,100075008,118000025,123000267,110000070,100000593,109000061,100010001,123000102,110000051,100078006,110000006,110000165,110000026,123000116,110001052,110000039,123001013,123000121,100079023,100000333,109000026,110001063,101000079,123000086,109000012,123000250,100001069,123000331,123000027,110000087,123000026,123000360,123000101,100000375,123001022,123001003,109000060,110001002,123000053,232000008,100000630,109000007,110000050,100000681,123000112,101000061,110000069,109000003,100000167,100000027,107000016,100000616,110000060,110000105,107000007,100001110,110000071,107000018,110000113,188001011,123001077,123000369,123000127,100000319,111000038,100000258,100001129,110000155,100000325,100001099,123000361,100001032,110000150,100000670,100000573,110000055,110000077,100001028,123000336,612000008,123000006,100000668,123000071,188001009,100000210,100000056,115000020,123000095,101000203,100079021,100000236,121000005,123000355
        );
        shopCodes.stream()
                .forEach(it -> {
                    Shop shop = ShopUtils.queryByCode(it.toString());
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
        Integer craftId = ShopLayoutUtils.queryCraftId(shop.getCode());
        if(craftId == null){
            return null;
        }

        //获取当前布局图上的所有货架
        List<LayoutShelf> layoutShelves = LayoutShelfUtils.listByShopLayoutId(craftId);

        StringBuilder sqlSb = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (LayoutShelf layoutShelf : layoutShelves) {
            Integer shelfSizeTypeId = layoutShelf.getShelfSizeType();
            ShelfType shelfSizeType = shelfTypeMap.get(shelfSizeTypeId);
            //只处理常温货架
            if (shelfSizeType.getParentId() != ROOM_TEMPERATURE) {
                continue;
            }

//            List<TopicProportion> curTopicProportion = topicProportions.get(layoutShelf.getId());

            String sql = "update layout_shelf set shelf_mark_list = '%s', shelf_mark_type = %d where id = %d;";

            //缺少pb标记
            if (shouldMarkPb(layoutShelf) && !hasMarkedPb(layoutShelf)) {
                sb.append(layoutShelf.getShopCode())
                        .append(";")
                        .append(shop.getName())
                        .append(";")
                        .append(layoutShelf.getShelfName())
                        .append(";")
                        .append(JsonUtil.toJsonString(layoutShelf.getShelfMarkList()))
                        .append("\n");

                List<Integer> shelfMarkList = layoutShelf.getShelfMarkList();
                if (CollectionUtils.isEmpty(shelfMarkList)){
                    shelfMarkList = Lists.newArrayList(ShelfMarkTypeEnum.ALL_GROUP_PB_SKU_SHELF_MARK.getCode());
                }else{
                    shelfMarkList.add(ShelfMarkTypeEnum.ALL_GROUP_PB_SKU_SHELF_MARK.getCode());
                }

                String format1 = String.format(sql, JsonUtil.toJsonString(shelfMarkList), ShelfMarkTypeEnum.ALL_GROUP_PB_SKU_SHELF_MARK.getCode(), layoutShelf.getId());


                sqlSb.append(format1).append("\n");
                continue;
            }


            if (shouldMarkBread(layoutShelf) && !hasMarkedBread(layoutShelf)) {
                sb.append(layoutShelf.getShopCode())
                        .append(";")
                        .append(shop.getName())
                        .append(";")
                        .append(layoutShelf.getShelfName())
                        .append(";")
                        .append(JsonUtil.toJsonString(layoutShelf.getShelfMarkList()))
                        .append("\n");


                List<Integer> shelfMarkList = layoutShelf.getShelfMarkList();
                if (CollectionUtils.isEmpty(shelfMarkList)){
                    shelfMarkList = Lists.newArrayList(ShelfMarkTypeEnum.BREAD_SHELF_MARK.getCode());
                }else{
                    shelfMarkList.add(ShelfMarkTypeEnum.BREAD_SHELF_MARK.getCode());
                }

                String format1 = String.format(sql, JsonUtil.toJsonString(shelfMarkList), ShelfMarkTypeEnum.BREAD_SHELF_MARK.getCode(), layoutShelf.getId());
                sqlSb.append(format1).append("\n");
            }

        }

        FileUtil.appendWriteToFile(sb.toString(), TARGET);
        FileUtil.appendWriteToFile(sqlSb.toString(), TARGET1);
        return sb.toString();

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

