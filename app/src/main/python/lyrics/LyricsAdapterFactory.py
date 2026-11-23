from lyrics.enums import Sources
from lyrics.adapters.FormatSpotifyAdapter import FormatSpotifyAdapter
from lyrics.adapters.FormatKugouAdapter import FormatKugouAdapter
from lyrics.adapters.base import LyricsAdapter

class LyricsAdapterFactory:
    @staticmethod
    def get_adapter(format_type: Sources) -> LyricsAdapter:
        if format_type == Sources.Spotify:
            return FormatSpotifyAdapter()
        elif format_type == Sources.Kugou:
           return FormatKugouAdapter()
        elif format_type == Sources.Other:
            raise ValueError("Source 'Other' is not supported yet")
        else:
            raise ValueError(f"Unsupported lyrics format: {format_type}")
