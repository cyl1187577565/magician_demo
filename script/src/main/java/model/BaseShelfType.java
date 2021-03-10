package model;

import lombok.Data;


/**
 * @author yanfang.xin
 */
@Data
public class BaseShelfType {

	private Integer id;

	private Integer parentId;

	private Integer type;

	private String name;

	private Integer width;

	private Integer height;

	private Integer depth;

	private String cityIds;

	private Boolean isFixedLevel;

	private Integer fixedLevels;

	private String levelsInfo;

	private Boolean isStructRate;

	private String structRateCitys;

	private Boolean isAutoDisplay;

	private String autoDisplayCitys;

	private Integer displayHeight;

	private String utensilCode;

	private String displayWay;

	private Integer levelThickness;

	private Integer holeSpace;

	private Boolean isVirtual;

	private Boolean haveNoAdjustLevel;

	private Integer noAdjustLevelTop;

	private Integer noAdjustLevelPly;

	private Integer maxBuffer;

	private Integer noHoleSpaceTop;

	private Integer noHoleSpaceBottom;

	private Integer fixLevelBegin;

	private Integer fixLevelEnd;

	private Boolean capping;

	private Integer status;
}