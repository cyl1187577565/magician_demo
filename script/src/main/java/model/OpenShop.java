package model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class OpenShop implements Serializable {
    private static final long serialVersionUID = 0L;

    /**
     * shopCode
     */
    private String shopCode;

    /**
     * 门店名称
     */
    private String shopName;

    /**
     *  门店所在城市编码
     */
    private String cityCode;

    /**
     * 门店所在城市名称
     */
    private String cityName;

    /**
     * 营业状态
     */
    private Integer businessState;

    /**
     * 是否是陈列管理
     */
    private Boolean isDisplayManage;

    /**
     * 开业时间
     */
    private Date openTime;

}