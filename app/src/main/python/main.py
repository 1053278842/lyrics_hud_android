# from lyrics.SpotifyLyricsListener import start
import time
from lyrics.LyricsAdapterFactory import LyricsAdapterFactory
from lyrics.enums import Sources
from lyrics.Communication import RaspberryPiComm

class KugouLyricManager():
   def __init__(self):
      self.lyrics = None
      self.id = None
        
   def send_kugou_lyrics(self,hostname,id,position,image,status,title,artist,duration):
      start = time.time()  # 秒（float）

      pi_comm = RaspberryPiComm(hostname=hostname)

         
      kugouFactory =  LyricsAdapterFactory.get_adapter(Sources.Kugou)
      param = {
         "title":title,
         "artist":artist,
         "duration":duration
      }
      standardLyricsObj = kugouFactory.convert(param)
      end = time.time()
      elapsed_ms = (end - start) * 1000  # 转换为毫秒
      print(f'歌词初始化时间：{elapsed_ms}')
      # 以下数据需要通过接口获得
      standardLyricsObj.set_title(title)
      standardLyricsObj.set_artist(artist)
      standardLyricsObj.set_time(elapsed_ms+position)
      standardLyricsObj.set_image(image)
      standardLyricsObj.set_status(status)
      standardLyricsObj.set_id(id)
      # 存储本次结果
      self.lyrics = standardLyricsObj.get_lyrics()
      self.id = standardLyricsObj.get_id()

      try:
         pi_comm.send_lyrics(standardLyricsObj.to_json())  
      except Exception as e:
         print("//TODO 通信错误，想办法激活树莓派",e)
      
   def sync_play_state(self,hostname,status,position):
      kugouFactory =  LyricsAdapterFactory.get_adapter(Sources.Kugou)
      standardLyricsObj = kugouFactory.convert(None)
      standardLyricsObj.set_time(position)
      standardLyricsObj.set_status(status)
      # 状态还会带出上次保存的歌词数据，防止中途接收不到歌词数据
      standardLyricsObj.set_lyrics(self.lyrics)
      standardLyricsObj.set_id(self.id)
      json = standardLyricsObj.to_json()
      
      pi_comm = RaspberryPiComm(hostname=hostname)
      try:
         pi_comm.send_lyrics(standardLyricsObj.to_json())  
         # RaspberryPiComm(hostname="192.168.1.33").send_lyrics(standardLyricsObj.to_json())    
      except Exception as e:
         print("//TODO 通信错误，想办法激活树莓派",e)
