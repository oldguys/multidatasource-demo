package com.example.multidatasource.modules.test.services;

import com.example.multidatasource.modules.db1.services.Test1Service;
import com.example.multidatasource.modules.db2.services.Test2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName: TestService
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/3 0003 下午 4:10
 **/
@Service
public class TestService {

    @Autowired
    private Test1Service test1Service;

    @Autowired
    private Test2Service test2Service;

    @Transactional(rollbackFor = Exception.class)
    public void test(){
        test1Service.test();
        test2Service.test();
//        throw new RuntimeException("测试");
    }
}
