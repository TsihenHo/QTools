package me.tsihen.config

import me.tsihen.util.PACKAGE_QQ
import me.tsihen.util.getClass

@Suppress("unused")
object ClassConfig {
    private val map: HashMap<Int, Class<*>> = hashMapOf()
    private fun get(id: Int, vararg path: String): Class<*> {
        if (map[id] != null) return map[id]!!

        path.forEach {
            try {
                val str = if (!it.startsWith("com")) "$PACKAGE_QQ.$it" else it
                map[id] = getClass(str)
                return map[id]!!
            } catch (e: Exception) {
            }
        }
        throw ClassNotFoundException("找不到类 ${path.contentToString()}")
    }

    @JvmStatic
    val messageHandlerUtilsClz: Class<*>
        get() = get(0, "app.MessageHandlerUtils")

    @JvmStatic
    val absStructMsgClz: Class<*>
        get() = get(1, "structmsg.AbsStructMsg")

    @JvmStatic
    val arkAppMessageClz: Class<*>
        get() = get(2, "data.ArkAppMessage")

    @JvmStatic
    val messageForPicClz: Class<*>
        get() = get(3, "data.MessageForPic")

    @JvmStatic
    val baseChatPieClz: Class<*>
        get() = get(4, "activity.BaseChatPie", "activity.aio.core.BaseChatPie")

    @JvmStatic
    val troopMemberInfoClz: Class<*>
        get() = get(5, "data.troop.TroopMemberInfo", "data.TroopMemberInfo")

    @JvmStatic
    val chatActivityFacadeClz: Class<*>
        get() = get(6, "activity.ChatActivityFacade")

    @JvmStatic
    val sessionInfoClz: Class<*>
        get() = get(7, "activity.aio.SessionInfo")

    @JvmStatic
    val qqAppInterfaceClz: Class<*>
        get() = get(8, "app.QQAppInterface")

    @JvmStatic
    val contactUtilsClz: Class<*>
        get() = get(9, "utils.ContactUtils")

    @JvmStatic
    val messageRecordClz: Class<*>
        get() = get(10, "data.MessageRecord")

    @JvmStatic
    val qqMessageFacadeClz: Class<*>
        get() = get(11, "app.message.QQMessageFacade", "com.tencent.imcore.message.QQMessageFacade")

    @JvmStatic
    val messageRecordFactoryClz: Class<*>
        get() = get(12, "service.message.MessageRecordFactory")

    @JvmStatic
    val messageCacheClz: Class<*>
        get() = get(13, "service.message.MessageCache")

    @JvmStatic
    val testStructMsgClz: Class<*>
        get() = get(14, "structmsg.TestStructMsg")

    @JvmStatic
    val chatMessageClz: Class<*>
        get() = get(15, "data.ChatMessage")

    @JvmStatic
    val qqManagerFactoryClz: Class<*>
        get() = get(16, "app.QQManagerFactory")
}