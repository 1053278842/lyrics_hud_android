from lyrics.adapters.base import LyricsAdapter
from lyrics.models.standardlyrics import StandardLyrics

class FormatSpotifyAdapter(LyricsAdapter):
    def convert(self, data: dict) -> StandardLyrics:
        lyrics = data["lyrics"].get("lines", [])
        return StandardLyrics(None,None, None, None,None,None,lyrics)
    
