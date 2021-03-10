import com.google.common.collect.Lists;
import excel.ExcelUtil;
import lombok.Data;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description: 先将周对周清单的数据进行删除，然后在进行追加
 * @author: yulong.cao
 * @since: 2020-05-11 15:49
 **/
public class Week2WeekDeleteAndAddScript {
    static Map<String, String> typeMap;
    static {
        typeMap = new HashMap<>();
        typeMap.put("必上", "must_add");
        typeMap.put("选上", "selected_add");
        typeMap.put("必下", "must_remove");
    }

    private static final String MUST_ADD = "must_add";
    private static final String MUST_REMOVE = "must_remove";
    private static final String SELECTED_ADD = "selected_add";
    private static final String SAVE_SKU = "save_sku";

    /**
     * 文件的根目录
     */
    private static final String BASE_PATH = "/Users/bianlifeng/Documents/work/data/";

    /**
     * 目标目录
     */
    private static final String TARGET_PATH = "/Users/bianlifeng/Documents/work/data/";

    /**
     * 周对周预跑清单store_sheet
     */
    private static final String STORE_LIST = "store_list";

    /**
     * 周对周预跑清单sku_list
     */
    private static final String SKU_LIST = "sku_list";


    /* ################################需要修改的变量################################ */

    /**
     *
     */
    private static final String BASE_FILE = "2月1号生效周清单(1).xlsx";

    /**
     * 增量清单
     */
    private static final String ADVERTISING_LIST = "20210113_add2.xlsx";


    private static final String target = "2021_02_01_v2周对周陈列清单.xlsx";


    private static final String SKU_TYPE = MUST_ADD;

//    private static final String SKU_IDENTITY = "广告商品";
//    private static final String SKU_IDENTITY = "商品结构调整";
    private static final String SKU_IDENTITY = "";
//    private static final String SKU_IDENTITY = "试新品";

    /**
     * 使用就增量的row 构建sku对象
     *
     * @param row
     * @return
     */
    private static Sku buildWithIncrementRow(XSSFRow row) {
        Sku sku = new Sku();
        if(row.getCell(0).getCellTypeEnum().equals(CellType.NUMERIC)){
            row.getCell(0).setCellType(CellType.STRING);
        }
        sku.setStoreCode(row.getCell(0).getStringCellValue());

        if(row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)){
            row.getCell(1).setCellType(CellType.STRING);
        }
        sku.setSkuCode(row.getCell(1).getStringCellValue());
//
        if(row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)){
            row.getCell(2).setCellType(CellType.STRING);
        }
        String skuType = row.getCell(2).getStringCellValue();

//        String skuTypeKey = row.getCell(2).getStringCellValue();
//        String key = skuTypeKey.trim();
//        String skuType = typeMap.get(key);
//        if(StringUtils.isBlank(skuType)){
//            throw new RuntimeException("缺少skuType");
//        }

        sku.setSkuType(skuType);
        sku.setSkuIendtity(SKU_IDENTITY);
        sku.setPsd("0.0");
        sku.setPsdAmount("0");
        sku.setFace("1");
        sku.setShelfType("0");
        return sku;

    }


    public static void main(String[] args) throws IOException {
        //陈列预跑清单
        String filePath = TARGET_PATH + BASE_FILE;

        Map<String, Sku> targetSkuMap = new LinkedHashMap<>();
        List<Store> targetStoreList = new ArrayList<>();

        //读取skuSheet
        readSkuSheet(filePath, targetSkuMap);

        //读取storeSheet
        readStoreSheet(filePath, targetStoreList);

        //增量清单
//        readOtherList(targetSkuMap);


        processStoreList(targetStoreList, targetSkuMap);

        writeResult(targetSkuMap, targetStoreList);
    }

    /**
     * 输出结果
     * @param targetSkuMap
     * @param targetStoreList
     */
    private static void writeResult(Map<String, Sku> targetSkuMap, List<Store> targetStoreList) throws IOException {
        //结果输出
        String targetFileName = TARGET_PATH + target;
        FileOutputStream fos = new FileOutputStream(targetFileName);
        XSSFWorkbook targetWb = new XSSFWorkbook();
        SXSSFWorkbook swb = new SXSSFWorkbook(targetWb, 10000);

        System.out.println(">> 开始写入storeSheet数据");
        int i = 0;
        SXSSFSheet targetStoreSheet = swb.createSheet(STORE_LIST);
        for (i = 0; i < targetStoreList.size(); i++) {
            System.out.println(">> 开始写入storeSheet数据 rowNum: " + i);
            Store curStore = targetStoreList.get(i);
            SXSSFRow row = targetStoreSheet.createRow(i);
            curStore.createAllCells(row);
        }


        System.out.println(">> 开始写入skuSheet数据");
        SXSSFSheet targetSkuSheet = swb.createSheet(SKU_LIST);
        List<Sku> targetSkuList = Lists.newArrayList(targetSkuMap.values());
        for (i = 0; i < targetSkuList.size(); i++) {
            System.out.println(">> 开始写入skuSheet数据 rowNum: " + i);
            Sku curSku = targetSkuList.get(i);
            SXSSFRow row = targetSkuSheet.createRow(i);
            curSku.createAllCells(row);
        }

        swb.write(fos);
        fos.close();

    }

    private static void processStoreList(List<Store> targetStoreList, Map<String, Sku> targetSkuMap) {
        Set<String> storeCodeSet = targetStoreList.stream().map(Store::getStoreCode).collect(Collectors.toSet());
        Set<String> needAddStoreCodeSet = targetSkuMap.values().stream()
                .map(Sku::getStoreCode)
                .filter(code -> {
                    return !storeCodeSet.contains(code);
                })
                .collect(Collectors.toSet());


        Store store = targetStoreList.get(targetStoreList.size() - 1);
        List<Store> needAddStoreList = needAddStoreCodeSet.stream()
                .map(code -> {
                    return buildWithCodeAndTemp(code, store);
                })
                .collect(Collectors.toList());
        targetStoreList.addAll(needAddStoreList);
    }

    private static void readOtherList(Map<String, Sku> map) throws IOException {
        String otherFile = BASE_PATH + ADVERTISING_LIST;
        FileInputStream advertisingFis = new FileInputStream(otherFile);
        XSSFWorkbook advertisingWB = new XSSFWorkbook(advertisingFis);
        XSSFSheet adSheet = advertisingWB.getSheet("sku_list");


        //处理广告品
        int i = 0;
        System.out.println(">> 开始读取增量清单");
        int adSheetLastRowNum = adSheet.getLastRowNum();
        for ( i = 1; i <= adSheetLastRowNum; i++) {
            System.out.println(">> 开始读取增量清单 rowNum: " + i);
            XSSFRow row = adSheet.getRow(i);
            Sku sku = buildWithIncrementRow(row);
            if(!needSku(sku)){
                continue;
            }
            map.put(sku.key(), sku);
        }
        advertisingFis.close();
    }

    private static boolean needSku(Sku sku) {
        List<String> shopCodes = Lists.newArrayList("123000153","109000056","110000126","123000155","118000011","110000083","110001039");
        return shopCodes.contains(sku.getStoreCode());
    }

    private static List<Store> readStoreSheet(String filePath, List<Store> targetStoreList) {
        int i = 0;
        System.out.println(">> 开始读取storeSheet");
        List<List<String>> storeList = ExcelUtil.getAllData(filePath, STORE_LIST);
        for (List<String> curStore : storeList) {
            System.out.println(">> 开始读取storeSheet rowNum: " + (++i));
            Store store = buildStoreWithStoreRow(curStore);
            targetStoreList.add(store);
        }

        return targetStoreList;
    }

    private static Map<String, Sku> readSkuSheet(String filePath, Map<String, Sku> map) {


        System.out.println(">> 开始去读取skuSheet");
        List<List<String>> skuList = ExcelUtil.getAllData(filePath, SKU_LIST);
        int i = 0;
        for (List<String> curSku : skuList) {
            System.out.println(">> 开始去读取skuSheet rowNum: " + (++i));
            System.out.println(curSku.toString());
            Sku sku = buildWithSkuList(curSku);
            if(ignoreSku(sku)){
                continue;
            }
            map.put(sku.key(), sku);

        }
        return map;
    }

    /**
     * 是否需要忽略sku
     * @param sku
     * @return
     */
    private static boolean ignoreSku(Sku sku) {
        return false;
    }


    /**
     * 使用skuListRow构建sku
     *
     * @param row
     * @return
     */
    private static Sku buildWithSkuList(List<String> row) {
        Sku sku = new Sku();
        sku.setStoreCode(row.get(0));
        sku.setSkuCode(row.get(1));
        sku.setSkuType(row.get(2));
        sku.setSkuIendtity(row.get(3));
        sku.setPsd(row.get(4));
        sku.setPsdAmount(row.get(5));
        sku.setFace(row.get(6));
        sku.setShelfType(row.get(7));
        return sku;
    }

    /**
     * 构建Store对象
     *
     * @param row
     * @return
     */
    private static Store buildStoreWithStoreRow(List<String> row) {
        Store store = new Store();
        store.setStoreCode(row.get(0));
        store.setStoreOperation(row.get(1));
        store.setStoreType(row.get(2));
        store.setMaxDownStock(row.get(3));
        store.setBlockLaminates(row.get(4));
        store.setDataSource(row.get(5));
        store.setAmountProfit(row.get(6));
        store.setEffectiveTime(row.get(7));
        return store;
    }


    /**
     * 根据code + temp 构建Store
     *
     * @param code
     * @param temp
     * @return
     */
    private static Store buildWithCodeAndTemp(String code, Store temp) {
        Store store = new Store();
        store.setStoreCode(code);
        store.setStoreOperation(temp.getStoreOperation());
        store.setStoreType(temp.getStoreType());
        store.setMaxDownStock(temp.getMaxDownStock());
        store.setBlockLaminates(temp.getBlockLaminates());
        store.setDataSource(temp.getDataSource());
        store.setAmountProfit(temp.getAmountProfit());
        store.setEffectiveTime(temp.getEffectiveTime());
        return store;

    }

    @Data
    public static class Sku {
        private static String STORE_CODE = "store_code";
        private static String SKU_CODE = "sku_code";
        private static String SKU_TYPE = "sku_type";
        private static String SKU_IDENTITY = "sku_identity";
        private static String PSD = "psd";
        private static String PSD_AMOUNT = "psd金额";
        private static String FACE = "face";
        private static String SHELF_TYPE = "shelf_type";

        private static Map<Integer, String> headMap = new HashMap() {{
            put(0, STORE_CODE);
            put(1, SKU_CODE);
            put(2, SKU_TYPE);
            put(3, SKU_IDENTITY);
            put(4, PSD);
            put(5, PSD_AMOUNT);
            put(6, FACE);
            put(7, SHELF_TYPE);
        }};


        public static void createHead(SXSSFSheet sheet) {
            SXSSFRow row = sheet.createRow(0);
            for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                SXSSFCell cell = row.createCell(entry.getKey());
                cell.setCellValue(entry.getValue());
            }
        }

        public void createAllCells(SXSSFRow row) {
            for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                SXSSFCell cell = row.createCell(entry.getKey());
                String cellValue = getValue(entry.getKey());
                cell.setCellValue(cellValue);
            }
        }

        private String getValue(Integer key) {
            if (0 == key) {
                return storeCode;
            }

            if (1 == key) {
                return skuCode;
            }

            if (2 == key) {
                return skuType;
            }
            if (3 == key) {
                return skuIendtity;
            }
            if (4 == key) {
                return psd;
            }
            if (5 == key) {
                return psdAmount;
            }
            if (6 == key) {
                return face;
            }
            if (7 == key) {
                return shelfType;
            }
            return null;
        }


        private String storeCode;
        private String skuCode;
        private String skuType;
        private String skuIendtity;
        private String psd;
        private String psdAmount;
        private String face;
        private String shelfType;

        public String key() {
            return storeCode + skuCode;
        }
    }

    @Data
    public static class Store {
        private static String STORE_CODE = "store_code";
        private static String STORE_OPERATION = "store_operation";
        private static String STORE_TYPE = "store_type";
        private static String MAX_DOWN_STOCK = "max_down_stock";
        private static String BLOCK_LAMINATES = "block_laminates";
        private static String DATA_SOURCE = "data_source";
        private static String AMOUNT_PROFIT = "amount_profit";
        private static String EFFECTIVE_TIME = "effective_time";
        private static Map<Integer, String> headMap = new HashMap() {{
            put(0, STORE_CODE);
            put(1, STORE_OPERATION);
            put(2, STORE_TYPE);
            put(3, MAX_DOWN_STOCK);
            put(4, BLOCK_LAMINATES);
            put(5, DATA_SOURCE);
            put(6, AMOUNT_PROFIT);
            put(7, EFFECTIVE_TIME);
        }};


        public static void createHead(SXSSFSheet sheet) {
            SXSSFRow row = sheet.createRow(0);
            for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                SXSSFCell cell = row.createCell(entry.getKey());
                cell.setCellValue(entry.getValue());
            }
        }

        public void createAllCells(SXSSFRow row) {
            for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                SXSSFCell cell = row.createCell(entry.getKey());
                String cellValue = getValue(entry.getKey());
                cell.setCellValue(cellValue);
            }
        }

        private String getValue(Integer key) {
            if (0 == key) {
                return storeCode;
            }

            if (1 == key) {
                return storeOperation;
            }

            if (2 == key) {
                return storeType;
            }
            if (3 == key) {
                return maxDownStock;
            }
            if (4 == key) {
                return blockLaminates;
            }
            if (5 == key) {
                return dataSource;
            }
            if (6 == key) {
                return amountProfit;
            }
            if (7 == key) {
                return effectiveTime;
            }
            return null;
        }


        private String storeCode;
        private String storeOperation;
        private String storeType;
        private String maxDownStock;
        private String blockLaminates;
        private String dataSource;
        private String amountProfit;
        private String effectiveTime;

    }

}