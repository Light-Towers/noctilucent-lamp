## 启动服务
nohup paddlex --serve --pipeline object_detection --port 8866 > paddlex.log 2>&1 &


# labelme 标注
labelme images --labels label.txt --nodata --autosave --output annotations

## LabelMe 使用技巧

### 1. 基本操作技巧

- **快捷键使用**：
  - `Ctrl + N`：新建标注
  - `Ctrl + S`：保存标注
  - `Ctrl + O`：打开文件
  - `Delete`：删除选中的标注
  - `Ctrl + Z`：撤销操作

- **标注模式选择**：
  - 矩形框标注：适合标注规则形状目标
  - 多边形标注：适合标注不规则形状目标
  - 点标注：用于关键点标注
  - 线条标注：用于边界或路径标注

### 2. 高效标注技巧

- **分层标注**：
  - 为不同类别设置不同颜色，便于区分
  - 按重要性或类别分批次标注

- **批量操作**：
  - 利用复制粘贴功能处理相似图像
  - 使用自动标注功能预标注，再人工修正

- **质量控制**：
  - 定期保存工作进度
  - 使用验证功能检查标注质量
  - 建立标注规范并严格执行

### 3. 项目管理技巧

- **数据组织**：
  - 按类别文件夹分类存储图像
  - 建立清晰的命名规则
  - 定期备份标注数据

- **团队协作**：
  - 统一标注标准和规范
  - 分配任务避免重复工作
  - 定期同步和整合标注结果

### 4. 导出和转换

- **格式选择**：
  - 支持多种输出格式（JSON、XML等）
  - 根据下游任务选择合适格式
  - 可自定义导出模板

- **数据验证**：
  - 导出前检查标注完整性
  - 验证标注框是否准确覆盖目标
  - 确认类别标签正确性

## 启动 LabelMe 的方法

### 1. 命令行启动

```bash
labelme
```

这是最常用的启动方式，直接在终端或命令提示符中输入 `labelme` 命令即可启动应用程序。

### 2. Python 模块方式启动

```bash
python -m labelme
```

如果直接使用 `labelme` 命令无法启动，可以尝试通过 Python 模块的方式启动。

### 3. 指定图像文件夹启动

```bash
labelme /path/to/image/folder
```

可以在启动时直接指定包含图像文件的文件夹路径，LabelMe 会自动加载该文件夹中的图像。

### 4. 启动参数选项

- `--autosave`：自动保存标注结果
- `--nodata`：不加载图像数据
- `--autosave-path`：指定自动保存路径
- `--config`：指定配置文件

例如：
```bash
labelme --autosave --output /path/to/save
```

### 5. 环境要求

确保已正确安装 LabelMe：
```bash
pip install labelme
```

安装完成后即可通过上述命令启动 LabelMe 应用程序。



## 目标检测
// TODO 下面使用的是paddlex推理遇到的问题

### 目标检测-模型推理
paddlex --pipeline object_detection \
        --input /root/paddlex/img/2025年畜牧-展位分布图-1105-01.png \
        --threshold 0.5 \
        --save_path /root/paddlex/img/output/ \
        --device gpu:0

### 目标检测-数据转换
python main.py -c paddlex/configs/modules/object_detection/PicoDet-L.yaml \
    -o Global.mode=check_dataset \
    -o Global.dataset_dir=../dataset/det_booth \
    -o CheckDataset.convert.enable=True \
    -o CheckDataset.convert.src_dataset_type=LabelMe


出现报错：`assert shape["shape_type"] == "rectangle", "Only rectangle are supported."`。只能使用矩形框





## 实例分割
### 实例分割-模型推理
paddlex --pipeline instance_segmentation \
        --input /root/paddlex/img/2025年畜牧-展位分布图-1105-01.png \
        --threshold 0.5 \
        --save_path /root/paddlex/img/output/ \
        --device gpu:0

### 实例分割-数据转换
python main.py -c paddlex/configs/modules/instance_segmentation/Mask-RT-DETR-L.yaml\
    -o Global.mode=check_dataset \
    -o Global.dataset_dir=../dataset/seg_booth \
    -o CheckDataset.convert.enable=True \
    -o CheckDataset.convert.src_dataset_type=LabelMe

### 实例分割-数据集切分
python main.py -c paddlex/configs/modules/instance_segmentation/Mask-RT-DETR-L.yaml \
    -o Global.mode=check_dataset \
    -o Global.dataset_dir=../dataset/seg_booth \
    -o CheckDataset.split.enable=True \
    -o CheckDataset.split.train_percent=90 \
    -o CheckDataset.split.val_percent=10


python main.py -c paddlex/configs/modules/instance_segmentation/Mask-RT-DETR-L.yaml \
    -o Global.mode=check_dataset \
    -o Global.dataset_dir=../dataset/seg_booth


python main.py -c paddlex/configs/modules/instance_segmentation/Mask-RT-DETR-H.yaml \
    -o Global.mode=train \
    -o Global.dataset_dir=../dataset/seg_booth_paddle \
    -o Global.device=gpu:0 \
    -o Train.num_classes=1 \
    -o Train.dy2st=True


python main.py -c paddlex/configs/modules/instance_segmentation/Mask-RT-DETR-H.yaml \
    -o Global.mode=evaluate \
    -o Global.dataset_dir=../dataset/seg_booth_paddle \
    -o Global.device=gpu:0 \
    -o Train.num_classes=1 



### 推理
python main.py -c paddlex/configs/modules/instance_segmentation/Mask-RT-DETR-H.yaml \
    -o Global.mode=predict \
    -o Global.output="/root/paddlex/img/output/ins_seg" \
    -o Predict.model_dir="./output/best_model/inference" \
    -o Predict.input="/root/paddlex/img/长沙国际会展中心.jpg" \
    -o Global.output="/root/paddlex/img/output/predict_results"




问题：
`paddlex.utils.errors.others.UnsupportedParamError: 'Mask-RT-DETR-H' is not a registered model name.`

一、
pip install -e .
paddlex --install

又遇到问题：
module 'pkgutil' has no attribute 'ImpImporter'. Did you mean: 'zipimporter'?

降级python版本到3.10


又遇到问题：
category_id = label_to_cat_id_map[int(num_id)]

设置训练参数：Train.num_classes=1


问题：
paddlex.utils.deps.DependencyError: `PDFReaderBackend` is not ready for use, because the following dependencies are not available:
pypdfium2

pip install pypdfium2






python -c "from PIL import Image; print(Image.open('/root/paddlex/img/2024年展位图_resized.jpg').size)"