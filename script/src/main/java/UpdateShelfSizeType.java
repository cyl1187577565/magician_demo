import common.utils.dbMgr.BaseShelfTypeUtils;
import common.utils.dbMgr.DbaUtils;
import common.utils.dbMgr.ShopLayoutUtils;
import io.fileIo.FileUtil;
import json.JsonUtil;
import lombok.Data;
import model.ShelfType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 修改布局图上货架类型和shelf的三级货架类型
 * @author: yulong.cao
 * @since: 2021-01-14 10:56
 **/
public class UpdateShelfSizeType {

    private static final String FILE_NAME = "/Users/bianlifeng/Documents/work/data/风幕柜拼接货架信息修改.xlsx";
    private static final String TARGET = "/Users/bianlifeng/Documents/work/data/风幕柜拼接货架信息修改n.sql";

    public static Map<String, ShelfType> shelfTypeMap;

    static {
        List<ShelfType> shelfTypes = BaseShelfTypeUtils.queryAll();
        shelfTypeMap = shelfTypes.stream().collect(Collectors.toMap(ShelfType::getName, Function.identity(), (v1, v2) -> v1));

    }

    public static void main(String[] args) throws Exception{
        FileInputStream fis = new FileInputStream(FILE_NAME);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        XSSFSheet sheet = workbook.getSheet("target");
        int lastRowNum = sheet.getLastRowNum();

        List<ChangeShelfSizeTypeData> allData = new ArrayList<>();
        for(int i = 1; i <= lastRowNum ; i++){
            XSSFRow row = sheet.getRow(i);
            ChangeShelfSizeTypeData data = getRowData(row);
            allData.add(data);
        }

        StringBuilder sb = new StringBuilder();
        for(ChangeShelfSizeTypeData data: allData){
            generateSql(sb,data);
        }

        FileUtil.appendWriteToFile(sb.toString(), TARGET);

    }

    /**
     * 生成sql
     * @param sb
     * @param data
     * @return
     */
    private static void generateSql(StringBuilder sb, ChangeShelfSizeTypeData data) {
        System.out.println("处理数据:" + JsonUtil.toJsonString(data));
        String sql1 = "update shelf set size_type = %d where id = %d;";
        String realSql1 = String.format(sql1, data.getTargetShelfSizeTypeId(), data.getShelfId());
        sb.append(realSql1)
                .append("\n");

        List<Integer> allLayoutShelfIds = new ArrayList<>();
        Integer publishedId = ShopLayoutUtils.queryLatestPublishedId(data.shopCode);
        if(publishedId != null){
            List<Integer> shelfIds = listLayoutShelfIds(publishedId, data.getShelfId());
            allLayoutShelfIds.addAll(shelfIds);
        }

        Integer craftId = ShopLayoutUtils.queryCraftId(data.getShopCode());
        if(craftId != null){
            List<Integer> shelfIds = listLayoutShelfIds(craftId, data.getShelfId());
            allLayoutShelfIds.addAll(shelfIds);
        }


        String sql2 = "update layout_shelf set shelf_size_type = %d where id = %d;";
        for(Integer layoutShelfId: allLayoutShelfIds){
            String realSql2 = String.format(sql2, data.getTargetShelfSizeTypeId(), layoutShelfId);
            sb.append(realSql2).append("\n");
        }

    }

    private static List<Integer> listLayoutShelfIds(Integer shopLayoutId, Integer shelfId){
        String sql = "select id from layout_shelf where shop_layout_id = %d and shelf_id = %d";
        List<Map<String, String>> maps = DbaUtils.processSql(sql, shopLayoutId, shelfId);
        List<Integer> result = new ArrayList<>();
        for(Map<String, String> map : maps){
            String s = map.get("0");
            int layoutShelfId = Integer.parseInt(s);
            result.add(layoutShelfId);
        }
        return result;
    }


    private static ChangeShelfSizeTypeData getRowData(XSSFRow row) {
        String shopCode = row.getCell(0).getRawValue();
        String shelfIdStr = row.getCell(3).getRawValue();
        int shelfId = Integer.parseInt(shelfIdStr);
        String targetShelfSizeTypeName = row.getCell(6).getStringCellValue().trim();

        ChangeShelfSizeTypeData data = new ChangeShelfSizeTypeData();
        data.setShopCode(shopCode);
        data.setShelfId(shelfId);
        data.setTargetShelfSizeTypeName(targetShelfSizeTypeName);
        ShelfType shelfType = shelfTypeMap.get(targetShelfSizeTypeName);
        if(shelfType == null){
            System.out.println("缺少货架类型shelfId:" + shelfIdStr);
            return null;
        }
        data.setTargetShelfSizeTypeId(shelfType.getId());

        return data;
    }

    @Data
    public static class ChangeShelfSizeTypeData{
        private String shopCode;
        private Integer shelfId;
        private String targetShelfSizeTypeName;
        private Integer targetShelfSizeTypeId;
    }
}
