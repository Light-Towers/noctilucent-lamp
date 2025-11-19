# HTTP `Content-Disposition` 头部详解：支持中文文件名的 Excel 导出实践

## 1. 什么是 `Content-Disposition`？

`Content-Disposition` 是 HTTP 响应头（Response Header）中的一个字段，用于**指示客户端（通常是浏览器）如何处理响应体的内容**。

最常见的用途是：
- **触发文件下载**（而不是在浏览器中直接显示内容）
- **指定下载时的默认文件名**

典型格式：
```http
Content-Disposition: attachment; filename="example.xlsx"
```

其中：
- `attachment` 表示内容应作为附件下载；
- `filename` 指定建议的文件名。

---

## 2. 文件名中的国际化问题（中文/Unicode 支持）

### ❗问题背景
早期 HTTP 规范规定 `filename` 参数**仅支持 ASCII 字符**。如果直接使用中文（如 `filename=销售报表.xlsx`），会导致：
- 浏览器乱码；
- 下载文件名变成 `_`、`?.xlsx` 或空；
- IE 等旧浏览器完全无法识别。

### ✅ 解决方案：使用 `filename*` 扩展参数（RFC 5987）

现代标准通过 **带星号的扩展语法 `filename*`** 支持 Unicode 文件名。

#### 语法格式：
```
filename*=charset'language'encoded_filename
```

- `charset`：字符编码，通常为 `utf-8`
- `language`：语言标签（可选），一般留空 → 写作 `''`
- `encoded_filename`：**URL 编码（Percent-Encoding）后的文件名**

✅ 示例：
```http
Content-Disposition: attachment; filename="_.xlsx"; filename*=utf-8''%E9%94%80%E5%94%AE%E6%8A%A5%E8%A1%A8.xlsx
```
> `%E9%94%80%E5%94%AE%E6%8A%A5%E8%A1%A8` = `URLEncoder.encode("销售报表", "UTF-8")`

---

## 3. 最佳实践：同时设置 `filename` 和 `filename*`

为了**兼顾新旧浏览器兼容性**，推荐同时提供两个参数：

```java
response.addHeader("Content-Disposition", 
    "attachment; filename=" + fallbackName + "; filename*=utf-8''" + encodedFilename);
```

| 参数 | 作用 | 兼容性 |
|------|------|--------|
| `filename=...` | 提供 ASCII 兼容的备用文件名（如英文或下划线替换） | 所有浏览器（包括 IE） |
| `filename*=utf-8''...` | 提供正确的 UTF-8 编码文件名 | Chrome/Firefox/Edge/Safari 等现代浏览器 |

> 💡 **现代浏览器会优先使用 `filename*`，忽略 `filename`；老浏览器只认 `filename`。**

---

## 4. Java 实现示例（Spring Boot / Servlet）

### ✅ 步骤说明：
1. 准备原始文件名（含中文）
2. 对其进行 **UTF-8 URL 编码**
3. 构造 `Content-Disposition` 头，同时包含 `filename` 和 `filename*`
4. 设置正确的 MIME 类型（Excel）

### 🔧 代码示例：

```java
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// 原始中文文件名
String originalFilename = "用户订单报表.xlsx";

// URL 编码（UTF-8）
String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replaceAll("\\+", "%20"); // 兼容空格

// 可选：为 filename 提供 ASCII 降级版本（例如替换非 ASCII 字符）
String fallbackName = originalFilename.replaceAll("[^\\x20-\\x7e]", "_");

// 构建 Content-Disposition
String contentDisposition = String.format(
    "attachment; filename=\"%s\"; filename*=utf-8''%s",
    fallbackName,
    encodedFilename
);

response.addHeader("Content-Disposition", contentDisposition);

// 设置 MIME 类型（根据 Excel 版本选择）
response.addHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
// 或 .xls 格式：application/vnd.ms-excel
```

---

## 5. Excel MIME 类型对照表

| MIME 类型 | 对应格式 | 特点 |
|-----------|--------|------|
| `application/vnd.ms-excel` | `.xls` (Excel 97–2003) | 二进制格式，兼容性好 |
| `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | `.xlsx` (Excel 2007+) | XML 开放格式，体积更小，功能更强 |

> ✅ 推荐优先使用 `.xlsx` 格式（除非需兼容非常老的 Excel 版本）。

---

## 6. 注意事项与常见陷阱

| 问题 | 解决方案 |
|------|--------|
| 文件名出现 `+` 而不是空格 | 使用 `.replaceAll("\\+", "%20")` 替换 |
| IE 下载文件名乱码 | 确保 `filename` 是 ASCII；必要时对 IE 单独处理（检测 User-Agent） |
| 未加双引号导致特殊字符解析错误 | `filename="..."` 必须用双引号包裹值 |
| 忘记设置 `Content-Type` | 可能导致浏览器无法识别文件类型，影响打开方式 |

---

## 7. 总结：关键要点速记

- ✅ 中文文件名必须使用 `filename*` + URL 编码；
- ✅ 同时保留 `filename` 以兼容老浏览器；
- ✅ 编码方式：`URLEncoder.encode(filename, "UTF-8")`；
- ✅ 空格处理：将 `+` 替换为 `%20`；
- ✅ MIME 类型要匹配实际 Excel 格式（`.xls` vs `.xlsx`）；
- ✅ 响应头值中的文件名建议用双引号包裹。

--- 

> 📌 **适用场景**：Web 应用中导出 Excel、PDF、CSV 等文件，且文件名包含中文或其他 Unicode 字符。
> 🧑‍💻 **建议**：在生产环境中务必测试主流浏览器（Chrome、Firefox、Safari、Edge、IE11）的下载行为。