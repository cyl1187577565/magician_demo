import common.constants.CylHttpConstants;
import common.utils.dbMgr.BaseShelfTypeUtils;
import common.utils.dbMgr.DbaUtils;
import common.utils.http.OkHttpUtils;
import json.JsonUtil;
import lombok.Data;
import model.Shelf;
import model.ShelfType;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 展开指定的小货架 并且删除小货架 然后修改层板用途为非日配
 * @author: yulong.cao
 * @since: 2021-01-07 21:52
 **/
public class UpdateLevelUseIdV3 {

    private static final String FILE = "/Users/bianlifeng/Documents/work/data/冰淇淋柜删层板删继承.1.xlsx";


    public static Map<Integer, ShelfType> shelfTypeMap;

    static {
        List<ShelfType> shelfTypes = BaseShelfTypeUtils.queryAll();
        shelfTypeMap = shelfTypes.stream().collect(Collectors.toMap(ShelfType::getId, Function.identity(), (v1, v2) -> v1));

    }


    public static String baseUrl = CylHttpConstants.BASE_URL;

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    //获取货架上的陈列url
    public static String  getShelfDisplays_url = "/chenlie/api/v2/shelfs/%d/floors/displays/withoutHeightInfo?psdType=1&cityCode=0&stand=0&storeType=0";

    //同步接口url
    public static String sync_url = "/chenlie/api/v2/floors/displays/sync";

    //删除货架的url
    public static String deleteShelfUrl = "/chenlie/api/v2/shelfs/delete";

    //修改层板用途
    public static String updateLevelUseUrl = "/chenlie/api/v2/floors/update";


    public static void main(String[] args) throws Exception{

        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(FILE));
        XSSFSheet sheet = workbook.getSheet("detail");
        int lastRowNum = sheet.getLastRowNum();

        List<Sku> skus = new ArrayList<>();
        for(int i = 1; i <= lastRowNum; i++){
            XSSFRow row = sheet.getRow(i);
            String shopCode = row.getCell(2).getRawValue();
            String shelfId = row.getCell(4).getRawValue();
            Sku sku = new Sku();
            sku.setShopCode(shopCode);
            sku.setShelfId(Integer.parseInt(shelfId));
            skus.add(sku);
        }

        skus.stream()
                .forEach(it -> {
                    try{
                        updateLevelUseId(it.getShopCode(), it.getShelfId());
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                });

//        updateLevelUseId("123000313",15830852);
    }



    /**
     * 获取需要更新的数据
     */
    public static void updateLevelUseId(String shopCode,Integer virtualShelfId){
        //获取父货架id
        String getParentShelfIdSql = "select parent_id from shelf where id = " + virtualShelfId;
        Shelf shelf = DbaUtils.processAndGetOne(Shelf.class, getParentShelfIdSql);
        if(shelf == null || shelf.getParentId() == null){
            return;
        }


        System.out.println("门店code:" + shopCode + " 货架id: " + shelf.getParentId());
        updateLevelUseId(shopCode, shelf.getParentId(), virtualShelfId);

    }

    /**
     * 展开小货架
     * @param shopCode
     * @param shelfId
     */
    public static void updateLevelUseId(String shopCode, Integer shelfId, Integer virtualShelfId){
        //获取货架数据
        String sql = "select id, size_type from shelf where id = %d";
        Shelf shelf = DbaUtils.processAndGetOne(Shelf.class, sql, shelfId);


        //查询大货架上所有的陈列
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        String realGetShelfDisplays_url = String.format(getShelfDisplays_url, shelfId);
        Request request = new Request.Builder()
                .url( baseUrl + realGetShelfDisplays_url)
                .method("GET", null)
                .addHeader("Cookie", CylHttpConstants.COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()){
                return;
            }

            Map body = JsonUtil.of(response.body().string(), Map.class);
            Map shelfMap = (Map) body.get("data");
            if(MapUtils.isEmpty(shelfMap)){
                return;
            }
            List<Map> levels = (List) shelfMap.get("displays");
            if(CollectionUtils.isEmpty(levels)){
                return;
            }

            for(Map curLevel: levels) {
                Integer curLevelId = (Integer) curLevel.get("id");
                if (ignoreLevel(virtualShelfId, curLevel)) {
                    continue;
                }

                //获取当前层板上的所有的品
                List<List<Display>> displays = getDisplaysFromLevel(curLevel);
                //调用sync接口同步层板
                if(CollectionUtils.isNotEmpty(displays)){
                    SyncParam syncParam = buildSyncParam(shelf,curLevelId, displays);
                    sync(syncParam);
                }

                //调用删除小货架接口删除小货架， 如果没有小货架则不需要删除小货架
                List<Integer>  smallShelfIds = getSmallShelfIds(curLevel);
                if(CollectionUtils.isNotEmpty(smallShelfIds)){
                    for (Integer smallShelfId: smallShelfIds){
                        deleteShelf(smallShelfId);
                    }
                }

                //调用修改层板用途接口，修改层板用途为非日配
                updateLevelUse(curLevelId);

                System.out.println("层板处理完成，层板id:" + curLevelId );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean ignoreLevel(Integer virtualShelfId, Map curLevel){
        List displays = (List) curLevel.get("displays");
        for (Object display : displays) {
            //说明是个小货架
            if (display instanceof Map) {
                //获取小货架上的品
                Map smallShelf = (Map) display;
                Integer smallShelfId = (Integer) smallShelf.get("id");
                if(smallShelfId.equals(virtualShelfId)){
                    return false;
                }
            }

        }

        return true;
    }

    /**
     * 修改层板用途
     * @param curLevelId
     */
    private static void updateLevelUse(Integer curLevelId) {
        Map<String, Integer> map = new HashMap<>();
        map.put("id", curLevelId);
        map.put("useId", 1);

        RequestBody body = RequestBody.create(JsonUtil.toJsonString(map), JSON);
        Response response = OkHttpUtils.post(updateLevelUseUrl, body);

        if(!response.isSuccessful()){
            throw new RuntimeException("跟新货架用途错误, levelId:" + curLevelId);
        }
    }

    /**
     * 删除货架
     * @param smallShelfId
     */
    private static void deleteShelf(Integer smallShelfId) {
        DeleteShelfParam deleteShelfParam = new DeleteShelfParam();
        deleteShelfParam.setId(smallShelfId);
        RequestBody body = RequestBody.create(JsonUtil.toJsonString(deleteShelfParam), JSON);

        Response response = OkHttpUtils.post(deleteShelfUrl, body);

        if (!response.isSuccessful()){
            throw new RuntimeException("删除货架出现异常,货架id:{}" + smallShelfId);
        }

    }

    private static List<Integer> getSmallShelfIds(Map curLevel) {
        List<Integer> smallShelfId = new ArrayList<>();

        //获取当前层板上的陈列， 有可能是小货架也有可能是品
        List displays = (List) curLevel.get("displays");
        for (Object display : displays) {
            //说明是个小货架
            if (display instanceof Map) {
                Integer shelfId = (Integer) ((Map) display).get("id");
                smallShelfId.add(shelfId);
            }

        }

        return smallShelfId;
    }

    /**
     * 构建sync接口入参
     * @param shelf
     * @param curLevelId
     * @param displays
     * @return
     */
    private static SyncParam buildSyncParam(Shelf shelf, Integer curLevelId, List<List<Display>> displays) {
        Integer sizeTypeId = shelf.getSizeType();
        ShelfType sizeType = shelfTypeMap.get(sizeTypeId);
        ShelfType type = shelfTypeMap.get(sizeType.getParentId());
        ShelfType skuShelfType = shelfTypeMap.get(type.getParentId());

        SyncParam syncParam = new SyncParam();
        syncParam.setShelfId(shelf.getId());
        syncParam.setId(curLevelId);
        syncParam.setSkuShelfType(skuShelfType.getId());
        syncParam.setType(type.getId());
        syncParam.setSizeType(type.getId());
        syncParam.setDisplays(displays);

        return syncParam;
    }

    /**
     * 调用sync接口
     */
    public static void sync(SyncParam syncParam){
        RequestBody body = RequestBody.create(JsonUtil.toJsonString(syncParam), JSON);
        Response response = OkHttpUtils.post(sync_url, body);
        if(!response.isSuccessful()){
            throw new RuntimeException("执行sync接口出现错误, 货架id：" + syncParam.shelfId);
        }

    }


    /**
     * 获取当前货架上的所有的品
     * @param shelf
     * @return
     */
    private static List<List<Display>> getDisplaysFromShelf(Map shelf) {
        List<Map> levels = (List) shelf.get("data");
        List<List<Display>> displays = new ArrayList<>();

        if(CollectionUtils.isEmpty(levels)){
            return displays;
        }
        for(Map curLevel: levels) {
            List<List<Display>> displaysFromLevel = getDisplaysFromLevel(curLevel);
            displays.addAll(displaysFromLevel);
        }
        return displays;
    }


    /**
     * 获取当前层板上所有的品
     * @param curLevel
     * @return
     */
    public static List<List<Display>> getDisplaysFromLevel(Map curLevel ){
        List<List<Display>> allDisplays = new ArrayList<>();

        //获取当前层板上的陈列， 有可能是小货架也有可能是品
        List displays = (List) curLevel.get("displays");
        for (Object display : displays) {
            //说明是个小货架
            if (display instanceof Map) {
                //获取小货架上的品
                Map smallShelf = (Map) display;
                List<List<Display>> displaysFromShelf = getDisplaysFromShelf(smallShelf);
                allDisplays.addAll(displaysFromShelf);
                continue;
            }

            //说明是品
            List<Display> curDisplay = JsonUtil.ofList(JsonUtil.toJsonString(display), Display.class);
            if (curDisplay != null){
                allDisplays.add(curDisplay);
            }
        }

        return allDisplays;
    }

    @Data
    public static class DeleteShelfParam{
        Integer id;
    }


    @Data
    public static class SyncParam{
        Integer shelfId;

        //层板id
        Integer id;
        Integer skuShelfType;
        Integer type;
        Integer sizeType;
        List<List<Display>> displays;

    }


    @Data
    public static class Display{
        private String displayWay;
        private Integer maxVCount;
        private String productCode;
        private Integer rotationAngle;
        private Integer skuBoxSpecId;
        private Integer vCount;
        private Integer wayType;
    }

    @Data
    public static class Sku{
        String shopCode;
        Integer shelfId;
    }
}
