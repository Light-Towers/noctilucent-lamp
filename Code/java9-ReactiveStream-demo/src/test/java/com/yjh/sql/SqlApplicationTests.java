package com.yjh.sql;

import com.yjh.sql.entity.Employee;
import com.yjh.sql.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={SqlApplication.class})// 指定启动类
class SqlApplicationTests {
    @Autowired
    EmployeeService employeeService;

    @Test
    void findTest() {
//        Employee queryHql = employeeService.findQueryHql("杨进华", "1111");
        Page<Employee> all = employeeService.findAll(1, 5, 1);
        System.out.println(all);
    }

}
