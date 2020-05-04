package com.example.multidatasource.services;

import com.example.multidatasource.modules.db1.services.Test1Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName: Test1ServiceTests
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/4 0004 上午 8:01
 **/
@SpringBootTest
public class Test1ServiceTests {

    @Autowired
    private Test1Service test1Service;

    @Test
    public void test(){

        test1Service.test();
    }
}
