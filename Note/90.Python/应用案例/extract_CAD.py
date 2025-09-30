import ezdxf
from ezdxf.enums import TextEntityAlignment

# DWG 文件路径
DWG_FILE = r"C:/Users/osmondy/Downloads/CAD/out/2025畜牧展4.23.dxf"
def extract_booth_info(filename):
    """
    从 DWG 文件中提取展位信息。
    
    参数:
        filename (str): DWG 文件的路径
        
    返回:
        None: 函数直接打印结果，无返回值
    """
    try:
        # 加载 DWG 文件
        doc = ezdxf.readfile(filename)
    except IOError as e:
        print(f"错误: 无法找到或打开文件 '{filename}'\n", f"异常详情: {e}")
        return
    except ezdxf.DXFStructureError as e:
        print(f"错误: 文件 '{filename}' 是无效的或损坏的 DWG/DXF 文件。\n", f"异常详情: {e}")
        return

    # 获取模型空间 (通常绘图内容都在模型空间)
    msp = doc.modelspace()

    # # 遍历模型空间中的实体
    # for entity in msp:
    #     print("模型空间中的实体: ", entity)

    booths = {}

    # 1. 提取展位号 (TEXT 或 MTEXT)
    # 假设展位号在 'Booth_Number' 图层
    booth_numbers = msp.query('TEXT[layer=="Booth_Number"]')
    
    print(f"找到了 {len(booth_numbers)} 个展位号文本。")
    
    # 打印所有TEXT实体
    all_text_entities = msp.query('TEXT')
    print(f"\n找到了 {len(all_text_entities)} 个TEXT实体:")
    for i, text in enumerate(all_text_entities):
        print(f"  TEXT {i+1}: '{text.dxf.text}' 在图层 '{text.dxf.layer}'")
    
    # 打印所有MTEXT实体
    all_mtext_entities = msp.query('MTEXT')
    print(f"\n找到了 {len(all_mtext_entities)} 个MTEXT实体:")
    for i, mtext in enumerate(all_mtext_entities):
        print(f"  MTEXT {i+1}: '{mtext.text}' 在图层 '{mtext.dxf.layer}'")

    for text in booth_numbers:
        booth_name = text.dxf.text
        # 获取文本的中心点或插入点作为其位置
        # 对于居中对齐的文本，位置可能更准确
        if text.dxf.halign != TextEntityAlignment.LEFT:
             # get_placement() 返回对齐点
            position = text.get_placement()[0]
        else:
            # 否则使用插入点
            position = text.dxf.insert

        booths[booth_name] = {'name': booth_name, 'position': position, 'boundary': None}

    # 2. 提取展位边界 (LWPOLYLINE)
    # 假设边界在 'Booth_Boundary' 图层
    booth_boundaries = msp.query('LWPOLYLINE[layer=="Booth_Boundary"]')
    print(f"找到了 {len(booth_boundaries)} 个展位边界。")

    # 3. 将边界和展位号关联起来
    # 这是一个简化的逻辑：找到离展位号最近的边界
    for boundary in booth_boundaries:
        if boundary.is_closed:
            # 计算边界的中心点 (近似)
            points = list(boundary.get_points('xy'))
            if not points:
                continue
            
            # 计算几何中心
            center_x = sum(p[0] for p in points) / len(points)
            center_y = sum(p[1] for p in points) / len(points)
            boundary_center = (center_x, center_y)

            # 寻找哪个展位号的中心点落在这个边界内
            for booth_name, info in booths.items():
                booth_pos = info['position']
                if boundary.point_in_polygon_2d(booth_pos):
                    booths[booth_name]['boundary'] = points
                    break # 找到后就跳出

    # 4. 打印结果
    for name, data in booths.items():
        print(f"--- 展位: {name} ---")
        print(f"  位置 (文本坐标): ({data['position'].x:.2f}, {data['position'].y:.2f})")
        if data['boundary']:
            print(f"  边界由 {len(data['boundary'])} 个顶点组成。")
        else:
            print("  未找到匹配的边界。")

if __name__ == "__main__":
    extract_booth_info(DWG_FILE)