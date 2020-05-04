package com.example.multidatasource.modules.test.services;

import com.example.multidatasource.modules.db1.services.Test1Service;
import com.example.multidatasource.modules.db2.services.Test2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    /**
     *  SUPPORTS: 支持当前事务，如果不存在就不适用于事务。内部事务不被外部事务影响：即 test1 完成 。test2 或 test 抛出异常，test1 不回滚
     *
     *  REQUIRED：支持当前事务，不存在就新建。包含子事务，任何一个抛异常都无法新建：即 test1 test2 test 任意抛出异常，都会导致回滚
     *
     *  MANDATORY：支持当前事务，不存在则抛异常。即：test 抛异常，其他正常执行
     *
     * ---
     *  REQUIRES_NEW：如果有事务存在，挂起当前事务，创建一个新的事务。即 test1 test2 test 任意抛出异常，都会导致回滚
     *
     *  NOT_SUPPORTED：以非事务的方式运行，如果有事务存在，挂起当前事务。test2 抛出异常，test1 没有回滚
     *
     *  NEVER: 以非事务方式卞，如果存在事务，抛出异常。 即 test1 test2 test 任意抛出异常，都会导致回滚
     *
     * ---
     *
     *
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void test() {
        test1Service.test();
        test2Service.test();
        throw new RuntimeException("测试");
    }

}
