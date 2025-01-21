package com.yjh.sql.service.impl;

import com.yjh.sql.dao.EmployeeDao;
import com.yjh.sql.entity.Employee;
import com.yjh.sql.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.List;

/**
 * @author osmondy
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeDao employeeDao;

    /**
     * jpa的第一种查询方式 根据命名规范来查询
     * @param name 姓名
     * @param password 密码
     * @return 单个员工或者null
     */
    @Override
    public Employee findByNameAndPassword(String name, String password) {
        return employeeDao.findByNameAndPassword(name, password);
    }
    /**
     * HQL 查询，说白了就是根据实体进行查询
     * @param name 姓名
     * @param password 密码
     * @return 单个员工或者null
     */
    @Override
    public Employee findQueryHql(String name, String password) {
        return employeeDao.findQueryHql(name,password);
    }
    /**
     * 原生sql进行查询
     * @param name 姓名
     * @param email 邮箱
     * @return 单个员工或者null
     */
    @Override
    public Employee findByQuery(String name, String email) {
        return employeeDao.findByQuery(name, email);
    }

    /**
     * 复杂查询的实现
     * @param page 页数 从0开始  从0开始 ，从0开始
     * @param size 每页的数据
     * @param id 模糊查询的参数
     * @return
     */
    @Override
    public Page<Employee> findAll(int page, int size, int id) {
        PageRequest pageable = PageRequest.of(page, size, Sort.Direction.DESC, "id");
        Page<Employee> all = employeeDao.findAll(pageable);
//        Page<Employee> all = employeeDao.findAll((Specification<Employee>) (root, query, cb) -> {
//            Predicate p1 = cb.like(root.get("id").as(String.class), "%" + id + "%");
//            return cb.and(p1);
//        }, pageable);
        return all;
    }

    /**
     * 保存员工
     * @param employee 员工实体
     * @return 保存后的员工
     */
    @Override
    public Employee saveEmp(Employee employee) {
        return employeeDao.save(employee);
    }

    /**
     * 修改员工
     * @param employee 员工实体
     * @return 修改后的员工
     */
    @Override
    public Employee updateEmp(Employee employee) {
        return employeeDao.save(employee);
    }

    /**
     * 调用jpa封装好的 查询所有方法
     * @return
     */
    @Override
    public List<Employee> findAll() {
        return employeeDao.findAll();
    }

}
