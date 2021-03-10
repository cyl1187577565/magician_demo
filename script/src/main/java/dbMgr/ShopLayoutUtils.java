package dbMgr;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-10 14:08
 **/
public class ShopLayoutUtils {

    private static String sql_pattern = "select id from shop_layout where shop_code =\'%s\' and layout_status = 1 order by version desc, id desc limit 1";

    private static String sql_pattern1 = "select id from shop_layout where shop_code =\'%s\' and layout_status = 0 order by version desc, id desc limit 1";

    public static void main(String[] args) {
        Integer craftId = queryLatestPublishedId("101000196");
        System.out.println(craftId);
    }

    public static Integer queryLatestPublishedId(String shopCode){
        List<Map<String, String>> list = DbaUtils.processSql(sql_pattern, shopCode);
        if(list == null || list.isEmpty()){
            return null;
        }
        String s = list.get(0).get("0");
        return Integer.parseInt(s);
    }


    public static Integer queryCraftId(String shopCode){
        List<Map<String, String>> list = DbaUtils.processSql(sql_pattern1, shopCode);
        if(list == null || list.isEmpty()){
            return null;
        }
        String s = list.get(0).get("0");
        return Integer.parseInt(s);
    }
}
