from lyrics.adapters.base import LyricsAdapter
from lyrics.models.standardlyrics import StandardLyrics
from lyrics.CommonUtil import CommonUtil
from ll_kugou_lyric_api.core import KugouApi

class FormatKugouAdapter(LyricsAdapter):
    def convert(self, data: dict) -> StandardLyrics:
        if(data == None):
            return StandardLyrics(None,None, None, None,None,None,None)
        lrc_content = KugouApi(data["title"],data["artist"],data["duration"]).get_kugou_lrc()
        if(lrc_content == None):
            lrc_content = CommonUtil.format_lrc(data["duration"])+"纯没找到歌词,请欣赏..."
        lyrics_json = CommonUtil.parse_lrc_to_json(lrc_content)
        lyrics = lyrics_json.get("lyrics", [])
        return StandardLyrics(None,None, None, None,None,None,lyrics)
    
    
