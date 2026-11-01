#!/usr/bin/env python3
"""앱 소스에 쓰인 한글만 담은 Noto Sans KR 글꼴을 Google Fonts에서 받아 composeResources에 넣는다.

한글 문구나 데이터를 바꾼 뒤 다시 실행한다. 빠진 글자는 Compose의 자동 대체 글꼴이 몇 초 늦게 채운다.
요청에는 글자 목록만 들어가고, User-Agent는 파이썬 기본값을 쓴다.

사용법: python3 -I scripts/update_korean_font.py
"""
import glob
import os
import re
import urllib.parse
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "composeApp/src/commonMain/composeResources/font/noto_sans_kr.ttf")
# 번들한 Noto Serif에 없어 한글 글꼴에서 함께 가져오는 기호.
EXTRA = "←→"


def needed_text():
    chars = set()
    for path in glob.glob(os.path.join(ROOT, "composeApp/src/*Main/kotlin/**/*.kt"), recursive=True):
        with open(path, encoding="utf-8") as f:
            chars |= {c for c in f.read() if 0xAC00 <= ord(c) <= 0xD7A3}
    return "".join(sorted(chars)) + EXTRA


def main():
    text = needed_text()
    query = urllib.parse.urlencode({"family": "Noto Sans KR", "text": text})
    css = urllib.request.urlopen("https://fonts.googleapis.com/css2?" + query).read().decode()
    url = re.search(r"url\((https://[^)]+)\)", css).group(1)
    data = urllib.request.urlopen(url).read()
    if data[:4] != b"\x00\x01\x00\x00":
        raise SystemExit(f"TrueType 글꼴이 아닙니다(앞 4바이트 {data[:4]!r}). 응답 CSS: {css[:200]}")
    with open(OUT, "wb") as f:
        f.write(data)
    print(f"{len(text)}자, {len(data) // 1024}KB -> {os.path.relpath(OUT, ROOT)}")


if __name__ == "__main__":
    main()
