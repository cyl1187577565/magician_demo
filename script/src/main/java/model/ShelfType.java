package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoqy
 * @since 07 十一月 2018
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShelfType {
    private Integer id;
    /**
     * 父节点id
     */
    private Integer parentId;
    /**
     * 货架类型(1:一级货架类型 2:二级货架类型 3:三级货架类型)
     */
    private Integer type;
    /**
     * 货架类型名称(尺寸级别货架名称)
     */
    private String name;
}
