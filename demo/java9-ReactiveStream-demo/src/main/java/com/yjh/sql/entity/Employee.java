package com.yjh.sql.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * 雇员
 * @author yangjinhua
 */
@Data
@Table
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /**
     * 名称
     */
    @Column
    private String name;
    /**
     * 密码
     */
    @Column
    private String password;

    private String email;
}
