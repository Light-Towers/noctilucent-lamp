package com.atguigu.path;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathDemo1 {

    public static void main(String[] args) {
        //创建path实例
        Path path = Paths.get("d:\\atguigu\\01.txt");

        //创建相对路径
        //代码1
        Path projects = Paths.get("d:\\atguigu", "projects");

        //代码2
        Path file = Paths.get("d:\\atguigu", "projects\\002.txt");


        String originalPath = "d:\\atguigu\\projects\\..\\yygh-project";

        Path path1 = Paths.get(originalPath);
        System.out.println("path1 = " + path1);

        Path path2 = path1.normalize();
        System.out.println("path2 = " + path2);

    }
}
