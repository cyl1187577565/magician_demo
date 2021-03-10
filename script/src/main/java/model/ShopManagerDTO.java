package model;

import lombok.Data;

import java.util.Date;

@Data
public class ShopManagerDTO {
    private Integer id;

    private String userName;

    private String jobNumber;

    private String userCode;

    private Date createTime;
    private Date ts;

}