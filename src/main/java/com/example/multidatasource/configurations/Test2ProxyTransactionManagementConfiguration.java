package com.example.multidatasource.configurations;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;


/**
 * @ClassName: Test1ProxyTransactionManagementConfiguration
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/3 0003 下午 5:02
 * @see AbstractTransactionManagementConfiguration
 * @see ProxyTransactionManagementConfiguration
 **/
@Configuration
public class Test2ProxyTransactionManagementConfiguration
//        extends AbstractTransactionManagementConfiguration
//        implements ImportAware
{


    @Bean(name = "test2PlatformTransactionManager")
    public PlatformTransactionManager txManager(@Qualifier("test2DruidDataSource") DruidDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "test2BeanFactoryTransactionAttributeSourceAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
            @Qualifier("test2TransactionAttributeSource")  TransactionAttributeSource transactionAttributeSource,
            @Qualifier("test2TransactionInterceptor") TransactionInterceptor transactionInterceptor) {

        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource);
        advisor.setAdvice(transactionInterceptor);
        advisor.setOrder(100000);
        return advisor;
    }

    @Bean(name = "test2TransactionAttributeSource")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionAttributeSource transactionAttributeSource() {


        AnnotationTransactionAttributeSource attributeSource = new AnnotationTransactionAttributeSource();

        return attributeSource;
    }

    @Bean(name = "test2TransactionInterceptor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionInterceptor transactionInterceptor(
            @Qualifier("test2TransactionAttributeSource") TransactionAttributeSource transactionAttributeSource,
            @Qualifier("test2PlatformTransactionManager") PlatformTransactionManager txManager
    ) {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(transactionAttributeSource);
        interceptor.setTransactionManager(txManager);
        return interceptor;
    }


    @Bean(name = "test2TransactionalEventListenerFactory")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static TransactionalEventListenerFactory transactionalEventListenerFactory() {
        return new TransactionalEventListenerFactory();
    }

}
