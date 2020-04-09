package com.mall.ware.vo;

import lombok.Data;
import org.apache.catalina.LifecycleState;

import java.util.List;

/**
 * @author
 * @date 2020/4/7
 */
@Data
public class MergeVo {
    private Long purchaseId;
    private List<Long> items;
}
