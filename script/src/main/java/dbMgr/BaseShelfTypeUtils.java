package dbMgr;

import model.ShelfType;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-16 21:18
 **/
public class BaseShelfTypeUtils {
    public static void main(String[] args) {
        List<ShelfType> shelfTypes = queryAll();
        System.out.println(shelfTypes.size());
    }

    public static List<ShelfType> queryAll(){
        String sql = "select * from base_shelf_type where id > %d order by id asc limit 100";

        List<ShelfType> result = new ArrayList<>();
        Integer curId = 0;
        while (true){
            List<ShelfType> shelfTypes = DbaUtils.processSql(ShelfType.class, sql, curId);
            result.addAll(shelfTypes);
            if(shelfTypes.size() < 100){
                break;
            }
            curId = shelfTypes.get(shelfTypes.size() -1).getId();
        }

        return result;
    }
}
