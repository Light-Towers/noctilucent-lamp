package com.study.hbase.dml;

import com.alibaba.fastjson2.JSON;
import com.study.hbase.config.HBaseConnect;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.ColumnValueFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author osmondy
 */
public class HBaseDML {
    /**
     * 添加静态属性 connection 指向单例连接
     */
    public static Connection connection = HBaseConnect.connection;

    /**
     * 插入数据
     *
     * @param namespace    命名空间名称
     * @param tableName    表名称
     * @param rowKey       主键
     * @param columnFamily 列族名称
     * @param columnName   列名
     * @param value        值
     */
    public static boolean putCell(String namespace, String tableName, String rowKey, String columnFamily,
            String columnName, String value) {
        try {
            // 1. 获取 table
            Table table = connection.getTable(TableName.valueOf(namespace, tableName));
            // 2. 调用相关方法插入数据
            // 2.1 创建 put 对象
            Put put = new Put(Bytes.toBytes(rowKey));
            // 2.2. 给 put 对象添加数据
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(value));
            // 2.3 将对象写入对应的方法
            table.put(put);
            // 3. 关闭 table
            table.close();
            System.out.println("put sucess！");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 读取数据 读取对应的一行中的某一列
     *
     * @param namespace    命名空间名称
     * @param tableName    表名称
     * @param rowKey       主键
     * @param columnFamily 列族名称
     * @param columnName   列名
     */
    public static List<String> getCells(String namespace, String tableName, String rowKey, String columnFamily,
            String columnName) {
        List<String> list = new ArrayList<>();
        try {
            // 1. 获取 table
            Table table = connection.getTable(TableName.valueOf(namespace, tableName));
            // 2. 创建 get 对象
            Get get = new Get(Bytes.toBytes(rowKey));
            // 如果直接调用 get 方法读取数据 此时读一整行数据
            // 如果想读取某一列的数据 需要添加对应的参数
            get.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
            // 设置读取数据的版本
            // get.readAllVersions();

            // 读取数据 得到 result 对象
            Result result = table.get(get);
            if (!result.isEmpty()) {
                // 处理数据
                Cell[] cells = result.rawCells();
                for (Cell cell : cells) {
                    // cell 存储数据比较底层
                    list.add(new String(CellUtil.cloneValue(cell)));
                }
            }
            // 关闭 table
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 扫描数据
     *
     * @param namespace 命名空间
     * @param tableName 表名称
     * @param startRow  开始的 row 包含的
     * @param stopRow   结束的 row 不包含
     */
    public static void scanRows(String namespace, String tableName, String startRow, String stopRow) {
        try {
            // 1. 获取 table
            Table table = connection.getTable(TableName.valueOf(namespace, tableName));
            // 2. 创建 scan 对象
            Scan scan = new Scan();
            // 如果此时直接调用 会直接扫描整张表
            // 添加参数 来控制扫描的数据
            // 默认包含
            scan.withStartRow(Bytes.toBytes(startRow));
            // 默认不包含
            scan.withStopRow(Bytes.toBytes(stopRow));
            // 读取多行数据 获得 scanner
            ResultScanner scanner = table.getScanner(scan);
            // result 来记录一行数据 cell 数组
            // ResultScanner 来记录多行数据 result 的数组
            for (Result result : scanner) {
                Cell[] cells = result.rawCells();
                for (Cell cell : cells) {
                    System.out.print(new String(CellUtil.cloneRow(cell)) + "-" + new String(CellUtil.cloneFamily(cell))
                            + "-" + new String(CellUtil.cloneQualifier(cell)) + "-"
                            + new String(CellUtil.cloneValue(cell)) + "\t");
                }
                System.out.println();
            }
            // 3. 关闭 table
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 带过滤的扫描
     *
     * @param namespace    命名空间
     * @param tableName    表名称
     * @param startRow     开始 row
     * @param stopRow      结束 row
     * @param columnFamily 列族名称
     * @param columnName   列名
     * @param value        value 值
     * @throws IOException
     */
    public static void filterScan(String namespace, String tableName, String startRow, String stopRow,
            String columnFamily, String columnName, String value) throws IOException {
        // 1. 获取 table
        Table table = connection.getTable(TableName.valueOf(namespace, tableName));
        // 2. 创建 scan 对象
        Scan scan = new Scan();
        // 如果此时直接调用 会直接扫描整张表
        // 添加参数 来控制扫描的数据
        // 默认包含
        scan.withStartRow(Bytes.toBytes(startRow));
        // 默认不包含
        scan.withStopRow(Bytes.toBytes(stopRow));
        // 可以添加多个过滤
        FilterList filterList = new FilterList();
        // 创建过滤器
        // (1) 结果只保留当前列的数据
        ColumnValueFilter columnValueFilter = new ColumnValueFilter(
                // 列族名称
                Bytes.toBytes(columnFamily),
                // 列名
                Bytes.toBytes(columnName),
                // 比较关系
                CompareOperator.EQUAL,
                // 值
                Bytes.toBytes(value));
        // (2) 结果保留整行数据
        // 结果同时会保留没有当前列的数据
        SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter(
                // 列族名称
                Bytes.toBytes(columnFamily),
                // 列名
                Bytes.toBytes(columnName),
                // 比较关系
                CompareOperator.EQUAL,
                // 值
                Bytes.toBytes(value));
        // 本身可以添加多个过滤器
        filterList.addFilter(singleColumnValueFilter);
        // 添加过滤
        scan.setFilter(filterList);
        try {
            // 读取多行数据 获得 scanner
            ResultScanner scanner = table.getScanner(scan);
            // result 来记录一行数据 cell 数组
            // ResultScanner 来记录多行数据 result 的数组
            for (Result result : scanner) {
                Cell[] cells = result.rawCells();
                for (Cell cell : cells) {
                    System.out.print(new String(CellUtil.cloneRow(cell)) + "-" + new String(CellUtil.cloneFamily(cell))
                            + "-" + new String(CellUtil.cloneQualifier(cell)) + "-"
                            + new String(CellUtil.cloneValue(cell)) + "\t");
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 3. 关闭 table
        table.close();
    }

    /**
     * 删除 column 数据
     *
     * @param nameSpace
     * @param tableName
     * @param rowKey
     * @param family
     * @param column
     * @throws IOException
     */
    public static boolean deleteColumn(String nameSpace, String tableName, String rowKey, String family,
            String column) {
        try {
            // 1.获取 table
            Table table = connection.getTable(TableName.valueOf(nameSpace, tableName));
            // 2.创建 Delete 对象
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            // 3.添加删除信息
            // 3.1 删除单个版本
            //
            delete.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            // 3.2 删除所有版本
            delete.addColumns(Bytes.toBytes(family), Bytes.toBytes(column));
            // 3.3 删除列族
            // delete.addFamily(Bytes.toBytes(family));
            // 3.删除数据
            table.delete(delete);
            // 5.关闭资源
            table.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            putCell("china", "beijing", "1001", "changping", "area", "天通苑");
            putCell("china", "beijing", "1002", "changping", "area", "北七家");

            // List<String> a = getCells("china", "beijing", "1001", "changping", "area");
            // System.out.println(JSON.toJSONString(a));
            // List<String> b = getCells("china", "beijing", "1001", "changping", "school");
            // System.out.println(JSON.toJSONString(b));

            // scanRows("china", "beijing", "1001", "2000");
            // List<String> strings = scanRows("bigdata", "student", "1001", "2000");
            // for (String string : strings) {
            // System.out.println(string);
            // deleteColumn("china", "jiangsu", "1001", "taizhou", "area");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
