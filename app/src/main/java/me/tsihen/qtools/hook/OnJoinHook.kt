package me.tsihen.qtools.hook

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.tsihen.config.ClassConfig.qqAppInterfaceClz
import me.tsihen.qtools.script.ScriptManager
import me.tsihen.util.PACKAGE_QQ
import me.tsihen.util.getClass
import me.tsihen.xposed.AbsHook

object OnJoinHook : AbsHook() {
    override fun doInit(): Boolean {
        XposedHelpers.findAndHookMethod(
            getClass("$PACKAGE_QQ.service.message.codec.decoder.TroopAddMemberBroadcastDecoder"),
            "a",
            qqAppInterfaceClz,
            Int::class.java,
            String::class.java,
            String::class.java,
            Long::class.java,
            Long::class.java,
            Long::class.java,
            "msf.msgcomm.msg_comm\$MsgHead",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    super.beforeHookedMethod(param)
                    // args[3] : 群主
                    // args[5] : 入群时间戳，单位：s
                    ScriptManager.onJoin(
                        param.args[2].toString(),
                        param.args[4].toString()
                    )
                }
            }
        )
        return true
    }
}