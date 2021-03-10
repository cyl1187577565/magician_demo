package model;

import lombok.Data;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2020-12-17 19:37
 **/

@Data
public class BaseTopic {
    private Integer id;
    private Integer parentId;
    private Integer type;
    private String name;
}
