package model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Shop implements Serializable{


	private static final long serialVersionUID = 1819689517916030562L;

	private Integer id;

	private String name;

	private String code;

	private Integer type;

	/**
	 * 门店状态 0：无效， 1：有效， 2：删除
	 */
	private Integer state;

	private Integer businessState;


}