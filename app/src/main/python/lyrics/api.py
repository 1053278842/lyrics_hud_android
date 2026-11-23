import time
import requests
import spotipy
import os
from .exceptions import NoLyricsException,UnauthException
from .totp import SpotifyTotp


USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.41 Safari/537.36"
TOKEN_FILE = "token.txt"

class Spotify:
    def __init__(self, dc_token: str) -> None:
        self.session = requests.Session()
        self.session.cookies.set('sp_dc', dc_token)
        self.session.headers['User-Agent'] = USER_AGENT
        self.session.headers['app-platform'] = 'WebPlayer'
        self.login()
        self.sp = spotipy.Spotify(self.token)

    def login(self):
        token = self.load_token()
        try:
            if token is None:
                print("获取token中...")
                server_time_seconds = int(time.time())
                totp = SpotifyTotp().generate_totp()
                TOKEN_URL = 'https://open.spotify.com/get_access_token?reason=init&productType=web_player'
                TOKEN_URL += f"&totp={totp}&totpVer=5&ts={server_time_seconds}"
                req = self.session.get(TOKEN_URL, allow_redirects=False)
                token = req.json()
                self.token = token['accessToken']
                self.session.headers['authorization'] = f"Bearer {self.token}"
                # i don't know why it sometimes returns the wrong token
                if(self.token[:2]!="BQ"): 
                    print("===================")
                    print(TOKEN_URL)
                    print(self.token)
                    print("===================")
                    self.login()
            else:
                self.token = token
                self.session.headers['authorization'] = f"Bearer {self.token}"
        except Exception as e:
            print(token)
            
    def save_token(self):
        """存储token"""
        with open(TOKEN_FILE, "w", encoding="utf-8") as f:
            f.write(self.token)

    def load_token(self):
        """读取token脚本"""
        if os.path.exists(TOKEN_FILE):
            with open(TOKEN_FILE, "r", encoding="utf-8") as f:
                token = f.read().strip()
                if token:
                    return token
        return None
    
    def delete_token(self):
        """删除保存 token 的文件"""
        if os.path.exists(TOKEN_FILE):
            os.remove(TOKEN_FILE)
            print("Token 文件已删除。")
        else:
            print("Token 文件不存在。")

    def get_me(self):
        try:
            return self.sp.current_user()
        except Exception as e:
            raise Exception("sp_dc provided is invalid, please check it again!") from e

    def get_current_song(self):
        try:
            return self.sp.currently_playing()
        except Exception as e:
            raise Exception("No song is currently playing.") from e

    def get_lyrics(self, track_id: str):
        params = 'format=json&market=from_token'
        req = self.session.get(f'https://spclient.wg.spotify.com/color-lyrics/v2/track/{track_id}', params=params)
        if(req.status_code == 401):
            raise UnauthException("token过期,未通过身份认证!")
        elif(req.status_code != 200):
            raise NoLyricsException("未找到歌词数据!"+track_id)
        return req.json()
    
    def album(self, album_id):
        return self.sp.album(album_id)
    
    def album_tracks(self, album_id, total):
        tracks = []
        for x in range(0, total, 50):
            album = self.sp.album_tracks(album_id, offset=x)
            tracks += [track['id'] for track in album['items']]
        tracks =  [x for x in tracks if x is not None]
        return tracks

    def playlist(self, playlist_id):
        return self.sp.playlist(playlist_id)
    
    def playlist_tracks(self, playlist_id, total):
        tracks = []
        for x in range(0, total, 100):
            play = self.sp.playlist_tracks(playlist_id, offset=x)
            tracks += [track['track']['id'] for track in play['items']]
        tracks =  [x for x in tracks if x is not None]
        return tracks
    
    def tracks(self, tracks):
        return self.sp.tracks(tracks)

    def search(self, q, type, limit):
        return self.sp.search(q=q, type=type, limit=limit)

    def select_user_playlist(self):
        playlist = self.sp.current_user_playlists()['items']
        for x, play in enumerate(playlist, start=1):
            print(f"{x}: {play['name']}")
        index = int(input("Enter the index of the playlist: "))
        return playlist[index-1]

    def select_user_album(self):
        albums = self.sp.current_user_saved_albums()['items']
        for x, album in enumerate(albums, start=1):
            print(f"{x}: {album['album']['name']}")
        index = int(input("Enter the index of the album: "))
        return albums[index-1]['album']
