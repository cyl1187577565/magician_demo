package model;

import lombok.Data;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-16 20:36
 **/

@Data
public class TopicProportion {
    /**
     * 货架主题id
     */
    private Integer shelfTopicId;
    /**
     * 货架主题名称
     */
    private String shelfTopicName;
    /**
     * 货架主题比例
     */
    private double proportion;
}
