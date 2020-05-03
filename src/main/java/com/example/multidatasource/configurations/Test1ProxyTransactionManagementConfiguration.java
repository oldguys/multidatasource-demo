package com.example.multidatasource.configurations;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration;
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
 *
 **/
@Configuration
public class Test1ProxyTransactionManagementConfiguration
//        extends AbstractTransactionManagementConfiguration
//        implements ImportAware
{

    @Primary
    @Bean(name = "test1PlatformTransactionManager")
    public PlatformTransactionManager txManager(@Qualifier("test1DruidDataSource") DruidDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "test1BeanFactoryTransactionAttributeSourceAdvisor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
            @Qualifier("test1TransactionAttributeSource")  TransactionAttributeSource transactionAttributeSource,
            @Qualifier("test1TransactionInterceptor") TransactionInterceptor transactionInterceptor) {

        BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setTransactionAttributeSource(transactionAttributeSource);
        advisor.setAdvice(transactionInterceptor);
        advisor.setOrder(100000);
        return advisor;
    }

    @Bean(name = "test1TransactionAttributeSource")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionAttributeSource transactionAttributeSource() {
        return new AnnotationTransactionAttributeSource();
    }

    @Bean(name = "test1TransactionInterceptor")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionInterceptor transactionInterceptor(
            @Qualifier("test1TransactionAttributeSource") TransactionAttributeSource transactionAttributeSource,
            @Qualifier("test1PlatformTransactionManager") PlatformTransactionManager txManager
    ) {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionAttributeSource(transactionAttributeSource);
        interceptor.setTransactionManager(txManager);
        return interceptor;
    }


    @Bean(name = "test1TransactionalEventListenerFactory")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public static TransactionalEventListenerFactory transactionalEventListenerFactory() {
        return new TransactionalEventListenerFactory();
    }

}
