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
Python 3.12.11
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

### 自制数据集
使用 `PPOCRLabel` 工具进行数据集制作

windows 环境安装：使用conda创建新个干净的环境`python 3.10`。
安装下列pip依赖包
```bash
# windows用户：No python win32com. Error: No module named 'win32com'
pip install premailer
pip install pywin32

pip install paddlepaddle==3.1.1
pip install paddleocr==3.1.1
pip install paddlex==3.1.1
pip install PPOCRLabel==3.0.2   # 版本太高，无法运行

# 选择标签模式来启动
PPOCRLabel --lang ch  # 启动【普通模式】，用于打【检测+识别】场景的标签
```
闪退的一些原因：
1. 待处理的图片路径中不要出现中文
2. 图片不能太大


### 2.2 数据校验
```bash
# 数据校验命令
python main.py -c paddlex/configs/modules/seal_text_detection/PP-OCRv4_server_seal_det.yaml \
 -o Global.mode=check_dataset \
 -o Global.dataset_dir=../dataset/ocr_curve_det_dataset_examples
```

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

## 4. 推理

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

## 5.服务部署
### 安装服务化部署插件
paddlex --install serving

### 修改产线配置
替换产线配置文件中的对应位置 `vim paddlex/configs/pipelines/seal_recognition.yaml`
```bash
SubPipelines:
......
  SealOCR:
    pipeline_name: OCR
    text_type: seal
    use_doc_preprocessor: False
    use_textline_orientation: False
    SubModules:
      TextDetection:
        module_name: seal_text_detection
        model_name: PP-OCRv4_server_seal_det
        model_dir: /root/paddlex/seal_text/PaddleX-3.1.4/output/best_accuracy/inference
        limit_side_len: 736
        limit_type: min
        max_side_len: 4000
        thresh: 0.2
        box_thresh: 0.6
        unclip_ratio: 0.5
......
```

### 启动服务
nohup paddlex --serve --pipeline seal_recognition --port 8866 > paddlex.log 2>&1 &



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

***

## 日志解读
```bash
[2025/08/30 18:00:18] ppocr INFO: epoch: [100/100], global_step: 100000, lr: 0.000001, loss: 0.091679, loss_shrink_maps: 0.035711, loss_threshold_maps: 0.041902, loss_binary_maps: 0.007171, loss_cbn: 0.007171, avg_reader_cost: 0.00148 s, avg_batch_cost: 0.71354 s, avg_samples: 8.0, ips: 11.21165 samples/s, eta: 0:00:00, max_mem_reserved: 14606 MB, max_mem_allocated: 12963 MB
eval model:: 100%|██████████| 2000/2000 [01:54<00:00, 17.45it/s]
[2025/08/30 18:02:13] ppocr INFO: cur metric, precision: 0.9986105597459309, recall: 0.9968297998811175, hmean: 0.9977193852255826, fps: 19.991583695711522
[2025/08/30 18:02:13] ppocr INFO: best metric, hmean: 0.9998018624925699, is_float16: False, precision: 0.9998018624925699, recall: 0.9998018624925699, fps: 20.009952393051556, best_epoch: 7
[2025/08/30 18:02:15] ppocr INFO: Export inference config file to /root/paddlex/seal_text/PaddleX-3.1.4/output/latest/inference/inference.yml
Skipping import of the encryption module
[2025/08/30 18:02:16] ppocr INFO: inference model is saved to /root/paddlex/seal_text/PaddleX-3.1.4/output/latest/inference/inference
[2025/08/30 18:02:20] ppocr INFO: Already save model info in /root/paddlex/seal_text/PaddleX-3.1.4/output/latest
[2025/08/30 18:02:20] ppocr INFO: save model in /root/paddlex/seal_text/PaddleX-3.1.4/output/latest/latest
[2025/08/30 18:02:20] ppocr INFO: Export inference config file to /root/paddlex/seal_text/PaddleX-3.1.4/output/iter_epoch_100/inference/inference.yml
Skipping import of the encryption module
[2025/08/30 18:02:21] ppocr INFO: inference model is saved to /root/paddlex/seal_text/PaddleX-3.1.4/output/iter_epoch_100/inference/inference
[2025/08/30 18:02:23] ppocr INFO: Already save model info in /root/paddlex/seal_text/PaddleX-3.1.4/output/iter_epoch_100
[2025/08/30 18:02:23] ppocr INFO: save model in /root/paddlex/seal_text/PaddleX-3.1.4/output/iter_epoch_100/iter_epoch_100
[2025/08/30 18:02:23] ppocr INFO: best metric, hmean: 0.9998018624925699, is_float16: False, precision: 0.9998018624925699, recall: 0.9998018624925699, fps: 20.009952393051556, best_epoch: 7
```

这份日志记录了使用PaddleOCR训练一个文字检测模型的最后阶段。下面是对每个指标的中文解读。

核心结论是：虽然训练一共运行了100个轮次（epoch），但**表现最好的模型实际上是在第7轮训练时得到的**。训练到第100轮的模型，其性能反而有轻微下降。

***

### 训练进度 (在第100轮时)

这一行是在训练结束时的状态快照，展示了模型当时的各项参数。

`[2025/08/30 18:00:18] ppocr INFO: epoch: [100/100], global_step: 100000, lr: 0.000001, loss: 0.091679, loss_shrink_maps: 0.035711, loss_threshold_maps: 0.041902, loss_binary_maps: 0.007171, loss_cbn: 0.007171, avg_reader_cost: 0.00148 s, avg_batch_cost: 0.71354 s, avg_samples: 8.0, ips: 11.21165 samples/s, eta: 0:00:00, max_mem_reserved: 14606 MB, max_mem_allocated: 12963 MB`

* **epoch: [100/100]**: 训练轮次。表示当前正在进行第100轮训练，总共设置了100轮。一个 "epoch" 指的是模型完整地学习了一遍所有的训练数据。
* **global\_step: 100000**: 全局步数。表示模型总共处理了100,000个批次（batch）的数据。
* **lr: 0.000001**: **学习率 (Learning Rate)**。它控制着模型在训练中调整参数的幅度。在训练末期，这个值通常会变得非常小，以便进行微调。
* **loss: 0.091679**: **总损失值**。这是衡量模型预测错误程度的核心指标。训练的目标就是让这个值尽可能地低。
* **loss\_shrink\_maps / loss\_threshold\_maps / loss\_binary\_maps**: 这些是总损失值的具体组成部分。它们分别对应文字检测任务中的不同子目标，例如预测文本区域的准确度、文本边界的清晰度等。
* **ips: 11.21165 samples/s**: **每秒处理样本数 (Instances Per Second)**。代表模型的训练速度，即每秒能处理约11张图片。
* **eta: 0:00:00**: **预计剩余时间 (Estimated Time of Arrival)**。因为训练已经完成，所以剩余时间为0。
* **max\_mem\_...**: 这两个值显示了训练过程中GPU显存的使用情况。

***

### 模型评估

训练结束后，模型会在一个它从未见过的“验证集”上进行测试，以评估其真实性能。

`[2025/08/30 18:02:13] ppocr INFO: cur metric, precision: 0.9986..., recall: 0.9968..., hmean: 0.9977..., fps: 19.99...`
`[2025/08/30 18:02:13] ppocr INFO: best metric, hmean: 0.9998..., ... best_epoch: 7`

这部分对比了**当前模型**（第100轮的模型）和整个训练过程中**最佳模型**的性能指标。

* **precision (精确率)**: 在所有模型预测为“有文字”的区域中，有多大比例是真的文字？高精确率意味着“报喜不报忧”，很少误报。
    $$\text{精确率} = \frac{\text{真正例}}{\text{真正例} + \text{假正例}}$$
* **recall (召回率)**: 在所有图片里实际存在的文字区域中，模型成功找出了多少？高召回率意味着“宁可错杀，也不放过”，很少漏报。
    $$\text{召回率} = \frac{\text{真正例}}{\text{真正例} + \text{假反例}}$$
* **hmean (F1-Score)**: 精确率和召回率的**调和平均数**。这通常是评估模型综合性能最重要的指标，因为它同时兼顾了精确率和召回率。hmean值高，代表模型既准又全。
    $$\text{H-mean} = 2 \times \frac{\text{精确率} \times \text{召回率}}{\text{精确率} + \text{召回率}}$$
* **fps (帧率)**: **每秒处理帧数 (Frames Per Second)**。这个指标衡量的是模型的推理（预测）速度，数值越高代表模型速度越快。
* **best\_epoch: 7**: 这是一个**至关重要的信息**。它告诉你，综合性能最好（hmean最高，达到0.9998）的模型是在**第7轮**训练后保存的。而训练到第100轮的最终模型，其hmean (0.9977) 反而略低。这暗示了模型在第7轮之后可能出现了轻微的“过拟合”现象。

***

### 模型保存与导出

日志的最后几行确认了系统正在保存训练好的模型文件。

`[2025/08/30 18:02:16] ppocr INFO: inference model is saved to /root/.../output/latest/inference/inference`
`[2025/08/30 18:02:21] ppocr INFO: inference model is saved to /root/.../output/iter_epoch_100/inference/inference`

框架保存了最终的模型（名为 `latest` 或 `iter_epoch_100`）。同时，它通常也会在名为 `best_model` 的文件夹中保存性能最佳的模型。

因此，在实际部署使用时，你应该选择**第7轮训练出的那个最佳模型**，而不是最后训练完成的模型。


***

## 问题与解决
### 问题1
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

### 问题2
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

### 问题3
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

### 问题4
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