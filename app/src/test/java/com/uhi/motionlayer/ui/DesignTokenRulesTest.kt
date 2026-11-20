package com.uhi.motionlayer.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 디자인 토큰 규칙(색·글자·모서리·간격은 토큰으로만 쓴다)을 코드에서 지키고 있는지 검사한다.
 * 화면·컴포넌트 코드(`ui/`)에 색·글자 크기·모서리·간격·아이콘 크기·선 두께·그림자를 숫자로 직접 쓰면 실패한다.
 * 값이 필요하면 `ui/theme/`에 이름을 먼저 추가하고 그 이름을 쓴다. 디자인 확인용 화면(`ui/preview`)과 시연용 창(`ui/debug`)은 제외한다.
 */
class DesignTokenRulesTest {
    private class Rule(val name: String, val pattern: Regex, val hint: String)

    private val rules = listOf(
        Rule("색을 숫자로 씀", Regex("""Color\(\s*0x"""), "MotionTheme.colors.xxx를 쓴다"),
        Rule("기본 색 이름을 씀", Regex("""\bColor\.(White|Black|Gray|DarkGray|LightGray|Red|Green|Blue|Yellow|Cyan|Magenta)\b"""), "MotionTheme.colors.xxx를 쓴다"),
        Rule("글자 크기를 숫자로 씀", Regex("""\b\d+(\.\d+)?\.sp\b"""), "MotionTheme.typography.xxx를 쓴다"),
        Rule("모서리를 숫자로 씀", Regex("""RoundedCornerShape\([^)]*\b\d+(\.\d+)?(\.dp)?\b"""), "MotionRadius.rNN을 쓴다"),
        Rule("간격을 숫자로 씀(spacedBy)", Regex("""spacedBy\(\s*\d"""), "MotionSpacing.sNN을 쓴다"),
        Rule("여백을 숫자로 씀(padding)", Regex("""\.padding\([^)]*\b\d+(\.\d+)?\.dp\b"""), "MotionSpacing.sNN을 쓴다"),
        Rule("선 두께를 숫자로 씀(border)", Regex("""\.border\(\s*(width\s*=\s*)?\d"""), "MotionBorder.xxx를 쓴다"),
        Rule("아이콘 크기를 숫자로 씀", Regex("""MlIconView\([^)]*\bsize\s*=\s*\d"""), "MotionIconSize.sNN을 쓴다"),
        Rule("그림자를 직접 그림", Regex("""\.shadow\("""), "motionShadow(MotionShadows.xxx, 모양)을 쓴다"),
    )

    private val exemptFolders = listOf("/theme/", "/preview/", "/debug/")

    private fun uiSources(): List<File> {
        // 단위 테스트의 작업 폴더는 app 모듈이다.
        val root = File("src/main/java/com/uhi/motionlayer/ui")
        assertTrue("ui 폴더를 찾지 못했어요: ${root.absolutePath}", root.isDirectory)
        return root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { file -> exemptFolders.none { file.invariantSeparatorsPath.contains(it) } }
            .toList()
    }

    @Test
    fun screensAndComponentsUseDesignTokensOnly() {
        val violations = mutableListOf<String>()
        for (file in uiSources()) {
            file.readLines().forEachIndexed { index, line ->
                val code = line.trim()
                // 설명(주석)에 쓴 숫자는 검사하지 않는다.
                if (code.startsWith("//") || code.startsWith("*") || code.startsWith("/*")) return@forEachIndexed
                for (rule in rules) {
                    if (rule.pattern.containsMatchIn(code)) {
                        violations += "${file.name}:${index + 1}  [${rule.name}] ${rule.hint}  →  $code"
                    }
                }
            }
        }
        assertTrue(
            "디자인 토큰 규칙을 어긴 곳이 ${violations.size}군데 있어요:\n" + violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }

    /**
     * 글자는 모두 `ui/components/Text.kt`의 `Text`로 그린다(보고서 6.3 "한글 낱말 단위 줄바꿈").
     * Material의 `Text`를 직접 import하면 한글이 "나 / 와요"처럼 낱말 가운데서 갈라진다.
     * 디자인 확인용 화면(`ui/preview`)과 그 `Text` 파일 자신은 제외한다.
     */
    @Test
    fun textIsDrawnWithTheAppText() {
        val root = File("src/main/java/com/uhi/motionlayer/ui")
        assertTrue("ui 폴더를 찾지 못했어요: ${root.absolutePath}", root.isDirectory)
        val violations = mutableListOf<String>()
        root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { !it.invariantSeparatorsPath.contains("/preview/") && it.name != "Text.kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    if (Regex("""androidx\.compose\.material3\.Text\b""").containsMatchIn(line)) {
                        violations += "${file.name}:${index + 1}  import com.uhi.motionlayer.ui.components.Text 를 쓴다  →  ${line.trim()}"
                    }
                }
            }
        assertTrue(
            "Material Text를 직접 쓴 곳이 ${violations.size}군데 있어요:\n" + violations.joinToString("\n"),
            violations.isEmpty(),
        )
    }
}
