import com.google.common.collect.Lists;
import common.utils.dbMgr.DbaUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: 统计缺失货架相对位置关系的门店
 * @author: yulong.cao
 * @since: 2020-12-29 10:42
 **/
public class CountShelfRelationMissing {

    public static void main(String[] args) {
        ArrayList<String> shopCodes = Lists.newArrayList("101000115","100000330","110000058","232000003","100000395","100000288","110000002","110000056","100017005","123000091","101000189","100000026","110001011","110001029","110000088","100000609","123000020","100000021","110000139","100001027","111000013","110001027","232000007","123001060","123000109","123000263","123000180","123000195","123001016","109000015","123000353","123000298","109000009","123000167","110000023","123001027","100000212","110000162","123000110","110001003","110000036","100017003","123000138","397000005","110000009","123001039","123000193","123000029","118000025","110000070","100010001","123000102","100078006","110000006","110000026","110001052","110000039","123001013","123000121","109000026","123000250","110000087","123000360","123001022","109000060","110000069","110000071","107000018","110000113","123001077","123000127","111000038","123000361","110000055","123000336","123000006","123000071","100000056","123000095","121000005");
        shopCodes.stream()
                .forEach(it -> {
                    count(it.toString());
                });
    }

    private static void count(String shopCode){
        String sql = "select data from layout_analysis where shop_layout_id = (select id from shop_layout where shop_code = '%s' and layout_status = 1 order by version desc, id desc limit 1) and type = 2 order by id desc limit 1";
        List<Map<String, String>> maps = DbaUtils.processSql(sql, shopCode);
        if(maps.isEmpty()){
            System.out.println(shopCode);
            return;
        }

        Map<String, String> map = maps.get(0);
        if(map == null){
            System.out.println(shopCode);
            return;
        }

        String s = map.get("0");
        if(StringUtils.isBlank(s)){
            System.out.println(shopCode);
            return;
        }
    }
}
