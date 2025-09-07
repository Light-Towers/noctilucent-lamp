# -*- coding: utf-8 -*-

import json

"""
    处理JSON文本中存在的\x00字符
"""
def remove_null_bytes(json_str):
    """Remove null bytes from a JSON string."""
    return json_str.replace('\x00', '')

def clean_json_file(input_filepath, output_filepath):
    """Read a JSON file, clean it, and write the result to a new file."""
    
    with open(input_filepath, 'r', encoding='utf-8', errors='replace') as file, open(output_filepath, 'w', encoding='utf-8') as outfile:
        while True:
            try:
                # 逐行读取
                line = file.readline()
                # print(line)
                if not line or line.strip() == '':
                    break  # End of file

                data = json.loads(line)
                cleaned_item = remove_null_bytes(json.dumps(data))
                print(cleaned_item)

                # 写入处理后的行到目标文件
                outfile.write(cleaned_item + '\n')

            except json.JSONDecodeError:
                print("Error decoding JSON. Ensure the file is valid JSON. ", line)
                return
    
# Example usage
# input_file = 'C:/Users/osmondy/Documents/WXWork/1688855639458009/Cache/File/2024-07/fair-20240718/ali_fair_fair_exhibitor/test.json'
input_file = '/root/yangjinhua/valid_excel/handle_mg_json/test.json'
output_file = '/root/yangjinhua/valid_excel/handle_mg_json/ali_fair_fair_exhibitor_handle.json'

clean_json_file(input_file, output_file)