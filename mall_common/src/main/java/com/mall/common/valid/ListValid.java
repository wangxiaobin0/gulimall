package com.mall.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author
 * @date 2020/4/6
 */
@Documented
@Constraint(
        validatedBy = {ListValidator.class}
)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValid {

    /**
     * 提示信息
     * @return
     */
    String message() default "{com.mall.common.valid.ListValid.message}";

    /**
     * 值
     * @return
     */
    int[] value() default {};

    /**
     * 组
     * @return
     */
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
