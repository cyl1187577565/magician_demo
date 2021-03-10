package dbMgr;

import model.BaseTopic;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-17 19:36
 **/
public class BaseTopicUtils {
    public static void main(String[] args) {
        List<BaseTopic> baseTopics = queryAll();
        System.out.println(baseTopics.size());
    }


    /**
     * 查询所有
     * @return
     */
    public static List<BaseTopic> queryAll() {
        String sql = "select * from base_topic where id > %d order by id asc limit 100";

        List<BaseTopic> result = new ArrayList<>();
        int curId = 0;
        while (true){
            List<BaseTopic> baseTopics = DbaUtils.processSql(BaseTopic.class, sql, curId);
            result.addAll(baseTopics);
            if(baseTopics.size() < 100){
                break;
            }

            curId = baseTopics.get(baseTopics.size() - 1).getId();
        }

        return result;
    }
}
