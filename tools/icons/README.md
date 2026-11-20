# 아이콘 생성 도구

Figma "01 파운데이션 > Icons · 24px, 2px stroke"의 아이콘(64개)을 안드로이드 벡터 드로어블로 바꾼다.

- `icons.json`: Figma에서 SVG로 내보낸 경로 데이터 (이름 → [경로, 채움 여부, 선 여부])
- `make_icons.py`: 이 데이터로 `app/src/main/res/drawable/ic_*.xml`과 `ui/components/MlIcon.kt`를 만든다

다시 만들 때 (프로젝트 루트에서):

    python tools/icons/make_icons.py tools/icons .

아이콘이 바뀌면 Figma에서 다시 내보내 `icons.json`을 갱신한 뒤 위 명령을 실행한다.
