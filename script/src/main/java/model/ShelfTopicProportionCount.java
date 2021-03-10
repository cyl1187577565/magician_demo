package model;

import lombok.Data;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-17 19:29
 **/

@Data
public class ShelfTopicProportionCount {
    private String shopCode;
    private String shopName;
    private String type;
    private String oneShelfTypeName;
    private String twoShelfTypeName;
    private String threeShelfTypeName;
    private String sectionTopicName;
    private String shelfTopicName;
    private Double shelfNum;

    public String key(){
        return oneShelfTypeName + twoShelfTypeName + threeShelfTypeName + sectionTopicName + shelfTopicName;
    }
}
