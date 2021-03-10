package dbMgr;

import model.Level;

import java.util.List;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2021-01-20 19:48
 **/
public class LevelUtils {

    /**
     *
     * @param shelfId
     * @return
     */
    public static List<Level> listByShelfId(Integer shelfId){
        String sql = "select id, use_id from level where shelf_id = " + shelfId;
        List<Level> levels = DbaUtils.processSql(Level.class, sql);
        return levels;
    }
}
