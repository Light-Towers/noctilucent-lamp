package com.yjh.java8;

/**
 * 外部定义变量
 *
 * @author osmondy
 * @create 2021/5/16 19:31
 */
public class LambdaTest2 {
    public static void main(String[] args) {

        // num 隐性的具有 final 的语义
        int num = 1;
        Converter<Integer, String> s = (param) -> System.out.println(String.valueOf(param + num));
        s.convert(2);  // 输出结果为 3

        // Lambda 表达式当中不允许声明一个与局部变量同名的参数或者局部变量
//        String first = "";
//        Comparator<String> comparator = (first, second) -> Integer.compare(first.length(), second.length());  //编译会出错
    }


    //    @FunctionalInterface  //函数式声明接口
    public interface Converter<T1, T2> {
        void convert(int i);
    }

}




