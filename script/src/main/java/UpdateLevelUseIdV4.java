import com.google.common.collect.Lists;
import common.constants.CylHttpConstants;
import common.utils.http.OkHttpUtils;
import json.JsonUtil;
import lombok.Data;
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

/**
 * @description: 修改指定门店指定货架 层板用途从 v1 -> v2
 * @author: yulong.cao
 * @since: 2021-01-07 21:52
 **/
public class UpdateLevelUseIdV4 {
    private static Integer SOURCE_LEVEL_USE_ID = 10;
    private static Integer TARGET_LEVEL_USE_ID = 7;

    private static final String FILE = "/Users/bianlifeng/Documents/work/data/21年春季换季门店清单.xlsx";


    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    //获取货架上的陈列url
    public static String  getShelfDisplays_url = "/chenlie/api/v2/shelfs/%d/floors/displays/withoutHeightInfo?psdType=1&cityCode=0&stand=0&storeType=0";


    //修改层板用途
    public static String updateLevelUseUrl = "/chenlie/api/v2/floors/update";


    public static void main(String[] args) throws Exception{

        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(FILE));
        XSSFSheet sheet = workbook.getSheet("detail");
        int lastRowNum = sheet.getLastRowNum();

        List<Record> skus = new ArrayList<>();
        for(int i = 1; i <= lastRowNum; i++){
            XSSFRow row = sheet.getRow(i);
            String shopCode = row.getCell(0).getRawValue();
            String shelfId = row.getCell(5).getRawValue();
            Record record = new Record();
            record.setShopCode(shopCode);
            record.setShelfId(Integer.parseInt(shelfId));
            skus.add(record);
        }

        ArrayList<String> shopCodes = Lists.newArrayList("100001005",
                "145030208",
                "108000001",
                "121000036",
                "123000290",
                "123000331",
                "123000385",
                "176000011");
        skus.stream()
                .filter(it -> {
                    return shopCodes.contains(it.getShopCode().trim());
                })
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
     * 展开小货架
     * @param shopCode
     * @param shelfId
     */
    public static void updateLevelUseId(String shopCode, Integer shelfId){
        System.out.println("shopCode: " +shopCode);

        //查询货架上的所有陈列
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        String realGetShelfDisplays_url = String.format(getShelfDisplays_url, shelfId);
        Request request = new Request.Builder()
                .url( CylHttpConstants.BASE_URL + realGetShelfDisplays_url)
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

                //如果存在小货架， 则处理小货架
                handleSmallShelfIsExist(curLevel);

                //调用修改层板用途接口，修改层板用途为非日配
                if(needUpdate(curLevel)){
                    updateLevelUse(curLevelId);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean needUpdate(Map curLevel) {
        Integer useId = (Integer) curLevel.get("useId");
        return SOURCE_LEVEL_USE_ID.equals(useId);
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
        map.put("useId", TARGET_LEVEL_USE_ID);

        RequestBody body = RequestBody.create(JsonUtil.toJsonString(map), JSON);
        Response response = OkHttpUtils.post(updateLevelUseUrl, body);

        if(!response.isSuccessful()){
            throw new RuntimeException("跟新货架用途错误, levelId:" + curLevelId);
        }

        System.out.println("层板处理完成，层板id:" + curLevelId );
    }


    /**
     * 获取当前货架上的所有的品
     * @param shelf
     * @return
     */
    private static List<List<Display>> handleSmallShelf(Map shelf) {
        List<Map> levels = (List) shelf.get("data");
        List<List<Display>> displays = new ArrayList<>();

        if(CollectionUtils.isEmpty(levels)){
            return displays;
        }
        for(Map curLevel: levels) {
            if(needUpdate(curLevel)){
                updateLevelUse((Integer) curLevel.get("id"));
            }
        }
        return displays;
    }


    /**
     * 获取当前层板上所有的品
     * @param curLevel
     * @return
     */
    public static void handleSmallShelfIsExist(Map curLevel ){
        List<List<Display>> allDisplays = new ArrayList<>();

        //获取当前层板上的陈列， 有可能是小货架也有可能是品
        List displays = (List) curLevel.get("displays");
        for (Object display : displays) {
            //说明是个小货架
            if (display instanceof Map) {
                //获取小货架上的品
                Map smallShelf = (Map) display;
                handleSmallShelf(smallShelf);
            }
        }

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
    public static class Record {
        String shopCode;
        Integer shelfId;
    }
}
