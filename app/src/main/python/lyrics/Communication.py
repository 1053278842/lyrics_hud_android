
import requests
from .CommonUtil import CommonUtil
from .exceptions import RaspberryPiCommNetException

class RaspberryPiComm:
    def __init__(self,hostname="raspberrypi.local", port=5588):
        self.host = CommonUtil.get_ip_from_hostname(hostname)
        self.port = port
        if(self.host is None):
            print("无法将hostname:",hostname,"解析成ip地址!")
            # raise ValueError("无法将hostname:",hostname,"解析成ip地址!")
    
    # 返回解析后的ip地址
    def get_ip(self):
        return self.host
    
    def send_lyrics(self,lyrics_json):
        url = f"http://{self.host}:{self.port}/upload"
        response = requests.post(url, json=lyrics_json)
        # 检查返回状态码
        if response.status_code == 200:
            print("成功发送歌词数据")
            print(response.json())  # 输出服务器返回的信息
        else:
            print(f"请求失败，状态码: {response.status_code}")
            print(f"响应内容: {response.text}")
            print(f"请求地址: {url}")
            raise RaspberryPiCommNetException("树莓派请求失败!")
