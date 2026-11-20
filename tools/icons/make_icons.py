"""Figma에서 내보낸 아이콘 경로(icons.json)를 안드로이드 VectorDrawable과 MlIcon enum으로 바꾼다.

모든 아이콘은 24x24, 선 두께 2, 둥근 끝(선만 있는 형태)이다. 색은 코드에서 tint로 정하므로 검정으로 둔다.
"""
import json
import pathlib
import sys

scratch = pathlib.Path(sys.argv[1])
project = pathlib.Path(sys.argv[2])

icons = json.loads((scratch / "icons.json").read_text(encoding="utf-8"))
drawable_dir = project / "app/src/main/res/drawable"
drawable_dir.mkdir(parents=True, exist_ok=True)

PATH = (
    '    <path\n'
    '        android:pathData="{d}"\n'
    '        android:strokeColor="#FF000000"\n'
    '        android:strokeWidth="2"\n'
    '        android:strokeLineCap="round"\n'
    '        android:strokeLineJoin="round" />\n'
)

problems = []
for name, items in sorted(icons.items()):
    body = ""
    for d, filled, stroked in items:
        if filled or not stroked:
            problems.append(f"{name}: 선이 아닌 요소 포함")
        body += PATH.format(d=d)
    xml = (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="24dp"\n'
        '    android:height="24dp"\n'
        '    android:viewportWidth="24"\n'
        '    android:viewportHeight="24">\n'
        f"{body}"
        "</vector>\n"
    )
    (drawable_dir / f"ic_{name.replace('-', '_')}.xml").write_text(xml, encoding="utf-8", newline="\n")


def pascal(name: str) -> str:
    return "".join(part.capitalize() for part in name.split("-"))


lines = [
    "package com.uhi.motionlayer.ui.components",
    "",
    "import androidx.annotation.DrawableRes",
    "import com.uhi.motionlayer.R",
    "",
    "/**",
    " * Figma \"01 파운데이션 > Icons · 24px, 2px stroke\"의 아이콘 전체. 이름은 Figma `icon/xxx`와 같다.",
    " * 이 파일은 `tools/icons/make_icons.py`로 생성했다. 아이콘이 바뀌면 `res/drawable/ic_*.xml`과 함께 다시 만든다.",
    " * 색은 쓰는 곳에서 tint로 정한다(드로어블은 검정 선).",
    " */",
    "enum class MlIcon(@param:DrawableRes val res: Int) {",
]
for name in sorted(icons):
    lines.append(f"    {pascal(name)}(R.drawable.ic_{name.replace('-', '_')}),")
lines.append("}")
lines.append("")

components_dir = project / "app/src/main/java/com/uhi/motionlayer/ui/components"
components_dir.mkdir(parents=True, exist_ok=True)
(components_dir / "MlIcon.kt").write_text("\n".join(lines), encoding="utf-8", newline="\n")

print(f"icons: {len(icons)}, problems: {problems}")
