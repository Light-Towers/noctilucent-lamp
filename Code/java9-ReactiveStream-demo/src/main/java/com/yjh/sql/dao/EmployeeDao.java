package com.yjh.sql.dao;

import com.yjh.sql.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author osmondy
 */
@Repository
public interface EmployeeDao extends JpaRepository<Employee,String> {
    /**
     * jpa的第一种查询方式 根据命名规范来查询
     * @param name 姓名
     * @param password 密码
     * @return 单个员工或者null
     */
    Employee findByNameAndPassword(String name,String password);

    /**
     * HQL 查询，说白了就是根据实体进行查询
     * @param name 姓名
     * @param password 密码
     * @return 单个员工或者null
     */
    @Query(value = "select e from Employee e where name =?1 and password=?2")
    Employee findQueryHql(String name,String password);

    /**
     * 原生sql进行查询
     * @param name 姓名
     * @param email 邮箱
     * @return 单个员工或者null
     */
    @Query(value = "select * from employee  where name =?1 and email=?2",nativeQuery =true)
    Employee findByQuery(String name, String email);

    /**
     * 复杂查询
     * @param spec 拼接的条件语句 如果有很复杂的语句比如 select * from a where a.name ='' ,
     *             a.password ='' or a.name ='' or a.name ='' or a.name in  ('','')
     * @param pageable 分页加排序 Pageable已经将这些事情做好了。
     * @return Page 形式的员工列表
     */
    Page<Employee> findAll(Specification<Employee> spec, Pageable pageable);
}
