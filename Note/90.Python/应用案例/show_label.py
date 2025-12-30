import matplotlib.pyplot as plt
import matplotlib.patches as patches
from PIL import Image

# 1. 加载展位图文件
img_path = 'd:/0-test\img_handle\场馆展位图\第十一届世界猪业博览会.jpeg'  # 请确保图片在当前目录下
img = Image.open(img_path)
fig, ax = plt.subplots(figsize=(12, 16))

# 显示原图
ax.imshow(img)

# 2. 准备数据 (以之前给出的10个随机测试点为例)
# 格式: "展位号": [左上, 右上, 右下, 左下]
booths = {
    "E203": [(259, 633), (343, 633), (343, 713), (259, 713)],
    "E201": [(384, 633), (501, 633), (501, 713), (384, 713)],
    "E202": [(514, 633), (631, 633), (631, 713), (514, 713)],
    "E205": [(672, 633), (756, 633), (756, 713), (672, 713)],
    "E210": [(259, 740), (322, 740), (322, 820), (259, 820)],
    "E208": [(384, 740), (501, 740), (501, 820), (384, 820)],
    "E209": [(514, 740), (631, 740), (631, 820), (514, 820)],
    "E211": [(672, 740), (735, 740), (735, 820), (672, 820)],
    "E217": [(259, 847), (322, 847), (322, 927), (259, 927)],
    "E215": [(384, 847), (501, 847), (501, 927), (384, 927)]
}

# 3. 循环绘制每个展位
for name, points in booths.items():
    # 提取左上角坐标点 (x, y)
    top_left = points[0]
    # 计算矩形的宽和高 (基于右上和左下的坐标差)
    width = points[1][0] - points[0][0]
    height = points[3][1] - points[0][1]
    
    # 创建矩形框 (红色边框，透明填充)
    rect = patches.Rectangle(
        top_left, width, height, 
        linewidth=2, edgecolor='red', facecolor='none'
    )
    ax.add_patch(rect)
    
    # 在展位中心标注名称
    ax.text(top_left[0] + width/2, top_left[1] + height/2, name, 
            color='blue', fontweight='bold', ha='center', va='center', fontsize=8)

# 4. 设置显示效果
plt.title("Booth Coordinates Visualization")
plt.axis('on') # 开启坐标轴可以方便你调试具体的像素位置
plt.show()