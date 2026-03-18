
import requests,json
from datetime import datetime
import time

def fetch_and_parse():
    # 定义URL
    url = "https://aistudio.baidu.com/studio/project/cluster/allList"

    # 定义请求头部信息
    headers = {
        "Accept": "*/*",
        "Accept-Encoding": "gzip, deflate, br, zstd",
        "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "Cache-Control": "no-cache",
        "Connection": "keep-alive",
        "Content-Length": "29",
        "Content-Type": "application/x-www-form-urlencoded",
        "Cookie": "BIDUPSID=0C4F38C27D84CC8B013D02EE44534F3E; PSTM=1761269592; jsdk-uuid=f8aa49ad-dd00-4fb9-ab90-135ffe2fc7e7; BAIDUID=0C4F38C27D84CC8B013D02EE44534F3E:SL=0:NR=10:FG=1; MCITY=-131%3A; H_WISE_SIDS_BFESS=60279_63146_66588_66579_66593_66677_66671_66694_66687_66802_66805_66961_66989_67005_66893_67050_67090_67055_67044_67110_67130_67139; BDUSS=k5XMW8tcjdiRlMzQmN4QmNVM0FaY1RVM1ZrNmtnU0R5SnNNMWZJcHIxLUhFTEpwRVFBQUFBJCQAAAAAAAAAAAEAAACCKWYseWFuZ19qaWFuX19odWEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIeDimmHg4ppY; BDUSS_BFESS=k5XMW8tcjdiRlMzQmN4QmNVM0FaY1RVM1ZrNmtnU0R5SnNNMWZJcHIxLUhFTEpwRVFBQUFBJCQAAAAAAAAAAAEAAACCKWYseWFuZ19qaWFuX19odWEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIeDimmHg4ppY; ai-studio-lc=zh_CN; BAIDUID_BFESS=0C4F38C27D84CC8B013D02EE44534F3E:SL=0:NR=10:FG=1; ZFY=8H9DbXlbiqQ3NwX:B9J5RMzAZAlrYK94SABmFLIq:ARdc:C; _xsrf=2|92f2c819|9451be89c74e690b06bbab0e13fcccee|1772437041; H_PS_PSSID=60279_63146_67601_67748_67756_67833_67861_67884_67917_67951_67953_67955_67970_67881_67992_68028_68042_68077_68087_67984_68003_68130; H_WISE_SIDS=60279_63146_67601_67748_67756_67833_67861_67884_67917_67951_67953_67955_67970_67881_67992_68028_68042_68077_68087_67984_68003_68130; __bid_n=19c2c51666d05765fa0610; ide-proxy=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNzI0ODgxIn0.ziqw4FROoE_ZHctTnzkIthcnTuMZAUx3EnA8UgK8a_o; hub-user=724881; lang=zh; ai-studio-ticket=79CDB229646D4464849D2AA7D55535AAE967EB1D54304DC6BF1B107CFEF76071; RT=\"z=1&dm=baidu.com&si=dc5c7a7a-7073-4061-978f-b8fecb390d98&ss=mmd5a537&sl=a&tt=5lo&bcn=https%3A%2F%2Ffclog.baidu.com%2Flog%2Fweirwood%3Ftype%3Dperf&ld=1b2t\"",
        "Host": "aistudio.baidu.com",
        "Origin": "https://aistudio.baidu.com",
        "Referer": "https://aistudio.baidu.com/project/edit/10028511",
        "Sec-Fetch-Dest": "empty",
        "Sec-Fetch-Mode": "cors",
        "Sec-Fetch-Site": "same-origin",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0",
        "sec-ch-ua": "\"Not)A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"127\", \"Chromium\";v=\"127\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"Windows\"",
        "x-requested-with": "XMLHttpRequest",
        "x-studio-token": "712349055F2B14ABF3D4F03F0E483DA04EF150549C824AA496BCC28D5130B3BA067C1CA58D6982CF73D540E4B69C2F4B"
    }

    # 定义请求体数据
    data = {
        # 这里添加你需要发送的数据，例如：
        "projectId": 10028511,
        "startMode": 0
    }

    # 发送POST请求
    response = requests.post(url, headers=headers, data=data)

    # # 打印响应内容
    # print(response.status_code)
    # print(response.text)

    # 解析JSON
    try:
        data = response.json()
    except json.JSONDecodeError:
        print("无法解析JSON响应", response.text)
        return
    
    # 检查result和scheduleList是否存在
    if 'result' not in data or 'scheduleList' not in data['result']:
        print("响应中未找到scheduleList", response.text)
        return

    # 提取scheduleList和membershipScheduleList
    schedule_list = data['result']['scheduleList']

    # 检查used为1的记录并输出displayName
    for schedule in schedule_list:
        if "可用" in schedule.get('tip', ''):
            displayName = schedule['displayName']
            if "V100 32GB" in displayName:
                print(datetime.now(), schedule['displayName'])
            # print(datetime.now(), schedule['displayName'])

# 无限循环，运行调度任务
while True:
    time.sleep(1)
    fetch_and_parse()
    