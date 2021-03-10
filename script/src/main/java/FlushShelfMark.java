import com.google.common.collect.Lists;
import common.enums.ShelfMarkTypeEnum;
import common.utils.dbMgr.DbaUtils;
import common.utils.dbMgr.ShopLayoutUtils;
import io.fileIo.FileUtil;
import json.JsonUtil;
import model.LayoutShelf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 刷数据
 * @author: yulong.cao
 * @since: 2020-12-10 12:59
 **/
public class FlushShelfMark {

    private static final List<Integer> needToFlushToShelfTypeMark = ShelfMarkTypeEnum.getAllCodes();

    private static final AtomicInteger ai = new AtomicInteger(1);

    public static void main(String[] args) {
        List<String> list = Lists.newArrayList("101000196");
        System.out.println(list.size());
        flushToShelfMark(list);
    }


    /**
     * 将shelfMarkTypeList 刷到ShelfMarkType中
     */
    public static void flushShelfMarkTypeList2ShelfMark(List<String> shopCodes) {
        String sql = "select id, shelf_mark_list from layout_shelf where shop_layout_id = %d and shelf_mark_list != ''";
        shopCodes.parallelStream()
                .forEach(shopCode -> {
                    System.out.println("process shopCode:" + shopCode + " 进度：" + ai.getAndAdd(1) + " 总数：" + shopCodes.size());
                    //查询最新发布状态的id
                    Integer publishedId = ShopLayoutUtils.queryLatestPublishedId(shopCode);
                    if (publishedId != null) {
                        //查询所有的layoutShelfId
                        List<Map<String, String>> maps = DbaUtils.processSql(sql, publishedId);
                        writeToShelfMarkType(maps);
                    }

                    //
                    Integer craftId = ShopLayoutUtils.queryCraftId(shopCode);
                    if (craftId != null) {
                        List<Map<String, String>> maps = DbaUtils.processSql(sql, craftId);
                        writeToShelfMarkType(maps);
                    }
                });
    }

    /**
     * 结果输出
     *
     * @param maps
     */
    private static void writeToShelfMarkType(List<Map<String, String>> maps) {
        String sql_pattern = "update layout_shelf set shelf_mark_type = %d where id = %s";
        StringBuilder sb = new StringBuilder();

        for (Map<String, String> map : maps) {
            String id = map.get("0");
            String shelfMarkList = map.get("1");
            List<Integer> shelfMarks = JsonUtil.ofList(shelfMarkList, Integer.class);
            for (Integer shelfMark : shelfMarks) {
                if (needToFlushToShelfTypeMark.contains(shelfMark)) {
                    String sql = String.format(sql_pattern, shelfMark, id);
                    sb.append(sql).append("\n");
                    continue;
                }
            }
        }

        FileUtil.appendWriteToFile(sb.toString(), "/Users/bianlifeng/Documents/work/data/flushToShelfMarkType");
    }

/** ====================================================分割线 ==========================================*/
    /**
     * 将shelfMakType 刷到shelfMariTypeList
     *
     * @param shopCodes
     */
    public static void flushToShelfMark(List<String> shopCodes) {
        String sql = "select id, shelf_mark_type, shelf_mark_list from layout_shelf where shop_layout_id = %d and shelf_mark_type != 0";

        shopCodes.parallelStream()
                .forEach(shopCode -> {
                    System.out.println("process shopCode:" + shopCode + " 进度：" + ai.getAndAdd(1) + " 总数：" + shopCodes.size());
                    //查询最新发布状态的id
                    Integer publishedId = ShopLayoutUtils.queryLatestPublishedId(shopCode);
                    if (publishedId != null) {
                        //查询所有的layoutShelfId
                        List<LayoutShelf> layoutShelves = DbaUtils.processSql(LayoutShelf.class, sql, publishedId);
                        writeToShelfMarkList(layoutShelves);
                    }

                    //
                    Integer craftId = ShopLayoutUtils.queryCraftId(shopCode);
                    if (craftId != null) {
                        List<LayoutShelf> layoutShelves = DbaUtils.processSql(LayoutShelf.class, sql, publishedId);
                        writeToShelfMarkList(layoutShelves);
                    }
                });
    }


    private static void writeToShelfMarkList(List<LayoutShelf> layoutShelves) {
        String sql_pattern = "update layout_shelf set shelf_mark_list = %s where id = %d";
        StringBuilder sb = new StringBuilder();
        for (LayoutShelf layoutShelf: layoutShelves) {
            Integer layoutShelfId = layoutShelf.getId();
            List<Integer> shelfMarkList = layoutShelf.getShelfMarkList();
            if (shelfMarkList == null){
                shelfMarkList = new ArrayList<>();
            }
            Integer shelfMarkType = layoutShelf.getShelfMarkType();
            if(!shelfMarkList.contains(shelfMarkType)){
                shelfMarkList.add(shelfMarkType);
            }


            String shelfMarkListStr = "\'" + JsonUtil.toJsonString(shelfMarkList) + "\'";
            String sql = String.format(sql_pattern, shelfMarkListStr, layoutShelfId);
            sb.append(sql).append(";\n");
        }


        FileUtil.appendWriteToFile(sb.toString(), "/Users/bianlifeng/Documents/work/data/flushToShelfMarkList");

    }
}
