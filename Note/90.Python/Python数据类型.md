# python 的基本数据类型



## 字符串 String

### 1. 字符串创建

```python
s1 = '单引号字符串'
s2 = "双引号字符串"
s3 = """多行
字符串"""
s4 = str(123)  # 将其他类型转换为字符串
```

### 2. 基本操作

```python
s = "Hello, World!"

# 长度
length = len(s)  # 13

# 索引访问
first_char = s[0]  # 'H'
last_char = s[-1]  # '!'

# 切片
sub1 = s[0:5]    # 'Hello'
sub2 = s[7:]     # 'World!'
sub3 = s[::2]    # 'Hlo ol!'

# 连接
combined = s1 + " " + s2
```

### 3. 常用方法

#### 查找与替换

```python
s = "hello world"

# 查找
pos = s.find('world')  # 6，找不到返回-1
pos = s.index('world') # 6，找不到会抛出异常

# 替换
new_s = s.replace('world', 'Python')  # 'hello Python'
```

#### 大小写转换

```python
s = "Python"

upper_s = s.upper()    # 'PYTHON'
lower_s = s.lower()    # 'python'
title_s = s.title()    # 'Python'
swap_s = s.swapcase()  # 'pYTHON'
```

#### 去除空白

```python
s = "  hello  "

stripped = s.strip()     # 'hello'
l_stripped = s.lstrip()  # 'hello  '
r_stripped = s.rstrip()  # '  hello'
```

#### 分割与连接

```python
s = "apple,banana,orange"

# 分割
parts = s.split(',')  # ['apple', 'banana', 'orange']
# 连接
joined = '-'.join(parts)  # 将分割后的parts连接: 'apple-banana-orange'
```

#### 判断方法

```python
s = "123abc"

is_alpha = s.isalpha()    # 是否全字母
is_digit = s.isdigit()    # 是否全数字
is_alnum = s.isalnum()    # 是否字母或数字
is_lower = s.islower()    # 是否全小写
is_upper = s.isupper()    # 是否全大写
is_space = s.isspace()    # 是否全空白字符
start = s.startswith('123')  # 是否以指定字符串开头
end = s.endswith('abc')      # 是否以指定字符串结尾
```

### 4. 格式化字符串

#### f-string (Python 3.6+)

```python
name = "Alice"
age = 25
msg = f"My name is {name} and I'm {age} years old."
```

#### format() 方法

```python
msg = "My name is {} and I'm {} years old.".format(name, age)
msg = "My name is {1} and I'm {0} years old.".format(age, name)
```

#### % 格式化 (较旧的方式)

```python
msg = "My name is %s and I'm %d years old." % (name, age)
```

### 5. 其他有用操作

```python
# 计数
count = "hello".count('l')  # 2

# 填充
centered = "hello".center(20, '-')  # '-------hello--------'
left = "hello".ljust(10)    # 'hello     '
right = "hello".rjust(10)   # '     hello'
zero_filled = "42".zfill(5) # '00042'

# 转换列表
chars = list("hello")  # ['h', 'e', 'l', 'l', 'o']

# 原始字符串（忽略转义）
path = r'C:\new_folder'  # C:\new_folder 反斜杠不会被转义
```

### 6. 字符串与字节转换

```python
# 字符串转字节
s = "hello"
b = s.encode('utf-8')  # b'hello', b 前缀是 bytes 类型的标识符，仅用于输出显示

# 字节转字符串
s = b.decode('utf-8')  # 'hello'
```

## List

列表是 Python 中最常用的可变序列类型，可以存储任意类型的元素。特点：可变、有序、允许重复元素

### 1. 创建列表

```python
# 空列表
empty_list = []
empty_list = list()

# 包含元素的列表
numbers = [1, 2, 3, 4, 5]
mixed = [1, "hello", 3.14, True]
nested = [[1, 2], [3, 4]]  # 嵌套列表

# 使用 list() 构造函数
chars = list("hello")  # ['h', 'e', 'l', 'l', 'o']
```

### 2. 基本操作

```python
my_list = [1, 2, 3, 4, 5]

# 访问元素
first = my_list[0]    # 1 (正向索引从0开始)
last = my_list[-1]    # 5 (负索引从-1开始)

# 切片操作
sub = my_list[1:4]    # [2, 3, 4] (不包括结束索引)
every_other = my_list[::2]  # [1, 3, 5] (步长为2)

# 长度
length = len(my_list)  # 5

# 检查元素是否存在
if 3 in my_list:
    print("3 is in the list")

# 连接列表
combined = [1, 2] + [3, 4]  # [1, 2, 3, 4]
```

### 3. 修改列表

```python
my_list = [1, 2, 3]

# 添加元素
my_list.append([4])       # [1, 2, 3, [4]]
my_list.insert(1, 1.5)  # [1, 1.5, 2, 3, 4] (在索引1处插入)
my_list.extend([5, 6])  # [1, 1.5, 2, 3, 4, 5, 6] (扩展列表)

# 修改元素
my_list[0] = 0          # [0, 1.5, 2, 3, 4, 5, 6]

# 删除元素
del my_list[1]          # [0, 2, 3, 4, 5, 6] (删除索引1的元素)
removed = my_list.pop() # 6, 列表变为 [0, 2, 3, 4, 5] (移除并返回最后一个元素)
my_list.remove(3)       # [0, 2, 4, 5] (移除第一个值为3的元素)
my_list.clear()         # [] (清空列表)
```

### 4. 列表方法

```python
nums = [5, 2, 8, 1, 4]

# 排序
nums.sort()            # [1, 2, 4, 5, 8] (原地排序)
sorted_nums = sorted(nums)  # 返回新排序列表，原列表不变

# 反转
nums.reverse()         # [8, 5, 4, 2, 1] (原地反转)
reversed_nums = nums[::-1]  # 创建反转副本

# 计数
count = nums.count(5)  # 1 (元素5出现的次数)

# 查找索引
index = nums.index(4)  # 2 (元素4的索引，不存在会报错)
```

### 5. 列表推导式

```python
# 创建平方数列表
squares = [x**2 for x in range(10)]  # [0, 1, 4, 9, 16, 25, 36, 49, 64, 81]

# 带条件的列表推导式
even_squares = [x**2 for x in range(10) if x % 2 == 0]  # [0, 4, 16, 36, 64]

# 嵌套列表推导式
matrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
flattened = [num for row in matrix for num in row]  # [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

### 6. 其他有用操作

```python
# 复制列表
original = [1, 2, 3]
shallow_copy = original.copy()  # 浅拷贝
deep_copy = original[:]         # 另一种浅拷贝方式

# 列表解包
a, b, c = [1, 2, 3]  # a=1, b=2, c=3
first, *rest = [1, 2, 3, 4]  # first=1, rest=[2, 3, 4]

# 枚举遍历
for index, value in enumerate(['a', 'b', 'c']):
    print(index, value)  # 0 a, 1 b, 2 c

# zip 并行遍历
names = ['Alice', 'Bob', 'Charlie']
ages = [25, 30, 35]
for name, age in zip(names, ages):
    print(name, age)  # Alice 25, Bob 30, Charlie 35
```

### 7. 性能考虑

- `append()` 和 `pop()` 操作是 O(1) 时间复杂度
- `insert()` 和 `remove()` 操作是 O(n) 时间复杂度
- 查找元素 (`in` 操作) 是 O(n) 时间复杂度
- 对于频繁的插入/删除操作，考虑使用 `collections.deque`

## Set

集合是 Python 中的一种无序、不重复元素集，非常适合用于成员检测、去重和数学集合运算。

### 1. 创建集合

```python
# 空集合
empty_set = set()  # 注意：不能用 {} 创建空集合，{} 创建的是空字典

# 包含元素的集合
fruits = {'apple', 'banana', 'orange'}
numbers = set([1, 2, 3, 4, 5])  # 从列表创建

# 集合推导式
squares = {x**2 for x in range(10)}  # {0, 1, 4, 9, 16, 25, 36, 49, 64, 81}
```

### 2. 基本操作

```python
my_set = {1, 2, 3}

# 添加元素
my_set.add(4)       # {1, 2, 3, 4}
my_set.update([5, 6])  # {1, 2, 3, 4, 5, 6} (添加多个元素)

# 删除元素
my_set.remove(6)    # {1, 2, 3, 4, 5} (元素不存在会报错)
my_set.discard(5)   # {1, 2, 3, 4} (元素不存在不会报错)
popped = my_set.pop()  # 随机移除并返回一个元素
my_set.clear()      # set() (清空集合)

# 长度
size = len(my_set)  # 0

# 检查元素是否存在
if 2 in my_set:
    print("2 is in the set")
```

### 3. 集合运算

```python
a = {1, 2, 3, 4}
b = {3, 4, 5, 6}

# 并集
union = a | b  # {1, 2, 3, 4, 5, 6}
union = a.union(b)

# 交集
intersection = a & b  # {3, 4}
intersection = a.intersection(b)

# 差集
difference = a - b  # {1, 2}
difference = a.difference(b)

# 对称差集 (仅在其中一个集合中存在的元素)
sym_diff = a ^ b  # {1, 2, 5, 6}
sym_diff = a.symmetric_difference(b)
```

### 4. 集合比较

```python
x = {1, 2}
y = {1, 2, 3}

# 子集检查
is_subset = x <= y  # True
is_subset = x.issubset(y)

# 真子集检查
is_proper_subset = x < y  # True

# 超集检查
is_superset = y >= x  # True
is_superset = y.issuperset(x)

# 真超集检查
is_proper_superset = y > x  # True

# 不相交检查
disjoint = x.isdisjoint({4, 5})  # True
```

### 5. 不可变集合 (frozenset)

```python
# 创建不可变集合
immutable_set = frozenset([1, 2, 3])

# 不可变集合支持集合运算，但不能修改
# immutable_set.add(4)  # 会报错
```

### 6. 常用方法

```python
# 复制集合
original = {1, 2, 3}
copied = original.copy()  # {1, 2, 3}

# 更新集合 (原地操作)
a = {1, 2}
a.update([3, 4])  # {1, 2, 3, 4}

# 交集更新 (原地操作)
a.intersection_update({2, 3, 4})  # a = {2, 3, 4}

# 差集更新 (原地操作)
a.difference_update({3, 4})  # a = {2}

# 对称差集更新 (原地操作)
a.symmetric_difference_update({2, 5})  # a = {5}
```

### 7. 集合应用场景

#### 去重
```python
names = ['Alice', 'Bob', 'Alice', 'Charlie']
unique_names = set(names)  # {'Alice', 'Bob', 'Charlie'}
```

#### 快速成员检测
```python
valid_users = {'user1', 'user2', 'user3'}
if input_username in valid_users:
    print("Access granted")
```

#### 筛选唯一元素
```python
numbers = [1, 2, 2, 3, 4, 4, 5]
unique_evens = {x for x in numbers if x % 2 == 0}  # {2, 4}
```

### 8. 性能特点

- 添加元素: O(1)
- 删除元素: O(1)
- 成员检测 (in 操作): O(1)
- 并集/交集/差集: O(len(a) + len(b))

集合基于哈希表实现，非常适合需要快速查找和去重的场景，但不保持元素的插入顺序（Python 3.7+ 中字典保持插入顺序，但集合仍然不保证）。


## Dict

### 1. 字典创建
```python
# 空字典
empty_dict = {}
empty_dict = dict()

# 直接初始化
person = {"name": "Alice", "age": 25, "city": "New York"}

# 通过键值对列表
data = dict([("a", 1), ("b", 2)])  # {'a': 1, 'b': 2}

# 字典推导式
squares = {x: x**2 for x in range(5)}  # {0:0, 1:1, 2:4, 3:9, 4:16}
```

---

### 2. 基本操作
#### 访问元素
```python
print(person["name"])   # "Alice" (KeyError 如果键不存在)
print(person.get("age", 0))  # 25 (若键不存在返回默认值 0)
```

#### 修改元素
```python
person["age"] = 26            # 更新现有键
person["country"] = "USA"     # 添加新键值对
```

#### 删除元素
```python
del person["city"]         # 删除指定键
age = person.pop("age")    # 删除并返回键值 → 26
person.clear()             # 清空字典
```

---

### 3. 常用方法
#### 键值遍历
```python
# 获取所有键
keys = person.keys()      # dict_keys(['name', 'country'])

# 获取所有值
values = person.values()  # dict_values(['Alice', 'USA'])

# 获取键值对
items = person.items()    # dict_items([('name', 'Alice'), ('country', 'USA')])

# 遍历字典
for key, value in person.items():
    print(f"{key}: {value}")
```

#### 合并字典
```python
# Python 3.9+ 合并运算符
dict1 = {"a": 1}
dict2 = {"b": 2}
merged = dict1 | dict2     # {'a':1, 'b':2}

# update() 方法
dict1.update(dict2)        # dict1 变为 {'a':1, 'b':2}
```

#### 默认值处理
```python
# get() 方法
value = person.get("height", 170)  # 键不存在时返回 170

# setdefault() 方法
city = person.setdefault("city", "Paris")  # 存在则返回原值，否则设置并返回默认值
```

---

### 4. 高级操作
#### 嵌套字典
```python
employees = {
    "Alice": {"age": 25, "position": "Engineer"},
    "Bob": {"age": 30, "position": "Manager"}
}

# 访问嵌套值
print(employees["Alice"]["position"])  # "Engineer"
```

#### 字典排序
```python
# 按键排序
sorted_by_key = dict(sorted(person.items()))

# 按值排序
sorted_by_value = dict(sorted(person.items(), key=lambda x: x[1]))
```

#### 字典视图
```python
keys_view = person.keys()       # 动态反映字典变化
person["email"] = "a@test.com"
print(keys_view)                # 包含新添加的 'email'
```

---

### 5. 性能优化技巧
1. **键的选择原则**：
   - 使用不可变类型作为键（字符串/数字/元组）
   - 避免使用复杂对象作为键（如列表）

2. **字典推导式**比循环更高效：
   ```python
   # 高效方式
   {k: v.upper() for k, v in data.items() if v}
   ```

3. **collections 模块扩展**：
   - `defaultdict`：自动处理缺失键
   - `OrderedDict`：保持插入顺序（Python 3.7+ 普通字典已支持）
   - `Counter`：快速计数工具

---

### 6. 类型转换
```python
# 字典 → 列表（仅保留键）
keys_list = list(person)  # ['name', 'country']

# 字典 ↔ JSON
import json
json_str = json.dumps(person)      # 字典转 JSON 字符串
person_dict = json.loads(json_str) # JSON 字符串转字典
```

---

### 应用场景
- **配置存储**：保存程序参数
- **数据聚合**：统计词频/分组数据
- **快速查找**：通过键直接访问值（比列表遍历快得多）
- **对象映射**：模拟简单对象属性（替代简单类）

---

**注意**：Python 3.7+ 的字典保持插入顺序，但若需要严格的顺序保证仍建议使用 `collections.OrderedDict`。


## 元组（Tuple）

### 一、元组的核心特性
**不可变序列**：  
- 一旦创建，**元素不可修改、添加或删除**
- 内存效率高于列表（适合存储固定数据）
- 可哈希（可用作字典的键）

---

### 二、元组的基本操作

#### 1. 创建元组
```python
# 空元组
empty_tuple = ()
empty_tuple = tuple()

# 单元素元组（必须加逗号）
single = (50,)  # 正确 → 元组
wrong = (50)    # 错误 → 整数 50

# 多元素元组
colors = ("red", "green", "blue")
numbers = tuple([1, 2, 3])  # 从列表转换
```

#### 2. 访问元素
```python
data = (10, 20, 30, 40, 50)

# 索引访问
print(data[0])    # 10
print(data[-1])   # 50

# 切片操作
subset = data[1:4]  # (20, 30, 40)

# 遍历元素
for item in data:
    print(item)
```

#### 3. 不可变性示例
```python
data = (1, 2, 3)
data[0] = 10  # ❌ 报错：TypeError（不可修改元素）
data.append(4)  # ❌ 报错：无此方法
```

---

### 三、元组常用方法

#### 1. `count()` - 统计元素出现次数
```python
t = (1, 2, 2, 3, 2)
print(t.count(2))  # 3
```

#### 2. `index()` - 查找元素首次出现位置
```python
t = ("a", "b", "c", "b")
print(t.index("b"))  # 1
print(t.index("x"))  # ❌ 报错：ValueError（元素不存在）
```

---

### 四、元组的高级操作

#### 1. 元组解包（Unpacking）
```python
# 基本解包
point = (3, 4)
x, y = point  # x=3, y=4

# 星号解包（Python 3+）
values = (1, 2, 3, 4, 5)
a, *b, c = values  # a=1, b=[2,3,4], c=5
```

#### 2. 元组拼接
```python
t1 = (1, 2)
t2 = (3, 4)
combined = t1 + t2  # (1, 2, 3, 4)
repeated = t1 * 3   # (1, 2, 1, 2, 1, 2)
```

---

## 五、元组 vs 列表

| **特性**       | 元组（Tuple）              | 列表（List）              |
|----------------|---------------------------|--------------------------|
| **可变性**     | 不可变                     | 可变                     |
| **内存占用**   | 较小                       | 较大                     |
| **功能方法**   | 仅 `count()`, `index()`    | 丰富的方法（如 `append()`）|
| **适用场景**   | 数据保护、字典键、函数返回值 | 需要动态修改的数据集合     |

---

### 六、典型使用场景

#### 1. 保护数据不被修改
```python
# 存储常量配置
COLORS = ("RED", "GREEN", "BLUE")  # 防止意外修改
```

#### 2. 作为字典的键
```python
# 列表不可哈希，元组可以
locations = {
    (35.6895, 139.6917): "Tokyo",
    (40.7128, -74.0060): "New York"
}
```

#### 3. 函数返回多个值
```python
def get_stats(numbers):
    return min(numbers), max(numbers), sum(numbers)/len(numbers)

min_val, max_val, avg_val = get_stats([1, 2, 3, 4, 5])
```

#### 4. 格式化字符串
```python
info = ("Alice", 25)
print("Name: %s, Age: %d" % info)  # Name: Alice, Age: 25
```

---

### 七、性能优化技巧

1. **快速交换变量值**  
   ```python
   a, b = 10, 20
   a, b = b, a  # 交换后 a=20, b=10（底层通过元组实现）
   ```

2. **替代简单对象**  
   ```python
   # 使用元组代替简单类
   person = ("Alice", 25, "Engineer")
   # 更规范的替代方案：collections.namedtuple
   ```

3. **内存敏感场景**  
   ```python
   # 创建百万级数据容器
   large_data = tuple(range(1_000_000))  # 比列表节省约 40% 内存
   ```

---

### 八、常见问题解决

#### 需要修改元组怎么办？
```python
# 转换为列表 → 修改 → 转回元组
t = (1, 2, 3)
temp_list = list(t)
temp_list[0] = 10
new_tuple = tuple(temp_list)  # (10, 2, 3)
```

#### 判断是否为元组
```python
data = (1, 2)
print(type(data) is tuple)  # True
print(isinstance(data, tuple))  # True
```

---

掌握元组的特性及应用场景，可以在保证数据安全性的同时提升程序性能。