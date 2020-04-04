package com.mall.product;

import com.mall.product.entity.BrandEntity;
import com.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author
 * @date 2020/4/4
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ProductTest {

    @Autowired
    BrandService brandService;

    @Test
    public void test1() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("Bad Apple");

        brandService.saveOrUpdate(brandEntity);
        List<BrandEntity> list = brandService.list();
        list.forEach((brand) -> {
            System.out.println(brand);
        });
        brandEntity.setFirstLetter("B");
        brandService.saveOrUpdate(brandEntity);

        brandService.list().forEach((brand) -> {
            System.out.println(brand);
            brandService.removeById(brand.getBrandId());
        });

    }
}
