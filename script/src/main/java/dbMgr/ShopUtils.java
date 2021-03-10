package dbMgr;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import json.JsonUtil;
import model.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-10 13:02
 **/
public class ShopUtils {
    private static final int pageSize = 100;

    public static void main(String[] args) {
        List<String> shopCodes = Lists.newArrayList("101000077","101000106","101000108","101000112","101000113","101000115","101000116","101000117","101000125","101000126","101000127","101000129","101000132","101000150","101000158","101000168","101000170","101000186","101000193","101000202","101000206","101000215","101000228","101001007","101001011","101001016","101001022","101001026","101001055","109000001","109000018","110000015","110000037","110000072","110001015","110001019","110001039","110001056","111000003","111000006","111000012","111000026","111000032","118000006","118000007","118000017","118000019","127000015","232000003","232000005","232000008","233000003","612000001","612000002","612000007");
        List<Integer> shop_id = shopCodes.parallelStream()
                .map(ShopUtils::queryShopIdByShopCode)
                .collect(Collectors.toList());
        System.out.println(JsonUtil.toJsonString(shop_id));


    }



    public static Shop queryByCode(String shopCode) {
        String sql = "select * from shop where code = '" + shopCode + "'";
        Shop shop = DbaUtils.processAndGetOne(Shop.class, sql);
        return shop;
    }

    /**
     * 根据门店code查询门店id
     * @param shopCode
     * @return
     */
    public static Integer queryShopIdByShopCode(String shopCode){
        String sql = "select id from shop where code = '" +shopCode + "'";
        List<Map<String, String>> maps = DbaUtils.processSql(sql);
        for (Map<String, String> map : maps) {
            return Integer.valueOf(map.get("0"));
        }
        return null;
    }

    /**
     * 查询所有的门店Code
     *
     * @param shopType
     * @param shopBusinessStatus
     * @return
     */
    public static List<String> queryAllShopCodes() {
        String sql = "select id, code from shop where id > %d and  type = 1 and business_state in (1) order by id asc limit 100";
        List<String> shopCodes = new ArrayList<>();

        int curId = 0;
        try {
            while (true) {
                List<Map<String, String>> data = DbaUtils.processSql(sql, curId);
                if(data == null || data.isEmpty()){
                    break;
                }
                List<String> curShopCodes = getShopCodes(data);
                shopCodes.addAll(curShopCodes);

                if(data.size() < pageSize){
                    break;
                }
                curId = Integer.parseInt(Iterables.getLast(data).get("0"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shopCodes;
    }

    /**
     * 查询所有的门店Code
     *
     * @param shopType
     * @param shopBusinessStatus
     * @return
     */
    public static List<Shop> queryAllShops() {
        String sql = "select id, code from shop where id > %d and  type = 1 and business_state in (1) order by id asc limit 100";
        List<Shop> shops = new ArrayList<>();

        int curId = 0;
        try {
            while (true) {
                List<Shop> data = DbaUtils.processSql(Shop.class,sql, curId);
                if(data == null || data.isEmpty()){
                    break;
                }

                shops.addAll(data);

                if(data.size() < pageSize){
                    break;
                }
                curId = Iterables.getLast(data).getId();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shops;
    }

    private static List<String> getShopCodes(List<Map<String, String>> data) {
        List<String> shopCodes = new ArrayList<>();
        for(Map<String, String> map: data){
            shopCodes.add(map.get("1"));
        }
        return shopCodes;
    }

    private static String getFromIntList(List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : list) {
            sb.append(i.toString());
            sb.append(",");
        }

        return sb.substring(0, sb.lastIndexOf(","));
    }
}

