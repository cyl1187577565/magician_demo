package dbMgr;

import model.LayoutShelf;

import java.util.List;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-17 19:56
 **/
public class LayoutShelfUtils {
    /**
     * 获取最新的布局图
     */
    private static final String queryShopLayoutUrl = "/chenlie/api/shop/layout/query/v2";

    public static void main(String[] args) {
        LayoutShelf layoutShelf = queryById(28805095);
        System.out.println(layoutShelf);
    }


    public static LayoutShelf queryById(Integer id) {
        String sql = "select id , shelf_size_type,shelf_mark_list, shelf_mark_type from layout_shelf where id = " + id;
        LayoutShelf layoutShelf = DbaUtils.processAndGetOne(LayoutShelf.class, sql);
        return layoutShelf;
    }

    /**
     * 根据布局图id查询物理货架
     * @param shopLayoutId
     * @return
     */
    public static List<LayoutShelf> listByShopLayoutId(Integer shopLayoutId){
        String sql = "select id,shop_code,shelf_id, shelf_name, shelf_mark_list, shelf_size_type from layout_shelf where shop_layout_id = " + shopLayoutId;
        List<LayoutShelf> layoutShelves = DbaUtils.processSql(LayoutShelf.class, sql);
        return layoutShelves;
    }


}
