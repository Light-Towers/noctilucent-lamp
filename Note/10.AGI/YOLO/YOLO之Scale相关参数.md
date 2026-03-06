这份关于 **Ultralytics YOLO26** 训练参数 `scale` 与 `multi_scale` 的技术指南，重点针对 OBB（旋转目标检测）任务中的小目标优化及显存管理。

---

# YOLO26 训练参数优化指南：Scale 与 Multi-scale

在 [Ultralytics YOLO26](https://docs.ultralytics.com/models/yolo26/) 的训练中，合理配置尺寸相关参数是提升模型鲁棒性（尤其是 OBB 小目标检测）的关键。

### 1. 核心参数定义与区别
虽然两者都涉及“缩放”，但作用维度不同：

*   **`scale` (图像级增强)**
    *   **原理**：在固定的 `imgsz` 画布内，对图像内容进行随机缩放（比例为 `1 ± scale`），随后通过填充或裁剪回到 `imgsz`。
    *   **目的**：模拟物体远近变化，不改变网络输入分辨率。
    *   **详情参考**：[数据增强配置](https://docs.ultralytics.com/guides/model-training-tips/#multi-scale-training)。

*   **`multi_scale` (网络级策略)**
    *   **原理**：动态改变**每个批次（Batch）**的输入分辨率。尺寸在 `imgsz * (1 ± multi_scale)` 范围内随机采样，并对齐至 32 步长（Stride）。
    *   **目的**：强制模型学习跨分辨率的特征提取，增强对不同分辨率输入的泛化性。
    *   **详情参考**：[多尺度训练最佳实践](https://docs.ultralytics.com/guides/model-training-tips/#multi-scale-training)。

### 2. OBB 任务与小目标优化建议
针对 `imgsz=1024` 的高分辨率场景，建议采用**非对称配置**：

*   **推荐配置**：`scale=0.8`, `multi_scale=0.25`（或根据显存关闭）。
*   **不建议 `multi_scale=0.8`**：
    1.  **目标消失风险**：当采样到极低分辨率且叠加 `scale` 缩小增强时，小目标像素可能低于下采样阈值而丢失。
    2.  **显存峰值**：`imgsz=1024` 配合 `0.5` 的 `multi_scale` 会使尺寸达到 1536，极易导致 16G 显存 OOM。
*   **补偿方案**：若显存不足，应优先保持高 `imgsz` 和高 `scale`，并确保 `mosaic=1.0` 开启以增强小目标样本。

### 3. 显存与增量训练策略
*   **显存允许含义**：指 GPU 内存需能支撑 `multi_scale` 波动范围内的**最大尺寸**。若发生 OOM，系统会触发 [自动减半 Batch 重试机制](https://docs.ultralytics.com/modes/train/)。
*   **增量训练**：修改这些参数**无需重新训练**。你可以在加载 `.pt` 权重后直接修改参数进行微调，模型会继承已有特征并适应新的尺寸分布。

### 4. 快速配置示例
```python
from ultralytics import YOLO

model = YOLO("yolo26n-obb.pt") # 或你的 best.pt
model.train(
    data="your_data.yaml",
    imgsz=1024,
    batch=-1,          # 自动匹配最大 batch
    scale=0.8,         # 激进的内容缩放
    multi_scale=0.25,  # 适度的输入尺寸波动 (768-1280)
    mosaic=1.0         # 必须开启以优化小目标
)
```

更多技术细节请访问 [Ultralytics 训练参数参考表](https://docs.ultralytics.com/platform/train/cloud-training/)。 ✅