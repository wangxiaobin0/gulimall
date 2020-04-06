package com.mall.product.exception;

import com.mall.common.exception.ValidExceptionEnum;
import com.mall.common.utils.R;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @date 2020/4/6
 */
@RestControllerAdvice
public class ValidExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R validHandler(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        List<FieldError> errorList = bindingResult.getFieldErrors();
        Map<String, String> map = new LinkedHashMap<>();
        errorList.forEach((error -> {
            map.put(error.getField(), error.getDefaultMessage());
        }));
        return R.error(ValidExceptionEnum.VALID_EXCEPTION.getCode(), ValidExceptionEnum.VALID_EXCEPTION.getMsg()).put("data", map);
    }

    @ExceptionHandler(Exception.class)
    public R exceptionHandler(Exception e) {
        e.printStackTrace();
        return R.error(ValidExceptionEnum.UNKNOWN_EXCEPTION.getCode(), ValidExceptionEnum.UNKNOWN_EXCEPTION.getMsg());
    }
}
