import cv2
import numpy as np

def Img_Outline(input_dir):
    original_img = cv2.imread(input_dir)
    gray_img = cv2.cvtColor(original_img, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray_img, (9, 9), 0)                     # 高斯模糊去噪（设定卷积核大小影响效果）
    _, RedThresh = cv2.threshold(blurred, 209, 255, cv2.THRESH_BINARY_INV)  # 设定阈值165（阈值影响开闭运算效果）
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (5, 5))          # 定义矩形结构元素
    closed = cv2.morphologyEx(RedThresh, cv2.MORPH_CLOSE, kernel)       # 闭运算（链接块）
    opened = cv2.morphologyEx(closed, cv2.MORPH_OPEN, kernel)           # 开运算（去噪点）
    return original_img, gray_img, RedThresh, closed, opened


def findContours_img(original_img, opened):
    contours, hierarchy = cv2.findContours(opened, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    c = sorted(contours, key=cv2.contourArea, reverse=True)[0]          # 计算最大轮廓的旋转包围盒
    rect = cv2.minAreaRect(c)
    # 将最小包围矩形中心点坐标映射到整幅图像的坐标系中
    # 获取最小包围矩形的四个顶点
    box = np.int0(cv2.boxPoints(rect))
    # 计算最小包围矩形对于整幅图的中心点坐标
    center = np.int0(np.mean(box, axis=0)) #返回xy的平均值
    rectCenterX = center[0]
    rectCenterY = center[1]
    # 原图像中心点
    height, width = original_img.shape[:2]
    centerYOriginal = height / 2
    centerXOriginal = width / 2
    # 计算平移距离
    dy = centerYOriginal - rectCenterY
    # 创建平移矩阵
    translation_matrix = np.float32([[1, 0, 0], [0, 1, dy]])
    # 应用仿射变换将最小包围矩形中心的y轴方向平移到y轴中心
    translated_image = cv2.warpAffine(original_img, translation_matrix, (width,height), borderValue=(255, 255, 255))
    cv2.imshow("translated_image", translated_image)
    # 计算要旋转物体最小包围矩形的偏移角
    angle = None
    if rect[1][0] > rect[1][1]:  # w > h
        angle = int(rect[2]) #逆时针旋转，正数
    else:
        angle = -(90 - int(rect[2])) #顺时针旋转，负数
    print("angle",angle)
    draw_img = cv2.drawContours(translated_image.copy(), [box], -1, (0, 0, 255), 3)
    M = cv2.getRotationMatrix2D((centerXOriginal, centerYOriginal), angle, 1)
    result_img = cv2.warpAffine(translated_image, M, (width,height), borderValue=(255, 255, 255))#旋转后的黑色区域用白色填充
    return result_img,draw_img


if __name__ == "__main__":
    input_dir = "da.png"
    original_img, gray_img, RedThresh, closed, opened = Img_Outline(input_dir)
    result_img,draw_img = findContours_img(original_img,opened)

    # cv2.imshow("original_img", original_img)
    # cv2.imshow("gray_img", gray_img)
    # cv2.imshow("RedThresh", RedThresh)
    # cv2.imshow("Close", closed)
    # cv2.imshow("Open", opened)
    cv2.imshow("draw_img", draw_img)
    cv2.imshow("result_img", result_img)
    cv2.waitKey(0)
    cv2.destroyAllWindows()