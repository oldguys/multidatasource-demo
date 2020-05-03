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
