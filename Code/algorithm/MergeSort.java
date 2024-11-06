/**
 * MergeSort类实现了归并排序算法，用于对整数数组进行排序
 */
public class MergeSort {
    /**
     * 对外提供的归并排序方法，用户通过调用此方法对数组进行排序
     * 
     * @param arr 待排序的整数数组
     */
    public static void mergeSort(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        mergeSort(arr, 0, arr.length - 1);
    }

    /**
     * 归并排序的递归方法，用于将数组划分为更小的部分
     * 
     * @param arr 待排序的整数数组
     * @param left 数组划分的左边界
     * @param right 数组划分的右边界
     */
    private static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    /**
     * 合并两个有序数组的方法，将两个有序的子数组合并成一个有序的整体
     * 
     * @param arr 待排序的整数数组
     * @param left 左侧子数组的起始位置
     * @param mid 左侧子数组的结束位置，也是右侧子数组起始位置的前一位
     * @param right 右侧子数组的结束位置
     */
    private static void merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;

        while (i <= mid && j <= right) {
            if (arr[i] <= arr[j]) {
                temp[k++] = arr[i++];
            } else {
                temp[k++] = arr[j++];
            }
        }

        //将左边剩余元素填充进temp中
        while (i <= mid) {
            temp[k++] = arr[i++];
        }

        //将右边剩余元素填充进temp中
        while (j <= right) {
            temp[k++] = arr[j++];
        }

        System.arraycopy(temp, 0, arr, left, temp.length);
        // // 或者：
        // t = 0;
        // //将temp中的元素全部拷贝到原数组中
        // while(left <= right){
        //     arr[left++] = temp[t++];
        // }
    }

    /**
     * 主方法，用于测试归并排序
     * 
     * @param args 命令行参数，未使用
     */
    public static void main(String[] args) {
        int[] arr = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        mergeSort(arr);
        for (int num : arr) {
            System.out.print(num + " ");
        }
    }
}