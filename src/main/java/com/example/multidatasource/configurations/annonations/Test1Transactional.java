package com.example.multidatasource.configurations.annonations;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * @ClassName: Test1Transactional
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/3 0003 下午 5:39
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Transactional(value = "test1PlatformTransactionManager", rollbackFor = Exception.class)
public @interface Test1Transactional {
}
