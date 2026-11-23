#!/bin/bash
# 从 LyricsDisplay 仓库同步 Python 源码到 Android 工程
#
#   bash scripts/sync_python.sh [/path/to/LyricsDisplay]
#
# 默认会去找 ../LyricsDisplay（两个仓库并排克隆时的常见布局）。
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="${1:-$HERE/../LyricsDisplay}"
DST="$HERE/app/src/main/python"

if [ ! -f "$SRC/main.py" ] || [ ! -d "$SRC/lyrics" ]; then
  echo "找不到 Python 源码：$SRC（需要 main.py 和 lyrics/）" >&2
  echo "用法： bash scripts/sync_python.sh /path/to/LyricsDisplay" >&2
  exit 1
fi

mkdir -p "$DST"
rm -rf "$DST/main.py" "$DST/lyrics"
cp "$SRC/main.py" "$DST/main.py"
cp -r "$SRC/lyrics" "$DST/lyrics"
find "$DST" -name '__pycache__' -type d -prune -exec rm -rf {} + 2>/dev/null || true

echo "已同步："
echo "  $SRC/main.py  -> $DST/main.py"
echo "  $SRC/lyrics/  -> $DST/lyrics/"
echo
echo "文件数：$(find "$DST" -name '*.py' | wc -l)"