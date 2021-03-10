package model;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-17 19:56
 **/
@Data
public class LayoutShelf {
    private Integer id;
    private Integer shelfId;
    private String shopCode;
    private String shelfName;
    private Integer shelfSizeType;
    private List<Integer> shelfMarkList;
    private Integer shelfMarkType;
}
