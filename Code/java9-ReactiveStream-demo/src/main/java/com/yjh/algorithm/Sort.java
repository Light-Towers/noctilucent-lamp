package com.yjh.algorithm;

import java.util.Arrays;

/**
 * 排序
 * @author osmondy
 */
public class Sort {

    /**
     * 计数排序
     * 范围：计数排序只能用在数据范围不大的场景中，如果数据范围 k 比要排序的数据 n 大很多，就不适合用计数排序了。
     * 而且，计数排序只能给非负整数排序，如果要排序的数据是其他类型的，要将其在不改变相对大小的情况下，转化为非负整数。
     * @param a 数组
     * @param n 数组大小
     */
    public static void countingSort(int[] a, int n) {
        if (n <= 1) {
            return;
        }

        // 查找数组中数据的范围
        int max = a[0];
        for (int i = 1; i < n; ++i) {
            if (max < a[i]) {
                max = a[i];
            }
        }

        // 申请一个计数数组 c，下标大小 [0,max]
        int[] c = new int[max + 1];
        for (int i = 0; i <= max; ++i) {
            c[i] = 0;
        }

        // 计算每个元素的个数，放入 c 中
        for (int i = 0; i < n; ++i) {
            c[a[i]]++;
        }

        // 依次累加
        for (int i = 1; i <= max; ++i) {
            c[i] = c[i-1] + c[i];
        }

        // 临时数组 r，存储排序之后的结果
        int[] r = new int[n];
        // 计算排序的关键步骤，有点难理解
        for (int i = n - 1; i >= 0; --i) {
            int index = c[a[i]]-1;
            r[index] = a[i];
            c[a[i]]--;
        }

        // 将结果拷贝给 a 数组
        for (int i = 0; i < n; ++i) {
            a[i] = r[i];
        }
    }

    public static void main(String[] args) {
        int[] a = {2,5,3,0,2,3,0,3};
        int n = a.length;

        countingSort(a, n);
        test(a, n);

        System.out.println(Arrays.toString(a));
    }

    /**
     * 自我实现
     *
     * @param a 数组
     * @param n 数组大小
     */
    static void test(int[] a, int n){
        if (n < 1){
            return;
        }
        //1、求出最大的值域(数据的范围)
        int max = a[0];
        for (int i = 0; i < n; i++) {
            if (max <= a[i]){
                max = a[i];
            }
        }

        //2、记录每个值的个数
        int[] b = new int[max + 1];
        for (int i = 0; i < n; i++) {
            b[a[i]] ++;
        }

        //3、对第二个数组进行 “顺序求和”
        for (int i = 1; i < b.length; i++) {
            b[i] = b[i-1] + b[i];
        }

        //4、临时c数组存放排序后的结构
        int[] c = new int[n];
        for (int i = n-1; i >= 0; i--) {
            int index = b[a[i]] - 1;
            c[index] = a[i];
            b[a[i]]--;
        }

        //5、将结果拷贝给 a 数组
        for (int i = 0; i < n; ++i) {
            a[i] = c[i];
        }

    }
}
