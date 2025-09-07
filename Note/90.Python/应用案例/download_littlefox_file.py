import os, re
import requests

def download_pdf_from_url(pdf_url, folder='pdfs'):
    # 创建保存PDF的文件夹
    if not os.path.exists(folder):
        os.makedirs(folder)

    # 发送HTTP请求获取PDF文件
    response = requests.get(pdf_url)
    response.raise_for_status()  # 检查请求是否成功

    # 尝试从响应头中提取文件名
    # 'Content-Disposition': 'inline; filename="Tire Town School, Going to School 1: Eddy\'s First Day.pdf"'
    content_disposition = response.headers.get('Content-Disposition')
    if content_disposition and 'filename=' in content_disposition:
        file_name = content_disposition.split('filename=')[-1].strip().strip('"')
        # 去除或替换非法字符（windows文件名非法字符）
        file_name = re.sub(r'[<>:\\/*?"|]', '', file_name)
        print(f'Downloading: {file_name}')
    else:
        # 如果无法从响应头中提取文件名，使用默认文件名
        file_name = 'downloaded_file.pdf'

    # 保存PDF文件
    file_path = os.path.join(folder, file_name)
    print(f'Saving to: {file_path}')
    with open(file_path, 'wb') as f:
        f.write(response.content)
    print(f'Downloaded: {file_path}')

if __name__ == '__main__':
    pdf_url = 'https://res.littlefox.com/en/supplement/load_pdf/C0006725?_=572458'  # 替换为实际的PDF文件URL
    download_pdf_from_url(pdf_url)