package com.example.multidatasource.modules.db1.services;

import com.example.multidatasource.configurations.annonations.Test1Transactional;
import com.example.multidatasource.configurations.annonations.Test2Transactional;
import com.example.multidatasource.modules.db1.dao.entities.TestEntity1;
import com.example.multidatasource.modules.db1.dao.jpas.TestEntity1Mapper;
import com.example.multidatasource.modules.db2.dao.entities.TestEntity2;
import com.example.multidatasource.modules.db2.dao.jpas.TestEntity2Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @ClassName: Test1Service
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/3 0003 下午 4:09
 **/
@Service
public class Test1Service {

    @Autowired
    private TestEntity1Mapper testEntity1Mapper;

    /**
     *  MANDATORY: 没有检查到事务，抛出异常
     *
     *  REQUIRES_NEW：
     */
    //    @Test1Transactional
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void test() {

        TestEntity1 entity1 = new TestEntity1();
        entity1.setCreateTime(new Date());
        entity1.setName("测试2");

//        testEntity1Mapper.insert(entity1);
//        throw new RuntimeException("测试");
    }
}
