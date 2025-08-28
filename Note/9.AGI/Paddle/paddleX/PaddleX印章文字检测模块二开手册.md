# paddlex 二开优化指南
> 官方文档：[PaddleX OCR模块文档](https://paddlepaddle.github.io/PaddleX/latest/module_usage/tutorials/ocr_modules/seal_text_detection.html)

## 目录
1. [环境准备](#1-环境准备)
2. [数据集操作](#2-数据集操作)
3. [训练与评估](#3-训练与评估)
4. [推理部署](#4-推理部署)

---

## 1. 环境准备

### 1.1 安装依赖
```bash
# GPU 版本，需显卡驱动程序版本 ≥550.54.14（Linux）或 ≥550.54.14（Windows）
python -m pip install paddlepaddle-gpu==3.0.0 -i https://www.paddlepaddle.org.cn/packages/stable/cu126/

# 安装指定版本matplotlib
pip install matplotlib==3.5.2

# 安装PaddleOCR插件
cd PaddleX
pip install -e ".[base]"
paddlex --install PaddleOCR --platform gitee.com
```

### 1.2 环境变量配置
```bash
# 解决matplotlib颜色异常问题
export MPLBACKEND=Agg
```

### 1.3 系统依赖安装
```bash
# 安装CMake依赖
sudo apt update
sudo apt install cmake
```

---

## 2. 数据集操作

### 2.1 下载示例数据集
```bash
wget https://paddle-model-ecology.bj.bcebos.com/paddlex/data/ocr_curve_det_dataset_examples.tar -P ./dataset
tar -xf ./dataset/ocr_curve_det_dataset_examples.tar -C ./dataset/
```

### 2.2 数据校验
```bash
# 数据校验命令
python main.py -c paddlex/configs/modules/seal_text_detection/PP-OCRv4_server_seal_det.yaml \
 -o Global.mode=check_dataset \
 -o Global.dataset_dir=../dataset/ocr_curve_det_dataset_examples
```

### 报错：
**问题1：**
```bash
RuntimeError: `deep_analyse` is not ready for use, because the following dependencies are not available: matplotlib
```

**原因：**
PaddleX 3.1.4 中的代码仍使用旧方法 `tostring_rgb()`，环境中安装的较新版本 Matplotlib 已被弃用，需使用`tostring_argb()`并手动转换RGB格式

**解决：**
```bash
pip install matplotlib==3.5.2
```

---

**问题2：**
```bash
_tkinter.TclError: unknown color name "white"
```

**原因：**
程序在尝试使用 Tkinter 图形库时遇到了问题，而所运行环境Ubuntu未安装图形界面，那么强制使用非 GUI 后端

**解决：**
```bash
export MPLBACKEND=Agg
```

---

**问题3：**
```bash
paddlex.utils.errors.others.UnsupportedParamError: 'PP-OCRv4_server_seal_det' is not a registered model name.
```

**原因：**
未正确安装PaddleOCR插件或未完成源码级安装，PaddleX的二次开发需安装 PaddleOCR 插件，参考：[PaddleX本地安装教程](https://paddlepaddle.github.io/PaddleX/latest/installation/installation.html#2-linuxpaddex)

**解决：**
```bash
cd PaddleX
pip install -e ".[base]"
# 安装PaddleOCR插件，通过--platform 指定 gitee.com 克隆源
paddlex --install PaddleOCR --platform gitee.com
```

---

**问题4：**
```bash
error: subprocess-exited-with-error

× python setup.py egg_info did not run successfully.
│ exit code: 1
╰─> [10 lines of output]
    /tmp/pip-install-n81qxllb/onnxoptimizer_240b7b6ae4454402befb603da3be24ed/setup.py:35: DeprecationWarning: Use shutil.which instead of find_executable
    CMAKE = find_executable('cmake')
    fatal: not a git repository (or any of the parent directories): .git
    Traceback (most recent call last):
    File "<string>", line 2, in <module>
    File "<pip-setuptools-caller>", line 35, in <module>
    File "/tmp/pip-install-n81qxllb/onnxoptimizer_240b7b6ae4454402befb603da3be24ed/setup.py", line 76, in <module>
        assert CMAKE, 'Could not find "cmake" executable!'
                ^^^^^
    AssertionError: Could not find "cmake" executable!
    [end of output]

note: This error originates from a subprocess, and is likely not a problem with pip.
error: metadata-generation-failed

× Encountered error while generating package metadata.
╰─> See above for output.
```

**原因：**
onnxoptimizer依赖的CMake未正确安装

**解决：**
```bash
sudo apt update
sudo apt install cmake
```

---

## 3. 训练与评估

### 3.1 训练命令
```bash
python main.py -c paddlex/configs/modules/seal_text_detection/PP-OCRv4_server_seal_det.yaml \
    -o Global.mode=train \
    -o Global.dataset_dir=../dataset/ocr_curve_det_dataset_examples \
    -o Global.device=gpu:0 \
    -o Train.epochs_iters=10 \
    -o Train.dy2st=True
```

### 3.2 评估命令
```bash
python main.py -c paddlex/configs/modules/seal_text_detection/PP-OCRv4_server_seal_det.yaml \
    -o Global.mode=evaluate \
    -o Global.dataset_dir=../dataset/ocr_curve_det_dataset_examples \
    -o Global.device=gpu:0
```

---

## 4. 推理部署

### 4.1 单图推理
```bash
python main.py -c paddlex/configs/modules/seal_text_detection/PP-OCRv4_server_seal_det.yaml \
    -o Global.mode=predict \
    -o Predict.model_dir="./output/best_accuracy/inference" \
    -o Predict.input="seal_text_det.png"
```

### 4.2 多图批量推理
```bash
# 批量处理示例（需自行扩展）
python main.py -c paddlex/configs/modules/seal_text_detection/PP-OCRv4_server_seal_det.yaml \
    -o Global.mode=predict \
    -o Predict.model_dir="./output/best_accuracy/inference" \
    -o Predict.input_dir="../test_images"
```


## 印章文本检测模型-训练结果实验对照

### PP-OCRv4_server_det

| 实验ID/(PP-OCRv4_server_det)          | 轮次    | 学习率    | 检测 Hmean(%) |
| ------------------------------------- | ------- | --------- | ------------- |
| 印章v4_server_det-30_0.001_97.71      | 30      | 0.001     | 97.71         |
| 印章v4_server_det-30_0.0001_93.02     | 30      | 0.0001    | 93.02         |
| 印章v4_server_det-30_0.00001_81.32    | 30      | 0.00001   | 81.32         |
| **印章v4_server_det-100_0.001_98.19** | **100** | **0.001** | **98.19**     |

### PP-OCRv4_mobile_det
| 实验ID/(PP-OCRv4_mobile_det)         | 轮次   | 学习率    | 检测 Hmean(%) |
| ------------------------------------ | ------ | --------- | ------------- |
| 印章v4_mobile_det-30_0.001_97.51     | 30     | 0.001     | 97.51         |
| 印章v4_mobile_det-30_0.0001_95.40    | 30     | 0.0001    | 95.40         |
| **印章v4_mobile_det-50_0.001_97.59** | **50** | **0.001** | **97.59**     |
