```python
# 导包
# pip  install pymysql python-docx
import pymysql
from docx import Document
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT
from docx.enum.text import WD_PARAGRAPH_ALIGNMENT
from docx.oxml import parse_xml
from docx.shared import Pt, RGBColor
from docx.oxml.ns import qn, nsdecls
 
# 读取或者创建文件
document = Document()       # 新建文档
# document = Document('test.docx')      # 读取现有文档，建立文档对象

conn = pymysql.connect(host="192.168.100.21",
                           port=33306,
                           user="root",
                           password="root",
                           charset="utf8")
cursor = conn.cursor()

def tb_list(db_name):
    sql='''
        SELECT
            TABLE_NAME,
            TABLE_COMMENT 
        FROM
            information_schema.TABLES 
        WHERE
            table_schema = '{0}' 
            AND TABLE_COMMENT NOT LIKE '%弃用%';
    '''.format(db_name)

    print('1，执行sql：',sql)
    cursor.execute(sql)
    result = cursor.fetchall()
    return result


def load_data_from_mysql(db_name, table_name):
    sql='''
    SELECT 
        t.COLUMN_NAME 字段名,
        t.COLUMN_COMMENT 逻辑名,
        IF(LEN IS NOT NULL,CONCAT(DATA_TYPE,'(',LEN,')'),DATA_TYPE) 数据类型,
        CASE
                WHEN t.column_key = 'PRI' 
                THEN 'PK' 
                ELSE '' 
            END 约束,
        t.COLUMN_COMMENT 说明 
    FROM (
        SELECT 
            COLUMN_NAME,
            COLUMN_COMMENT,
            DATA_TYPE,
            (
                CASE
                    WHEN DATA_TYPE = 'float' 
                    OR DATA_TYPE = 'double' 
                    OR DATA_TYPE = 'tinyint' 
                    OR DATA_TYPE = 'SMALLINT' 
                    OR DATA_TYPE = 'MEDIUMINT' 
                    OR DATA_TYPE = 'INT' 
                    OR DATA_TYPE = 'INTEGER' 
                    OR DATA_TYPE = 'decimal' 
                    OR DATA_TYPE = 'bigint' 
                    THEN NUMERIC_PRECISION 
                    ELSE CHARACTER_MAXIMUM_LENGTH
                END
            ) + '' LEN,
        --   CASE
        --     WHEN IS_NULLABLE = 'YES' 
        --     THEN '是' 
        --     ELSE '否' 
        --   END 空否,
            column_key
        FROM
            INFORMATION_SCHEMA.COLUMNS 
        WHERE table_schema = '{0}' 
            AND table_name = '{1}'
    ) AS t
    '''.format(db_name, table_name)

    print('2，执行sql：',sql)
    cursor.execute(sql)
    result = cursor.fetchall()
    return result
 

def set_heading(content1, level, content2):
    # 第二种设置标题的方式，此方式还可以设置文本的字体、颜色、大小等属性
    run = document.add_heading(content1, level).add_run(content2)
    # 设置西文字体
    run.font.name = u'宋体'
    # 设置中文字体
    run._element.rPr.rFonts.set(qn('w:eastAsia'), u'宋体')
    # 设置字体颜色
    run.font.color.rgb = RGBColor(0,0,0)  # 黑色
    # 设置字体大小
    # run.font.size = Pt(30)
    # 设置下划线
    # run.font.underline = True
    # 设置删除线
    # run.font.strike = True
    # 设置加粗
    run.bold = True
    # 设置斜体
    # run.italic = True
 


# 总的大标题部分
set_heading('', 1, '表结构')


'''
要查询的表初始化
'''
# 需要查询的库名
# db_name_list = ['db_mingyang_builder']
db_name_list = ['db_mingyang_builder', 'db_mingyang_certificate', 'db_mingyang_common', 'db_mingyang_common_service', 'db_mingyang_home_court', 'db_mingyang_matching', 'db_mingyang_pay', 'db_mingyang_pubilc', 'db_mingyang_venue_booking', 'db_mingyang_venue_services_manage', 'db_seata']

 
'''
写入小标题以及表格内容
'''
# 设置正文全局字体
document.styles['Normal'].font.name = u'宋体'
document.styles['Normal']._element.rPr.rFonts.set(qn('w:eastAsia'), u'宋体')
 
# for i in range(0, len(db_name_list)):
#     print (i, db_name_list[i])


for i in range(len(db_name_list)):
    # 库名
    db_name = db_name_list[i]
    # print('db_name_list---', db_name_list[i])
    tbs = tb_list(db_name)

    # 写入小标题
    set_heading('', 5, '表清单')
    # document.add_heading('表清单', level=3)
    # 表格标题
    table_name = ['表名', '功能说明']
    # 二维元组转化为二维列表 ,且将其他类型转化为字符串 且将None转化为 ''
    table_data_list = list(list([( '' if it is None else str(it)) for it in items]) for items in list(tbs))
    # 创建表格行列
    table = document.add_table(rows=len(tbs)+1, cols=len(table_name),style='Table Grid')
    # 首行设置背景色
    rows = table.rows[0]
    for cell in rows.cells:
        shading_elm = parse_xml(r'<w:shd {} w:fill="D9D9D9"/>'.format(nsdecls('w')))
        cell._tc.get_or_add_tcPr().append(shading_elm)
    # 写入表格标题
    for i in range(len(table_name)):
        cell = table.cell(0, i)
        cell.paragraphs[0].alignment = WD_PARAGRAPH_ALIGNMENT.CENTER  # 水平居中
        cell.paragraphs[0].add_run(table_name[i]).font.bold = True   # 加粗 
        cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER  # 垂直居中

    # 写入表格内容
    for i in range(len(table_data_list)):
        for j in range(len(table_name)):
            table.cell(i+1, j).text = table_data_list[i][j]
    


    for tb in tbs:
        print('tb：',tb[0], tb[1])
        table_name = tb[0]
        table_name_desc = tb[1]

        # 查询sql获得二维元组
        tuple1 = load_data_from_mysql(db_name, table_name)
        # 二维元组转化为二维列表 ,且将其他类型转化为字符串 且将None转化为 ''
        table_data_list = list(list([( '' if it is None else str(it)) for it in items]) for items in list(tuple1))
        
        # 写入小标题
        title = table_name + ' ( ' + table_name_desc + '表 )'
        # document.add_heading(title, level=5)
        set_heading('', 6, title)
        # 表格标题
        table_name = ['字段名', '逻辑名', '数据类型', '约束', '说明']
        # 创建表格行列
        table = document.add_table(rows=len(table_data_list)+1, cols=len(table_name),style='Table Grid')
        # 首行设置背景色
        rows = table.rows[0]
        for cell in rows.cells:
            shading_elm = parse_xml(r'<w:shd {} w:fill="D9D9D9"/>'.format(nsdecls('w')))
            cell._tc.get_or_add_tcPr().append(shading_elm)
        # 写入表格标题
        for i in range(len(table_name)):
            cell = table.cell(0, i)
            cell.paragraphs[0].alignment = WD_PARAGRAPH_ALIGNMENT.CENTER  # 水平居中
            cell.paragraphs[0].add_run(table_name[i]).font.bold = True   # 加粗 
            cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER  # 垂直居中
    
        # 写入表格内容
        for i in range(len(table_data_list)):
            for j in range(len(table_name)):
                table.cell(i+1, j).text = table_data_list[i][j]
 
 
# 4
document.save("test.docx")
```

