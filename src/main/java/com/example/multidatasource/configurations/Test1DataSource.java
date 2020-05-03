package com.example.multidatasource.configurations;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

/**
 * @ClassName: Test1DataSource
 * @Author: ren
 * @Description:
 * @CreateTIme: 2019/7/17 0017 下午 1:55
 * @see EnableTransactionManagement
 * @see TransactionManagementConfigurer 效果等同于 EnableTransactionManagement,区别只是自己实现
 **/
//@EnableTransactionManagement
@MapperScan(basePackages = {
        "com.example.multidatasource.modules.db1.dao.jpas"
},
        sqlSessionTemplateRef = "test1SqlSessionTemplate",
        sqlSessionFactoryRef = "test1SqlSessionFactory")
@Configuration
public class Test1DataSource extends AbstractMybatisPlusConfiguration
//        implements TransactionManagementConfigurer
{

//    @Primary
    @Bean(name = "test1DruidDataSource")
    @ConfigurationProperties(prefix = "test1.datasource")
    public DruidDataSource druidDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = "test1MybatisPlusProperties")
    @ConfigurationProperties(prefix = "mybatis-plus.test1")
    public MybatisPlusProperties mybatisPlusProperties() {
        return new MybatisPlusProperties();
    }


    @Bean(name = "test1SqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(@Qualifier("test1DruidDataSource") DruidDataSource dataSource,
                                                   @Qualifier("test1MybatisPlusProperties") MybatisPlusProperties properties,
                                                   ResourceLoader resourceLoader,
                                                   ApplicationContext applicationContext) throws Exception {
        return getSqlSessionFactory(dataSource,
                properties,
                resourceLoader,
                null,
                null,
                applicationContext);
    }

    @Bean(name = "test1SqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("test1MybatisPlusProperties") MybatisPlusProperties properties,
                                                 @Qualifier("test1SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return getSqlSessionTemplate(sqlSessionFactory, properties);
    }

}
