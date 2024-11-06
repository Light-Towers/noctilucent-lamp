/**
 * 快速排序类
 */
public class QuickSort {
    /**
     * 对数组进行快速排序
     * 
     * @param arr 待排序的数组
     */
    public static void quickSort(int[] arr) {
        if (arr == null || arr.length < 2) {
            return;
        }
        quickSort(arr, 0, arr.length - 1);
    }

    /**
     * 快速排序的递归实现
     * 
     * @param arr 待排序的数组
     * @param left 排序的起始索引
     * @param right 排序的结束索引
     */
    private static void quickSort(int[] arr, int left, int right) {
        if (left < right) {
            int pivotIndex = partition(arr, left, right);
            quickSort(arr, left, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, right);
        }
    }

    /**
     * 将数组分割为两部分，并确定基准元素的位置
     * 
     * @param arr 待分割的数组
     * @param left 分割的起始索引
     * @param right 分割的结束索引
     * @return 基准元素的索引
     */
    private static int partition(int[] arr, int left, int right) {
        int pivot = arr[right];
        int i = left - 1;
        for (int j = left; j < right; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, right);
        return i + 1;
    }

    /**
     * 交换数组中的两个元素
     * 
     * @param arr 包含要交换元素的数组
     * @param i 第一个元素的索引
     * @param j 第二个元素的索引
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * 主函数，用于测试快速排序
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        int[] arr = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        quickSort(arr);
        for (int num : arr) {
            System.out.print(num + " ");
        }
    }
}