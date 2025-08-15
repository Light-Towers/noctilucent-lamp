## 简介
`pdf2image` 是一个将 PDF 文档转换为图像的 Python 库，依赖于 `poppler` 工具集。适用于需要将 PDF 页面转换为 JPG/PNG 等格式的场景。

## 安装

### 1. 安装 poppler 依赖

`pdf2image` 需要系统已安装 poppler。以下是各平台的安装方法：

#### Windows
1. 从 [poppler-windows releases](https://github.com/oschwartz10612/poppler-windows/releases/) 下载最新版 zip 包（官方 [poppler](https://poppler.freedesktop.org/) 需要自行源码编译）。
2. 解压到本地目录，例如 `D:\Software\poppler\poppler-24.08.0`。
3. 配置环境变量：
   - 新建系统变量 `POPPLER_HOME`，值为解压路径（如 `D:\Software\poppler\poppler-24.08.0`）。
   - 编辑 `PATH` 变量，新增 `%POPPLER_HOME%\Library\bin`。
4. 验证安装：打开命令提示符，执行 `pdftoppm -h`，若显示帮助信息则配置成功。

#### Ubuntu/Debian
```bash
sudo apt-get install poppler-utils
```

#### Arch Linux
```bash
sudo pacman -S poppler
```

#### macOS
```bash
brew install poppler
```

### 2. 安装 Python 包
```bash
pip install pdf2image
```

## 使用示例

### 基本转换
```python
from pdf2image import convert_from_path
import os

def pdf_to_images(pdf_path, output_folder):
    """将PDF转换为JPEG图片"""
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    
    # 转换PDF为图片列表（默认DPI=200, JPG格式）
    pages = convert_from_path(pdf_path)
    
    # 保存每一页
    for i, page in enumerate(pages):
        page.save(os.path.join(output_folder, f'page_{i+1}.jpg'), 'JPEG')

# 使用示例
if __name__ == "__main__":
    pdf_to_images("document.pdf", "output_images")
```

### 高级配置
```python
def pdf_to_images_advanced(pdf_path, output_folder):
    """高级配置示例：自定义参数"""
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
    
    pages = convert_from_path(
        pdf_path,
        dpi=300,                # 提高分辨率
        fmt='png',              # 使用PNG格式（注意小写）
        grayscale=False,        # 保留彩色
        first_page=1,           # 起始页
        last_page=5,            # 仅转换前5页（可选）
        thread_count=4,         # 多线程加速
        userpw=None,            # PDF密码（如有）
        timeout=120             # 超时时间（秒）
    )
    
    for i, page in enumerate(pages):
        # 使用3位数页码格式（如 page_001.png）
        page.save(os.path.join(output_folder, f'page_{i+1:03d}.png'), 'PNG')
```

## 常见问题

### 1. `poppler not found` 错误
- **原因**：poppler 未正确安装或 PATH 未配置。
- **解决**：按上述步骤验证 poppler 安装，确保 `pdftoppm -h` 可执行。

### 2. 转换速度慢
- **优化**：增加 `thread_count` 参数值，利用多核CPU加速。

### 3. 图片质量与文件大小
- **权衡**：高 DPI 提升质量但增大文件，PNG 无损但体积大，JPG 有损但体积小。

## 参考资料
- [pdf2image GitHub](https://github.com/Belval/pdf2image)
- [pdf2image 官方文档](https://pypi.org/project/pdf2image/)
- [poppler 官方文档](https://poppler.freedesktop.org/)