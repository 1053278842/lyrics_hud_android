import base64
import json
import time
import threading
import requests
from flask import Flask, request, redirect
from .api import Spotify
import sys
from .Communication import RaspberryPiComm
from .exceptions import NoLyricsException,UnauthException

from .LyricsAdapterFactory import LyricsAdapterFactory
from .enums import Sources

import urllib
sys.stdout.reconfigure(encoding='utf-8')

CLIENT_ID = "7602eb0b6eaa4803899575f2698474a5"
CLIENT_SECRET = "1cec5e19785c4b44be3216d3f53efe8e"
REDIRECT_URI = "http://localhost:5000/callback"
AUTH_URL = "https://accounts.spotify.com/authorize"
TOKEN_URL = "https://accounts.spotify.com/api/token"

SCOPES = "user-read-playback-state"

app = Flask(__name__)
access_token = None

def save_token(token):
    global access_token
    access_token = token
    print(f"获取 Token: {token}")

def get_token():
    return access_token

@app.route("/")
def login():
    auth_params = {
        "client_id": CLIENT_ID,
        "response_type": "code",
        "redirect_uri": REDIRECT_URI,
        "scope": SCOPES
    }
    return redirect(f"{AUTH_URL}?" + "&".join([f"{k}={v}" for k, v in auth_params.items()]))

@app.route("/callback")
def callback():
    code = request.args.get("code")
    if not code:
        return "授权失败"
    
    token_data = {
        "grant_type": "authorization_code",
        "code": code,
        "redirect_uri": REDIRECT_URI,
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET
    }
    response = requests.post(TOKEN_URL, data=token_data)
    token_json = response.json()
    save_token(token_json.get("access_token"))
    return "授权成功, 可以关闭此页面"

def get_lyrics(music_canonical):
    """返回歌词json数据,当前歌曲没有歌词则返回None"""
    sp = Spotify("AQCFDbh4qjdCxAOHzNk-ehVbOUNIMSSs72JxZorIttETE-sycbrfRIYyH6zoNnpXzLIwmyi_kBwi6nIy7T6TavQLMXBySfnVo4v1wxkiPh9BxN1hB5RZ2quXtV2_j8Afla6WlK1Qxgu36qg6U_vF8Z-OX_IP9nyMFApvmgTpQzMIB0oHorA2399HiFvkvwJO9f3K46F6QeRoXbC4a8nrwCgk9i3Q")
    try:
        resultJson = sp.get_lyrics(music_canonical)
        
        print("获取歌词成功!")
        #存储正确的token
        sp.save_token()
        save_local(resultJson,"lyrics")
    except UnauthException as e:
        print("认证出错,重新获取中!!")
        sp.delete_token()
        time.sleep(1)
        return get_lyrics(music_canonical)
    except NoLyricsException as e:
        print("get_lyrics未找到歌词数据! NoLyricsException:",e)
        resultJson = None
    except Exception as e:
        resultJson = None
    return resultJson

def fetch_playback_state(is_local):
    global access_token
    while True:
        if not access_token:
            print("开始获取accessToken")
            time.sleep(5)
            continue
        
        headers = {"Authorization": f"Bearer {access_token}"}
        response = requests.get("https://api.spotify.com/v1/me/player", headers=headers)
        
        if response.status_code == 401:
            print("Token 失效，重新授权...")
            access_token = None
            continue
        
        if response.status_code == 204:
            print("⏱️  Spotify未播放!")
            time.sleep(5)
            continue
        
        data = response.json()
        save_local(data,"me")
        if "error" in data:
            print("获取播放状态失败:")
            
        if not data.get("item"):
            print("当前没有正在播放的歌曲")
            time.sleep(1)
            # 广告时间或者没有启动。不做任何操作
            continue

        # 获取歌曲 URL 和封面 URL
        curr_music_url = data["item"]["external_urls"]["spotify"]
        curr_music_image_url = data["item"]["album"]["images"][0]["url"]
        
        is_playing = False if not data["is_playing"] else True 
        curr_music_timestamp =  data["timestamp"]
        curr_music_progress_ms =  data["progress_ms"]
        title = data["item"]["name"]
        artist =  data["item"]["artists"][0]["name"]
        print("🎵 播放状态:","⏸️" if not is_playing else "▶️")
        print("🎵 时间戳:",curr_music_timestamp)
        print("🎵 进程mm:",curr_music_progress_ms)
        
        # 提取标识符
        music_canonical = curr_music_url.split("/")[-1]
        music_image_canonical = curr_music_image_url.split("/")[-1]

        # print(f"当前音乐: {curr_music_url} 标识: {music_canonical}")
        print(f"🎵 专辑图片: {curr_music_image_url}")

        # 范例json要求1.歌词数据 2.当前进程curr_music_progress_ms 3.播放状态 4.音乐名称 5.专辑名称（可选） 6.专辑图片（可选）
        
        # 构造歌词请求 URL
        
        lyrics_json = get_lyrics(music_canonical)
        if(lyrics_json is None):
            print("未找到歌词，推送空白！")
            lyrics_json = {"status":"no lyrics"}  # 空白歌词
        else:    
            spotifyFactory =  LyricsAdapterFactory.get_adapter(Sources.Spotify)
            standardLyricsObj = spotifyFactory.convert(lyrics_json)
            # 以下数据需要通过接口获得
            standardLyricsObj.set_title(title)
            standardLyricsObj.set_artist(artist)
            standardLyricsObj.set_time(curr_music_progress_ms)
            standardLyricsObj.set_image(curr_music_image_url)
            standardLyricsObj.set_status(is_playing)
            standardLyricsObj.set_id(music_canonical)
            standardLyricsObj.to_json()
            save_local(standardLyricsObj.to_dict(),"spotify_json")
        # 无论如何都会推送
        #pi_comm = RaspberryPiComm()
        if(is_local):
            pi_comm = RaspberryPiComm(hostname="127.0.0.1")
        else:
            pi_comm = RaspberryPiComm()
        try:
            pi_comm.send_lyrics(standardLyricsObj.to_json())  
        except Exception as e:
            print("//TODO 通信错误，想办法激活树莓派",e)
        time.sleep(5)

def save_local(data,name):
    # 写入当前目录下的 data.json
    with open(f"./{name}.json", "w", encoding="utf-8") as file:
        json.dump(data, file, indent=4, ensure_ascii=False)

def start(is_local=True):
    threading.Thread(target=lambda: fetch_playback_state(is_local), daemon=True).start()
    app.run(port=5000)

 
