package com.example.multidatasource.modules.db1.dao.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName: TestEntity1
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/5/3 0003 上午 11:11
 **/
@Data
@TableName("test_entity_1")
public class TestEntity1 {

    @TableId(type = IdType.ID_WORKER)
    private Long id;

    private String name;

    private Date createTime;
}
