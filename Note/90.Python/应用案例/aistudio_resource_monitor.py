
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
        "Cookie": "BAIDUID=072B4FFD371F8075C1179588E52D4312:FG=1; BIDUPSID=80A7AE0F11C69BEC77CE2F00A8FBE3D7; PSTM=1689492606; H_PS_PSSID=60271_62325_63148_63242_63354_63393_63390_63404_63442_63427_63506_63497_63537_63567_63564_63583_63578; H_WISE_SIDS=60271_62325_63148_63242_63354_63385_63393_63390_63404_63442_63427_63506_63497_63537; MCITY=-131%3A; jsdk-uuid=1537cf19-cc1a-4c41-858d-43bbbfe63e33; Hm_lvt_be6b0f3e9ab579df8f47db4641a0a406=1749622509; Hm_up_be6b0f3e9ab579df8f47db4641a0a406=%7B%22uid_%22%3A%7B%22value%22%3A%2215909385%22%2C%22scope%22%3A1%7D%2C%22user_reg_date%22%3A%7B%22value%22%3A%2220250102%22%2C%22scope%22%3A1%7D%2C%22user_course_rt%22%3A%7B%22value%22%3A%22%E9%9D%9E%E8%AF%BE%E7%A8%8B%E7%94%A8%E6%88%B7%22%2C%22scope%22%3A1%7D%2C%22user_center_type%22%3A%7B%22value%22%3A%225%22%2C%22scope%22%3A1%7D%7D; MAWEBCUID=web_BthINzoDPAKVQsgBuOXrCLKuGvpBSuWhXHQQzXRFHRudwxvhWI; newlogin=1; BDUSS=VRyeTV6dy0yVlpoS0t3TEJ6VVhQRXdva0F4ZUV2amFqUDVIRG9VQXFIOFVIeWhvRVFBQUFBJCQAAAAAAQAAAAEAAACA971HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABSSAGgUkgBoL; ZFY=fgZH2jNtu4vmProZw6SD9FBP:BACZZTdnvR4DqlkBKFc:C; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; RT=\"z=1&dm=baidu.com&si=e149ad0f-1d28-44cb-a3c5-e10daa2c2e35&ss=mbrj58p4&sl=3f&tt=z29&bcn=https%3A%2F%2Ffclog.baidu.com%2Flog%2Fweirwood%3Ftype%3Dperf&ld=1k09i\"; __bid_n=196e7d8376a87363d3326e; delPer=0; PSINO=2; BA_HECTOR=2h85010k2h2l04a00h0lak2g24258h1k4i3ft24; BDRCVFR[S4-dAuiWMmn]=I67x6TjHwwYf0; ab_sr=1.0.1_MDY3MjliNTQwMzJjMDhlMzZmYWM1NWFlN2ZmNTZjOGQ5Y2U4NzM5MDg0MDkwZmEwMDNlMzlmYmY4YmY2NmFkNjI4MDk5MzE3ZWNhMzhlMDg2M2U2Mzk3ZTJkYjc1NDU2ZTAzZmYxMjE2Y2M4YzNjM2ZhYThjYzg0ZmU2MThlZjQzMDNkNzMyNGFmZTEwOWEyYzNjZDliYWExZDI4MGVjMGJiYTllNDhiMDcwZGMxMDE2MTVlYzY5MzZjMDYzYzFh; ai-studio-ticket=A4135089B8B043D38089C06AFEA1C4473D0DD3D6682F4DE6B6CE8492F716990D; __tins__21208037=%7B%22sid%22%3A%201749622508986%2C%20%22vd%22%3A%2050%2C%20%22expires%22%3A%201749625368949%7D; __51cke__=; __51laig__=50; Hm_lpvt_be6b0f3e9ab579df8f47db4641a0a406=1749623568; HMACCOUNT=6BD4870791147D8A; ai-studio-lc=zh_CN; ide-proxy=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMTU5MDkzODUifQ.TmdFnVlhC7hds3VcZSIBD4GStD1ZhXu85JHZJTa4gmA; hub-user=15909385",
        "Host": "aistudio.baidu.com",
        "Origin": "https://aistudio.baidu.com",
        "Referer": "https://aistudio.baidu.com/project/edit/8954035",
        "Sec-Fetch-Dest": "empty",
        "Sec-Fetch-Mode": "cors",
        "Sec-Fetch-Site": "same-origin",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0",
        "sec-ch-ua": "\"Not)A;Brand\";v=\"99\", \"Microsoft Edge\";v=\"127\", \"Chromium\";v=\"127\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"Windows\"",
        "x-requested-with": "XMLHttpRequest",
        "x-studio-token": "55FE8A3A66C1F08E8481925681AB823812A490CB1218432399246B4999156AB2BA22B85BBEEEB09808911692B20BD04E"
    }

    # 定义请求体数据
    data = {
        # 这里添加你需要发送的数据，例如：
        "projectId": 8954035,
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
            if "32GB" in displayName:
                print(datetime.now(), schedule['displayName'])
            # print(datetime.now(), schedule['displayName'])

# 无限循环，运行调度任务
while True:
    time.sleep(1)
    fetch_and_parse()
    