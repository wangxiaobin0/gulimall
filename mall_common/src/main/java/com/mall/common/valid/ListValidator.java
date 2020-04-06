package com.mall.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author
 * @date 2020/4/6
 */
public class ListValidator implements ConstraintValidator<ListValid, Integer> {
    private Set<Integer> set;
    @Override
    public void initialize(ListValid constraintAnnotation) {
        set = new HashSet<Integer>();
        for (int i = 0; i < constraintAnnotation.value().length; i++) {
            set.add(constraintAnnotation.value()[i]);
        }
    }

    @Override
    public boolean isValid(Integer o, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(o);
    }
}
