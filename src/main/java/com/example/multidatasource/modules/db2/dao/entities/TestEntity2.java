package com.example.multidatasource.modules.db2.dao.entities;

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
@TableName("test_entity_2")
@Data
public class TestEntity2 {

    @TableId(type = IdType.ID_WORKER)
    private Long id;

    private String name;

    private Date createTime;
}
