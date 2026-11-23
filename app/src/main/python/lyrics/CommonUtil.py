import socket
import re

class CommonUtil:
    
    # 解析 raspberrypi.local 的 IP 地址
    def get_ip_from_hostname(hostname):
        try:
            ip = socket.gethostbyname(hostname)
            return ip
        except socket.gaierror:
            return None
        
    def parse_lrc_to_json(lrc_text):
        lyrics = []
        pattern = re.compile(r'\[(\d{2}):(\d{2})\.(\d{2})](.*)')
        
        for line in lrc_text.strip().splitlines():
            match = pattern.match(line)
            if match:
                minutes = int(match.group(1))
                seconds = int(match.group(2))
                hundredths = int(match.group(3))
                total_ms = minutes * 60 * 1000 + seconds * 1000 + hundredths * 10
                lyrics.append({
                    "startTimeMs": str(total_ms),
                    "words": match.group(4).strip(),
                    "syllables": [],
                    "endTimeMs": "0"
                })

        json_data = {
            "lyrics": lyrics
        }
        return json_data
    
    def format_lrc(ms: int) -> str:
        minutes = ms // 60000
        seconds = (ms % 60000) // 1000
        hundredths = (ms % 1000) // 10  # 保留两位小数（百分之一秒）
        return f"[{minutes:02}:{seconds:02}.{hundredths:02}]"
