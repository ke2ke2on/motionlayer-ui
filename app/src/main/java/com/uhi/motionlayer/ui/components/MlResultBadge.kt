package com.uhi.motionlayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.uhi.motionlayer.ui.theme.MotionIconSize
import com.uhi.motionlayer.ui.theme.MotionSize
import com.uhi.motionlayer.ui.theme.MotionTheme

/**
 * 결과·빈 화면 가운데 큰 원의 색 묶음. Figma에서 화면의 뜻마다 색이 정해져 있다.
 *
 *  - [Success]: 방금 한 단계가 성공했다(ON-03 체험 성공, GS-08 만들기 완료, PR-04 연결됨). 초록
 *  - [Brand]: 흐름이 모두 끝났다(ON-11 준비 완료, GS-10 연습 완료, PR-13 발표 종료 요약). 파랑
 *  - [Danger]: 실패했다(PR-05 연결 실패). 빨강
 *  - [Neutral]: 보여 줄 것이 없거나 막혔다(AP-05 권한 없음, AP-08 검색 결과 없음, DV-09 인터넷 필요). 회색
 */
enum class MlResultTone { Success, Brand, Danger, Neutral }

/**
 * Figma 88×88 결과 원(모서리 44) 안에 아이콘 하나. 성공·완료는 체크 44, 실패·빈 화면은 40으로 그린다.
 *
 * @param iconSize 아이콘 크기. 모르면 톤에 맞는 Figma 값(성공·완료 44, 그 밖 40)을 쓴다
 */
@Composable
fun MlResultBadge(
    icon: MlIcon,
    tone: MlResultTone,
    modifier: Modifier = Modifier,
    iconSize: Dp = when (tone) {
        MlResultTone.Success, MlResultTone.Brand -> MotionIconSize.s44
        MlResultTone.Danger, MlResultTone.Neutral -> MotionIconSize.s40
    },
) {
    val colors = MotionTheme.colors
    val (container, content) = when (tone) {
        MlResultTone.Success -> colors.statusSuccessWeak to colors.statusSuccess
        MlResultTone.Brand -> colors.brandWeak to colors.brandPrimary
        MlResultTone.Danger -> colors.statusDangerWeak to colors.statusDanger
        MlResultTone.Neutral -> colors.fillWeak to colors.textTertiary
    }
    MlResultBadge(container = container, modifier = modifier) {
        MlIconView(icon = icon, tint = content, size = iconSize)
    }
}

/** 아이콘 대신 다른 내용(예: 연결하는 중의 회전 고리)을 [container] 색 원 안에 넣는다. */
@Composable
fun MlResultBadge(
    container: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(MotionSize.resultCircle)
            .clip(CircleShape)
            .background(container),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
