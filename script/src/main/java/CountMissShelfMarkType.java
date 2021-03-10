import com.google.common.collect.Lists;
import common.utils.dbMgr.DbaUtils;
import common.utils.dbMgr.ShopUtils;
import io.fileIo.FileUtil;
import model.LayoutShelf;
import model.Shelf;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 将布局图上的货架标记刷到实时陈列
 *
 * @author: yulong.cao
 * @since: 2021-01-05 19:30
 **/
public class CountMissShelfMarkType {
    public static String fileName = "/Users/bianlifeng/Documents/work/data/flushMarkType.sql";

    public static void main(String[] args) {
        List<String> list = ShopUtils.queryAllShopCodes();
        List<Integer> shopCodes = Lists.newArrayList(
                100000208,101000231,232000001,101000115,100000522,100022005,100000095,110000119,110000111,100000330,101000091,100017002,100001151,110000058,232000003,100033002,118000006,100000395,100000686,100000288,100022001,101000181,100000351,101000159,101000232,110000002,110000056,110000015,100017005,101001028,109000073,110000156,100001079,123000091,101000189,101001009,100001086,100000026,100000335,101000229,110001011,110001029,100000357,123000238,100000507,111000058,188001005,100000697,101000050,110000088,100000587,100000609,123000370,176000011,100001077,123001057,123000020,110000032,100076007,100000021,100001112,110000139,100001071,123000075,100001027,111000013,110000059,100000620,109000002,109000039,123000073,101000056,110000161,123000191,110001027,100000610,232000007,101001013,123001060,123000109,123000328,110000053,123001026,123000263,100000311,123000085,110000080,100075007,110001053,109000068,123001035,100001096,123000180,100000270,397000010,612000011,109000025,123000195,612000013,110000012,101000225,110001025,123001016,110001019,176000007,100000661,176000003,110000011,109000015,110000138,110000061,110000081,110000158,110001061,110000102,123000150,110000153,100000667,100000290,123000353,123000171,123000011,100070001,110000062,110001005,612000015,100000516,110000092,110000097,100000379,123000298,109000009,123000167,110000023,115000012,123001027,100000196,101000075,100000212,123000256,100001065,100000518,110000162,100071008,107000002,123000110,110001003,110000057,110000036,100017003,123000163,100000623,123000138,100002002,123000378,397000005,110000009,123001039,110000086,123000193,100000228,100073007,100000088,123000029,100000675,110000021,107000037,123000090,101000186,100075008,118000025,123000267,110000070,100000593,109000061,100010001,123000102,110000051,100078006,110000006,110000165,110000026,123000116,110001052,110000039,123001013,123000121,100079023,100000333,109000026,110001063,101000079,123000086,109000012,123000250,100001069,123000331,123000027,110000087,123000026,123000360,123000101,100000375,123001022,123001003,109000060,110001002,123000053,232000008,100000630,109000007,110000050,100000681,123000112,101000061,110000069,109000003,100000167,100000027,107000016,100000616,110000060,110000105,107000007,100001110,110000071,107000018,110000113,188001011,123001077,123000369,123000127,100000319,111000038,100000258,100001129,110000155,100000325,100001099,123000361,100001032,110000150,100000670,100000573,110000055,110000077,100001028,123000336,612000008,123000006,100000668,123000071,188001009,100000210,100000056,115000020,123000095,101000203,100079021,100000236,121000005,123000355
        );



        shopCodes.parallelStream()
                .forEach(it -> {
                    isMissing(it.toString());
                });

    }


    /**
     * 是否丢失了
     * @param shopCode
     */
    public static void isMissing(String shopCode){
        String sql1 = "select shelf_id, shelf_mark_type  from layout_shelf where shop_layout_id = (select id from shop_layout where shop_code = '%s' and layout_status = 1 order by version desc, id desc limit 1) and shelf_mark_type != 0";
        List<LayoutShelf> layoutShelves = DbaUtils.processSql(LayoutShelf.class, sql1, shopCode);
        if(layoutShelves == null || layoutShelves.isEmpty()){
            return;
        }
        Map<Integer, Integer> shelfId2MarkTypeMap = layoutShelves.stream().collect(Collectors.toMap(LayoutShelf::getShelfId, LayoutShelf::getShelfMarkType));


        String sql = "select id, mark_type from shelf where id in ( select shelf_id  from layout_shelf where shop_layout_id = (select id from shop_layout where shop_code = '%s' and layout_status = 1 order by version desc, id desc limit 1) and shelf_mark_type != 0)";
        List<Shelf> shelves = DbaUtils.processSql(Shelf.class, sql, shopCode);

        boolean isMissing = false;
        String sqlPattern = "update shelf set mark_type = %d where id = %d";
        StringBuilder sb = new StringBuilder();
        for(Shelf shelf: shelves){
            if (shelf.getMarkType() == 0){
                Integer markType = shelfId2MarkTypeMap.get(shelf.getId());
                String format = String.format(sqlPattern, markType,shelf.getId());
                sb.append(format).append(";\n");
                isMissing = true;
            }
        }

        if(isMissing){
            FileUtil.appendWriteToFile(sb.toString(), fileName);
            System.out.println(shopCode);
        }
    }


    public static void isMatch(String shopCode){
        String sql1 = "select shelf_id, shelf_mark_type  from layout_shelf where shop_layout_id = (select id from shop_layout where shop_code = '%s' and layout_status = 1 order by version desc, id desc limit 1) and shelf_mark_type != 0";
        List<LayoutShelf> layoutShelves = DbaUtils.processSql(LayoutShelf.class, sql1, shopCode);
        if(layoutShelves == null || layoutShelves.isEmpty()){
            return;
        }
        Map<Integer, Integer> shelfId2MarkTypeMap = layoutShelves.stream().collect(Collectors.toMap(LayoutShelf::getShelfId, LayoutShelf::getShelfMarkType));


        String sql2 = "select id, mark_type from shelf where id in ( select shelf_id  from layout_shelf where shop_layout_id = (select id from shop_layout where shop_code = '%s' and layout_status = 1 order by version desc, id desc limit 1) and shelf_mark_type != 0)";
        List<Shelf> shelves = DbaUtils.processSql(Shelf.class, sql2, shopCode);

        boolean isMatch = true;

        for(Shelf shelf: shelves){
            Integer markType = shelfId2MarkTypeMap.get(shelf.getId());
            Integer markType1 = shelf.getMarkType();
            if (markType != markType1){
                isMatch = false;
            }

        }

        if(!isMatch){
            System.out.println(shopCode);
        }
    }
}
