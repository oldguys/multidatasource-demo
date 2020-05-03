#### springboot+mybatis-plus 多容器时,解决事务问题无法传递问题


> 背景：在基于SpringBoot 项目进行开发时候，配置事务的操作特别容易，基本使用注解：org.springframework.transaction.annotation.EnableTransactionManagement 就行了。但当系统配置多个不同数据源的时候，这里配置就部分失效。本章基于这个话题，对于 spring 事务之间的 依赖关系，与此问题的解决方法进行讲解
> 
> 版本：
> SpringBoot 2.2.6.RELEASE
> Mybatis-Plus 3.2.0
> 
>

主要分3部分内容：
1. 复习下如何将 Mybatis-plus 改造成多数据源
2. 对 @EnableTransactionManagement 相关机制 及原理 进行分析
3. 对事务进行改造，使其兼容多数据源，并且满足 spring 事务传播特性



#### 前置知识：复习下如何将 Mybatis-plus 改造成多数据源。

1. 按照 SpringBoot 项目的日常操作，找到 Auto 配置类：**com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration** 然后对其进行 抽象成多数据源 需要的  通用类：AbstractMybatisPlusConfiguration。
2. 模仿 MybatisPlusAutoConfiguration  编写特定数据源相关类，主要有以下：

**com.alibaba.druid.pool.DruidDataSource**：数据源
**com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties**：数据配置
**org.apache.ibatis.session.SqlSessionFactory**：mybatis 工厂
**org.mybatis.spring.SqlSessionTemplate**：mybatis 处理模板

将变量进行适当修改之后，注意修改 @MapperScan 的默认参数，使其兼容

3. 模拟 mybatis-plus 在 yml 中编写 配置变量 进行变量修改
4. 剔除SpringBootApplication中一些自动装配

~~~
package com.example.multidatasource;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @ClassName: MultiDatasourceDemoApplication
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/3 0003 上午 11:16
 **/
@SpringBootApplication(   exclude = {
        DataSourceAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class,
})
public class MultiDatasourceDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceDemoApplication.class, args);
    }

}

~~~

application.yml
~~~
test1:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/test-db1?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: root
    driver-class-name: com.mysql.jdbc.Driver
test2:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/test-db2?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: root
    driver-class-name: com.mysql.jdbc.Driver

mybatis-plus:
  test1:
#    config-location: classpath:configs/myBatis-config.xml
    mapper-locations: classpath:mappers/test1/**/*.xml
    type-aliases-package: com.example.multidatasource.modules.db1.dao.entities
    global-config:
      banner: false
  test2:
#    config-location: classpath:configs/myBatis-config.xml
    mapper-locations: classpath:mappers/test2/**/*.xml
    type-aliases-package: com.example.multidatasource.modules.db2.dao.entities
    global-config:
      banner: false


~~~

#### AbstractMybatisPlusConfiguration
~~~
package com.example.multidatasource.configurations;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * @ClassName: AbstractMybatisPlusConfiguration
 * @Author: ren
 * @Description:
 * @CreateTIme: 2019/6/18 0018 上午 11:02
 * @see MybatisPlusAutoConfiguration
 **/
public class AbstractMybatisPlusConfiguration {

    protected SqlSessionFactory getSqlSessionFactory(
            DataSource dataSource,
            MybatisPlusProperties properties,
            ResourceLoader resourceLoader,
            Interceptor[] interceptors,
            DatabaseIdProvider databaseIdProvider,
            ApplicationContext applicationContext
    ) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(properties.getConfigLocation())) {
            factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        }
        applyConfiguration(factory, properties);
        if (properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(properties.getConfigurationProperties());
        }
        if (!ObjectUtils.isEmpty(interceptors)) {
            factory.setPlugins(interceptors);
        }
        if (databaseIdProvider != null) {
            factory.setDatabaseIdProvider(databaseIdProvider);
        }
        if (StringUtils.hasLength(properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }
        // TODO 自定义枚举包
        if (StringUtils.hasLength(properties.getTypeEnumsPackage())) {
            factory.setTypeEnumsPackage(properties.getTypeEnumsPackage());
        }
        if (properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(properties.getTypeAliasesSuperType());
        }
        if (StringUtils.hasLength(properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(properties.resolveMapperLocations())) {
            factory.setMapperLocations(properties.resolveMapperLocations());
        }
        // TODO 此处必为非 NULL
        GlobalConfig globalConfig = properties.getGlobalConfig();
        //注入填充器
        if (applicationContext.getBeanNamesForType(MetaObjectHandler.class,
                false, false).length > 0) {
            MetaObjectHandler metaObjectHandler = applicationContext.getBean(MetaObjectHandler.class);
            globalConfig.setMetaObjectHandler(metaObjectHandler);
        }
        //注入主键生成器
        if (applicationContext.getBeanNamesForType(IKeyGenerator.class, false,
                false).length > 0) {
            IKeyGenerator keyGenerator = applicationContext.getBean(IKeyGenerator.class);
            globalConfig.getDbConfig().setKeyGenerator(keyGenerator);
        }
        //注入sql注入器
        if (applicationContext.getBeanNamesForType(ISqlInjector.class, false,
                false).length > 0) {
            ISqlInjector iSqlInjector = applicationContext.getBean(ISqlInjector.class);
            globalConfig.setSqlInjector(iSqlInjector);
        }
        factory.setGlobalConfig(globalConfig);
        return factory.getObject();
    }

    private void applyConfiguration(MybatisSqlSessionFactoryBean factory, MybatisPlusProperties properties) {
        MybatisConfiguration configuration = properties.getConfiguration();
        if (configuration == null && !StringUtils.hasText(properties.getConfigLocation())) {
            configuration = new MybatisConfiguration();
        }
//        if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
//            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
//                customizer.customize(configuration);
//            }
//        }
        factory.setConfiguration(configuration);
    }


    public SqlSessionTemplate getSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, MybatisPlusProperties properties) {
        ExecutorType executorType = properties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

}
~~~
##### DruidDataSource 
~~~
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

~~~
##### Test2DataSource 
~~~
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
 **/
//@EnableTransactionManagement
@MapperScan(basePackages = {
        "com.example.multidatasource.modules.db2.dao.jpas"
},
        sqlSessionTemplateRef = "test2SqlSessionTemplate",
        sqlSessionFactoryRef = "test2SqlSessionFactory")
@Configuration
public class Test2DataSource extends AbstractMybatisPlusConfiguration
//        implements TransactionManagementConfigurer
{

    /**
     *  @Primary ：会使用默认事务
     * @return
     */
//    @Primary
    @Bean(name = "test2DruidDataSource")
    @ConfigurationProperties(prefix = "test2.datasource")
    public DruidDataSource druidDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = "test2MybatisPlusProperties")
    @ConfigurationProperties(prefix = "mybatis-plus.test2")
    public MybatisPlusProperties mybatisPlusProperties() {
        return new MybatisPlusProperties();
    }


    @Bean(name = "test2SqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(@Qualifier("test2DruidDataSource") DruidDataSource dataSource,
                                                   @Qualifier("test2MybatisPlusProperties") MybatisPlusProperties properties,
                                                   ResourceLoader resourceLoader,
                                                   ApplicationContext applicationContext) throws Exception {
        return getSqlSessionFactory(dataSource,
                properties,
                resourceLoader,
                null,
                null,
                applicationContext);
    }

    @Bean(name = "test2SqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("test2MybatisPlusProperties") MybatisPlusProperties properties,
                                                 @Qualifier("test2SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return getSqlSessionTemplate(sqlSessionFactory, properties);
    }


}

~~~

--- 

#### @EnableTransactionManagement 的相关原理及 SpringBoot 这一块的源码分析。

SpringBoot事务的两个主要注解：
org.springframework.transaction.annotation.Transactional
org.springframework.transaction.annotation.EnableTransactionManagement

所以就从这2个注解开始入手。
打开@EnableTransactionManagement 源码，可以看到几个东西：
![image.png](https://upload-images.jianshu.io/upload_images/14387783-b02f2932139cf163.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

~~~
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableTransactionManagement
 * public class AppConfig implements TransactionManagementConfigurer {
 *
 *     &#064;Bean
 *     public FooRepository fooRepository() {
 *         // configure and return a class having &#064;Transactional methods
 *         return new JdbcFooRepository(dataSource());
 *     }
 *
 *     &#064;Bean
 *     public DataSource dataSource() {
 *         // configure and return the necessary JDBC DataSource
 *     }
 *
 *     &#064;Bean
 *     public PlatformTransactionManager txManager() {
 *         return new DataSourceTransactionManager(dataSource());
 *     }
 *
 *     &#064;Override
 *     public PlatformTransactionManager annotationDrivenTransactionManager() {
 *         return txManager();
 *     }
 * }</pre>
~~~
>接口：org.springframework.transaction.annotation.TransactionManagementConfigurer
>实现这个类，并且重置事务，
注解：org.springframework.context.annotation.Primary 在多数据源的时候 起到默认数据源作用
我们事务管理是基于这个类：org.springframework.transaction.PlatformTransactionManager
>
>可以找到我们Mybatis实现事务管理的类 org.springframework.jdbc.datasource.DataSourceTransactionManager。

管理核心有了，怎么接下来是怎么将 事务绑定，根据我们对 Spring 的理解，基本上所有的 操作都是基于 AOP ，也就是代理，所以在 **@EnableTransactionManagement** 看到关键字：**org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration**，看下里面有啥东东。


~~~
/**
 * {@code @Configuration} class that registers the Spring infrastructure beans
 * necessary to enable proxy-based annotation-driven transaction management.
 *
 * @author Chris Beams
 * @author Sebastien Deleuze
 * @since 3.1
 * @see EnableTransactionManagement
 * @see TransactionManagementConfigurationSelector
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProxyTransactionManagementConfiguration extends AbstractTransactionManagementConfiguration {

~~~
所以明白了，我们最重要的 AOP代理类找到了。接下来翻一下这里类里面有什么东西。

从**org.springframework.transaction.annotation.AnnotationTransactionAttributeSource** 翻到了点重要的东西。

~~~

/**
 * Implementation of the
 * {@link org.springframework.transaction.interceptor.TransactionAttributeSource}
 * interface for working with transaction metadata in JDK 1.5+ annotation format.
 *
 * <p>This class reads Spring's JDK 1.5+ {@link Transactional} annotation and
 * exposes corresponding transaction attributes to Spring's transaction infrastructure.
 * Also supports JTA 1.2's {@link javax.transaction.Transactional} and EJB3's
 * {@link javax.ejb.TransactionAttribute} annotation (if present).
 * This class may also serve as base class for a custom TransactionAttributeSource,
 * or get customized through {@link TransactionAnnotationParser} strategies.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 1.2
 * @see Transactional
 * @see TransactionAnnotationParser
 * @see SpringTransactionAnnotationParser
 * @see Ejb3TransactionAnnotationParser
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#setTransactionAttributeSource
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean#setTransactionAttributeSource
 */
@SuppressWarnings("serial")
public class AnnotationTransactionAttributeSource extends AbstractFallbackTransactionAttributeSource
		implements Serializable {

~~~

跟 @Transactional 串起来了。


接下来是 另一个重要的东西 **@Transactional**，同理，从源码里面可以看到我们想要的东西，并且有些东西似曾相识

~~~
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

	/**
	 * Alias for {@link #transactionManager}.
	 * @see #transactionManager
	 */
	@AliasFor("transactionManager")
	String value() default "";

	/**
	 * A <em>qualifier</em> value for the specified transaction.
	 * <p>May be used to determine the target transaction manager,
	 * matching the qualifier value (or the bean name) of a specific
	 * {@link org.springframework.transaction.PlatformTransactionManager}
	 * bean definition.
	 * @since 4.2
	 * @see #value
	 */
	@AliasFor("value")
	String transactionManager() default "";
~~~

从属性：transactionManager 可以看到，这个是根据容器bean 关联的，基本所有需要的东西都有了。

> 关系：
> 围绕 TransactionManager ，Spring利用 代理，扫描 @Transactional 相关类，并 根据 @Transactional.value() 与 TransactionManager  进行绑定。所以我们要做多数据源，只需要模拟这个过程。
>
>

---

从  **ProxyTransactionManagementConfiguration** copy 一份，整理成为：Test1ProxyTransactionManagementConfiguration

##### Test1ProxyTransactionManagementConfiguration
~~~
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
~~~

##### 同理编写 Test2ProxyTransactionManagementConfiguration
~~~
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

~~~

使用 @Transactional(value = "test1PlatformTransactionManager", rollbackFor = Exception.class) 在特定方法上，就可以做到指定数据源事务。
> PS: 必须在 其中一个TransactionManager 配置 @primary 注解，不然会包 多Bean 冲突 异常。
>

为了方便平时的使用，这里可以使用自定义注解：
~~~
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Transactional(value = "test1PlatformTransactionManager", rollbackFor = Exception.class)
public @interface Test1Transactional {
}


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Transactional(value = "test2PlatformTransactionManager", rollbackFor = Exception.class)
public @interface Test2Transactional {
}
~~~

利用 注解的继承 特性，避免一些普通编码的误差。

----

测试例子

~~~
@Service
public class Test1Service {

    @Autowired
    private TestEntity1Mapper testEntity1Mapper;

    @Test1Transactional
    public void test() {

        TestEntity1 entity1 = new TestEntity1();
        entity1.setCreateTime(new Date());
        entity1.setName("测试2");

        testEntity1Mapper.insert(entity1);
//        throw new RuntimeException("测试");
    }
}


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
//        throw new RuntimeException("测试");
    }
}

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
        throw new RuntimeException("测试");
    }
}
~~~
调用
~~~
@SpringBootTest
public class TestServiceTests {

    @Autowired
    private TestService testService;

    @Test
    public void test(){
        testService.test();
    }
}

~~~

结果图：
没有异常的时候：
![没有异常的时候](https://upload-images.jianshu.io/upload_images/14387783-666aebde12410276.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在三个类中任意一个出现异常：
![抛异常的时候](https://upload-images.jianshu.io/upload_images/14387783-604bd879d07a984d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![数据库结果](https://upload-images.jianshu.io/upload_images/14387783-3194f85dc44bd70f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

----
以上完成了文章的编写
