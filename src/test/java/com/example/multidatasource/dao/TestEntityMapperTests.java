package com.example.multidatasource.dao;

import com.example.multidatasource.modules.db1.dao.entities.TestEntity1;
import com.example.multidatasource.modules.db1.dao.jpas.TestEntity1Mapper;
import com.example.multidatasource.modules.db2.dao.entities.TestEntity2;
import com.example.multidatasource.modules.db2.dao.jpas.TestEntity2Mapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class TestEntityMapperTests {

    @Autowired
    private TestEntity2Mapper testEntity2Mapper;
    @Autowired
    private TestEntity1Mapper testEntity1Mapper;

    @Test
    void test() {

        TestEntity1 entity = new TestEntity1();
        entity.setName("测试1");
        entity.setCreateTime(new Date());

        testEntity1Mapper.insert(entity);

        TestEntity2 entity2 = new TestEntity2();
        entity2.setName("测试2");
        entity2.setCreateTime(new Date());

        testEntity2Mapper.insert(entity2);

    }

}
