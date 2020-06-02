package com.mall.seckill.to;

import com.mall.seckill.vo.SeckillSkuRelationVo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 秒杀活动场次
 * 
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:35:51
 */
@Data
public class SeckillSessionTo implements Serializable {

	/**
	 * id
	 */
	private Long id;
	/**
	 * 场次名称
	 */
	private String name;
	/**
	 * 每日开始时间
	 */
	private Date startTime;
	/**
	 * 每日结束时间
	 */
	private Date endTime;
	/**
	 * 启用状态
	 */
	private Integer status;
	/**
	 * 创建时间
	 */
	private Date createTime;

	List<SeckillSkuRelationVo> relationSkus;
}
