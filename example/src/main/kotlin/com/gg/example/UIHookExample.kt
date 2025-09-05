@file:OptIn(ExperimentalMaterialApi::class)

package com.gg.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.Text
import com.xuncorp.spw.workshop.api.PlayerLyricsExtensionPoints
import org.pf4j.Extension

/**
 * 播放器歌词区域 UI 扩展示例
 *
 * 这个扩展会替代原有播放器的歌词区域显示一个自定义的 UI 组件
 * 开发时请注意Compose的重组机制，避免不必要的重组
 */
@Extension
class UIHookExample : PlayerLyricsExtensionPoints {
    @Composable
    override fun buildUi(modifier: Modifier) {
        var clickCount by remember { mutableStateOf(0) }

        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = 4.dp,
            onClick = {
                clickCount++
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Gray,
                            RoundedCornerShape(12.dp),
                        ).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "🎵 SPW 示例插件",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue,
                )

                Text(
                    text = "这是一个功能完整的示例插件",
                    fontSize = 14.sp,
                    color = Color.White,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Item("你好呀", "✅")
                    Item("Hello", "✅")
                    Item("Ciallo", "😡")
                }

                // 交互示例
                Text(
                    text = "点击次数: $clickCount",
                    fontSize = 12.sp,
                    color = Color.Green,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }

    /**
     * 可以自定义一个控件
     */
    @Composable
    private fun Item(
        label: String,
        status: String,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = status,
                fontSize = 16.sp,
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFFA6ADC8),
            )
        }
    }
}
