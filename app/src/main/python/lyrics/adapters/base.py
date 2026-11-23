# adapters/base.py

from abc import ABC, abstractmethod
from lyrics.models.standardlyrics import StandardLyrics

class LyricsAdapter(ABC):
    @abstractmethod
    def convert(self, data: dict) -> StandardLyrics:
        """将原始数据转换为 StandardLyrics 对象"""
        pass
    
    
