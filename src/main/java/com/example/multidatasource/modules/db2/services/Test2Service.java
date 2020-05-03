package com.example.multidatasource.modules.db2.services;

import com.example.multidatasource.configurations.annonations.Test1Transactional;
import com.example.multidatasource.modules.db2.dao.entities.TestEntity2;
import com.example.multidatasource.modules.db2.dao.jpas.TestEntity2Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @ClassName: Test1Service
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/3 0003 下午 4:09
 **/
@Service
public class Test2Service {

    @Autowired
    private TestEntity2Mapper testEntity2Mapper;

    @Test1Transactional
    public void test() {

        TestEntity2 entity2 = new TestEntity2();
        entity2.setCreateTime(new Date());
        entity2.setName("测试2");

        testEntity2Mapper.insert(entity2);
        throw new RuntimeException("测试");
    }
}
