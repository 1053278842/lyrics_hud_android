import json

class StandardLyrics:
    def __init__(self,id:str, title: str, artist: str, image:str,time:str,status:bool,lyrics: list):
        self.id = id
        self.title = title
        self.artist = artist
        self.lyrics = lyrics
        self.image = image
        self.time = time
        self.status = status

    def to_dict(self):
        return {
            "id":self.id,
            "title": self.title,
            "artist": self.artist,
            "time":self.time,
            "lyrics": self.lyrics,
            "image": self.image,
            "status": self.status
        }
        
    def to_json(self, indent=4, ensure_ascii=False):
        """将对象转换成JSON字符串"""
        return json.dumps(self.to_dict(), indent=indent, ensure_ascii=ensure_ascii)
        
    def set_title(self,title):
        self.title= title
        
    def set_time(self,time):
        self.time= time

    def set_artist(self,artist):
        self.artist= artist
        
    def set_image(self,image):
        self.image= image
        
    def set_status(self,status):
        self.status= status
        
    def set_id(self,id):
        self.id = id
    def get_id(self):
        return self.id
        
    def set_lyrics(self,lyrics):
        self.lyrics = lyrics
        
    def get_lyrics(self):
        return self.lyrics