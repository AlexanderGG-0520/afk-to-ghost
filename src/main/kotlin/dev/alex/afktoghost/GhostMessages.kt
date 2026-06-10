package dev.alex.afktoghost

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.util.Locale

object GhostMessages {
    fun enterTitle(player: ServerPlayer): Component {
        return Component.literal(messagesFor(player).title)
    }

    fun enterSubtitle(player: ServerPlayer): Component {
        return Component.literal(messagesFor(player).subtitle)
    }

    fun actionbar(player: ServerPlayer): Component {
        return Component.literal(messagesFor(player).actionbar)
    }

    fun exit(player: ServerPlayer): Component {
        return Component.literal(messagesFor(player).exit)
    }

    private fun messagesFor(player: ServerPlayer): Bundle {
        val locale = player.clientInformation()
            .language()
            .lowercase(Locale.ROOT)

        return when {
            locale.startsWith("ja_") -> Japanese
            locale == "zh_cn" || locale == "zh_sg" -> SimplifiedChinese
            locale == "zh_tw" || locale == "zh_hk" || locale == "zh_mo" -> TraditionalChinese
            locale.startsWith("ko_") -> Korean
            else -> English
        }
    }

    private data class Bundle(
        val title: String,
        val subtitle: String,
        val actionbar: String,
        val exit: String,
    )

    private val English = Bundle(
        title = "AFK Ghost",
        subtitle = "You are protected while away.",
        actionbar = "AFK Ghost",
        exit = "AFK ghost mode ended.",
    )

    private val Japanese = Bundle(
        title = "AFK ゴースト",
        subtitle = "離席中は保護されています。",
        actionbar = "AFK ゴースト",
        exit = "AFK ゴーストモードを終了しました。",
    )

    private val SimplifiedChinese = Bundle(
        title = "暂离保护",
        subtitle = "离开期间不会受到伤害",
        actionbar = "暂离保护中 — 移动或操作即可解除",
        exit = "欢迎回来",
    )

    private val TraditionalChinese = Bundle(
        title = "暫離保護",
        subtitle = "離開期間不會受到傷害",
        actionbar = "暫離保護中 — 移動或操作即可解除",
        exit = "歡迎回來",
    )

    private val Korean = Bundle(
        title = "자리 비움 보호",
        subtitle = "자리를 비운 동안 피해를 받지 않습니다",
        actionbar = "자리 비움 보호 중 — 움직이거나 조작하면 해제됩니다",
        exit = "돌아오셨습니다",
    )
}
