package dbMgr;

import model.ShopSnap;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-16 20:13
 **/
public class ShopSnapUtils {
    public static void main(String[] args) {
        ShopSnap shopSnap = queryLatestShopLayoutIdByShopId(3405);
        System.out.println(shopSnap.getCreateTime());
    }

    public static ShopSnap queryLatestShopLayoutIdByShopId(Integer shopId) {
        String sql = "select layout_id,create_time from shop_snap  where type = 0 and  shop_id = " + shopId + " order by id desc limit 1";
        return DbaUtils.processAndGetOne(ShopSnap.class, sql, shopId);
    }
}
