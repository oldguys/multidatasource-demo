package com.example.multidatasource.services;

import com.example.multidatasource.modules.test.services.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName: TestServiceTests
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/3 0003 下午 4:13
 **/
@SpringBootTest
public class TestServiceTests {

    @Autowired
    private TestService testService;

    @Test
    public void test(){
        testService.test();
    }
}
