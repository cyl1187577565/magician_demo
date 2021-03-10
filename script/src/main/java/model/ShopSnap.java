package model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: yulong.cao
 * @since: 2021-01-13 22:35
 **/
@Data
public class ShopSnap {
    Integer layoutId;
    LocalDateTime createTime;
}
