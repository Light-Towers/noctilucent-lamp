package com.yjh.sql.vo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import com.yjh.sql.util.QueryCondition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author osmondy
 */
public abstract class BaseQuery<T> {
    /**
     * start from 0
     */
    protected int pageIndex = 0;
    protected int pageSize = 10;


    /**
     * 将查询转换成Specification
     * @return
     */
    public abstract Specification<T> toSpec();

    //JPA分页查询类
    public Pageable toPageable() {
        return PageRequest.of(pageIndex, pageSize);
    }

    //JPA分页查询类,带排序条件
    public Pageable toPageable(Sort sort) {
        return PageRequest.of(pageIndex, pageSize, sort);
    }

    //动态查询and连接
    protected Specification<T> toSpecWithAnd() {
        return this.toSpecWithLogicType("and");
    }

    //动态查询or连接
    protected Specification<T> toSpecWithOr() {
        return this.toSpecWithLogicType("or");
    }

    //logicType or/and
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Specification<T> toSpecWithLogicType(String logicType) {
        BaseQuery outerThis = this;
        return (root, criteriaQuery, cb) -> {
            Class clazz = outerThis.getClass();
            //获取查询类Query的所有字段,包括父类字段
            List<Field> fields = getAllFieldsWithRoot(clazz);
            List<Predicate> predicates = new ArrayList<>(fields.size());
            for (Field field : fields) {
                //获取字段上的@QueryWord注解
                QueryCondition qw = field.getAnnotation(QueryCondition.class);
                if (qw == null){
                    continue;
                }

                // 获取字段名
                String column = qw.column();
                //如果主注解上colume为默认值"",则以field为准
                if (column.equals("")){
                    column = field.getName();
                }

                field.setAccessible(true);

                try {

                    // nullable
                    Object value = field.get(outerThis);
                    //如果值为null,注解未标注nullable,跳过
                    if (value == null && !qw.nullable()){
                        continue;
                    }

                    // can be empty
                    if (value != null && String.class.isAssignableFrom(value.getClass())) {
                        String s = (String) value;
                        //如果值为"",且注解未标注emptyable,跳过
                        if (s.equals("") && !qw.emptyable()){
                            continue;
                        }
                    }

                    //通过注解上func属性,构建路径表达式
                    Path path = root.get(column);
                    switch (qw.func()) {
                        case equal:
                            predicates.add(cb.equal(path, value));
                            break;
                        case like:
                            predicates.add(cb.like(path, "%" + value + "%"));
                            break;
                        case gt:
                            predicates.add(cb.gt(path, (Number) value));
                            break;
                        case lt:
                            predicates.add(cb.lt(path, (Number) value));
                            break;
                        case ge:
                            predicates.add(cb.ge(path, (Number) value));
                            break;
                        case le:
                            predicates.add(cb.le(path, (Number) value));
                            break;
                        case notEqual:
                            predicates.add(cb.notEqual(path, value));
                            break;
                        case notLike:
                            predicates.add(cb.notLike(path, "%" + value + "%"));
                            break;
                        case greaterThan:
                            predicates.add(cb.greaterThan(path, (Comparable) value));
                            break;
                        case greaterThanOrEqualTo:
                            predicates.add(cb.greaterThanOrEqualTo(path, (Comparable) value));
                            break;
                        case lessThan:
                            predicates.add(cb.lessThan(path, (Comparable) value));
                            break;
                        case lessThanOrEqualTo:
                            predicates.add(cb.lessThanOrEqualTo(path, (Comparable) value));
                            break;
                        default:
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            Predicate p = null;
            if (logicType == null || logicType.equals("") || logicType.equals("and")) {
                p = cb.and(predicates.toArray(new Predicate[predicates.size()]));//and连接
            } else if (logicType.equals("or")) {
                p = cb.or(predicates.toArray(new Predicate[predicates.size()]));//or连接
            }
            return p;
        };
    }

    //获取类clazz的所有Field，包括其父类的Field
    private List<Field> getAllFieldsWithRoot(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        Field[] dFields = clazz.getDeclaredFields();//获取本类所有字段
        if (null != dFields && dFields.length > 0){
            fieldList.addAll(Arrays.asList(dFields));
        }

        // 若父类是Object，则直接返回当前Field列表
        Class<?> superClass = clazz.getSuperclass();
        if (superClass == Object.class){
            return Arrays.asList(dFields);
        }

        // 递归查询父类的field列表
        List<Field> superFields = getAllFieldsWithRoot(superClass);

        if (null != superFields && !superFields.isEmpty()) {
            superFields.stream().
                    filter(field -> !fieldList.contains(field)).//不重复字段
                    forEach(field -> fieldList.add(field));
        }
        return fieldList;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
