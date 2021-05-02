package me.tsihen.qtools.script

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.newInstance
import me.tsihen.config.ClassConfig.absStructMsgClz
import me.tsihen.config.ClassConfig.arkAppMessageClz
import me.tsihen.config.ClassConfig.chatActivityFacadeClz
import me.tsihen.config.ClassConfig.chatMessageClz
import me.tsihen.config.ClassConfig.contactUtilsClz
import me.tsihen.config.ClassConfig.messageCacheClz
import me.tsihen.config.ClassConfig.messageForPicClz
import me.tsihen.config.ClassConfig.messageRecordClz
import me.tsihen.config.ClassConfig.messageRecordFactoryClz
import me.tsihen.config.ClassConfig.qqAppInterfaceClz
import me.tsihen.config.ClassConfig.qqMessageFacadeClz
import me.tsihen.config.ClassConfig.sessionInfoClz
import me.tsihen.config.ClassConfig.testStructMsgClz
import me.tsihen.config.ConfigManager.Companion.config
import me.tsihen.qtools.hook.PttWrongTimeHook
import me.tsihen.treflex.callMethod
import me.tsihen.treflex.callStaticMethod
import me.tsihen.treflex.canCastTo
import me.tsihen.treflex.filter.MethodFilter
import me.tsihen.treflex.getFirstMethod
import me.tsihen.util.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList

@Suppress("unused")
object ScriptApi {
    @JvmStatic
    fun getNickname(senderUin: String, friendUin: String): String =
        getClass("$PACKAGE_QQ.utils.ContactUtils")
            .callStaticMethod(
                ".*",
                qqAppInterface,
                senderUin,
                friendUin,
                1,
                0
            ) as String

    @JvmStatic
    fun print(str: String) {
        log.i("脚本日志：$str")
    }

    @JvmStatic
    fun toast(str: String) {
        getQQApplication()?.let {
            Toast.makeText(it, str, Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    @JvmOverloads
    fun send(data: MessageData, msg: String, atList: Array<String?>? = arrayOf()) {
        val arrayList = arrayListOf<Any>()
        val sb = StringBuilder()
        if (atList != null && atList.isNotEmpty()) {
            val clz = try {
                Class.forName(
                    "com.tencent.mobileqq.data.MessageForText\$AtTroopMemberInfo",
                    false,
                    loader
                )
            } catch (e: ClassNotFoundException) {
                Class.forName("com.tencent.mobileqq.data.AtTroopMemberInfo", false, loader)
            }
            atList.forEachIndexed { _, l ->
                if (l == null) return@forEachIndexed
                val msgObj = clz.newInstance()
                XposedHelpers.setByteField(msgObj, "flag", if (l == "0") 1 else 0)
                XposedHelpers.setLongField(msgObj, "uin", l.toLong())
                XposedHelpers.setShortField(msgObj, "startPos", sb.length.toShort())
                XposedHelpers.setShortField(
                    msgObj,
                    "textLen",
                    (getNickname(l, data.friendUin).length + 1).toShort()
                )
                arrayList.add(msgObj)
                sb.append("@${getNickname(l, data.friendUin)} ")
            }
        }

        sb.append(msg)
        methodSendText.invoke(
            null,
            qqAppInterface,
            getQQApplicationNonNull(),
            buildSessionInfo(data.friendUin, data.isGroup),
            sb.toString(),
            arrayList,
            sendMsgParamsClass.newInstance()
        )
    }

    @JvmStatic
    fun createData(uin: String, isGroup: Boolean) = MessageData().apply {
        friendUin = uin
        this.isGroup = isGroup
    }

    @JvmStatic
    fun createPrivateChatData(group: String, person: String): MessageData {
        val session = sessionInfoClz.newInstance()
        setObject(session, "b|realTroopInfo", group)
        setObject(session, "c|troopInfo", group)
        setObject(session, "a|curFriendUin", person)
        setObject(session, "a|curType", 1000)
        val res = MessageData()
        res.apply {
            isGroup = false
            friendUin = person
            senderUin = getAccountUin()
            sendUin = senderUin
        }
        return res
    }

    @JvmStatic
    fun getCurrActivity() = me.tsihen.util.getCurrActivity()

    @JvmStatic
    fun record(data: MessageData) {
        try {
            if (qqAppInterface == null) {
                log.w("QQAppInterface == null.")
                return
            }
            val msgFacade = getFirstMethod(
                qqAppInterfaceClz,
                true,
                MethodFilter("a|getMessageFacade", returnType = qqMessageFacadeClz)
            )(
                qqAppInterface
            )
            val msgRecord = getFirstMethod(
                messageRecordFactoryClz,
                true,
                MethodFilter(
                    "a",
                    returnType = messageRecordClz,
                    paramTypes = arrayOf(messageRecordClz)
                )
            )(null, data.__msg)!!
            setObject(
                msgRecord,
                "time",
                (getObject(
                    msgRecord,
                    "time",
                    Long::class.java
                )!! - Math.random() * 10.0 + 1.0).toLong()
            )
            callMethod(
                qqAppInterface!!, MethodFilter(
                    returnType = messageCacheClz,
                    name = "a|getMsgCache"
                )
            )?.callVirtualMethod("b", true)
            callMethod(
                msgFacade!!,
                MethodFilter(name = "d|revokeMsgByMessageRecord", returnType = Void.TYPE),
                msgRecord
            )
            callMethod(
                msgFacade,
                MethodFilter(name = "c", returnType = Void.TYPE),
                msgRecord
            )
        } catch (e: Throwable) {
            log.e(e)
        }
    }

    @JvmStatic
    fun sendCard(data: MessageData, source: String) {
        val sb = StringBuilder()
        val session = buildSessionInfo(data.friendUin, data.isGroup)
        try {
            val arkAppMsg = arkAppMessageClz.newInstance()
            if (!(arkAppMsg.callVirtualMethod(
                    "fromAppXml",
                    source
                ) as Boolean)
            ) throw Exception("不满足 JSON 语法")
            methodSendArk.invoke(null, qqAppInterface, session, arkAppMsg)
            return
        } catch (e: Exception) {
            sb.append(e.message)
        }

        try {
            val absStructMsg = callStaticMethod(
                testStructMsgClz,
                MethodFilter(
                    paramTypes = arrayOf(String::class.java),
                    returnType = absStructMsgClz
                ),
                source
            )
            methodSendAbsStruct.invoke(null, qqAppInterface, session, absStructMsg)
            return
        } catch (e: Exception) {
            sb.append(e.message)
        }

        log.e(IllegalArgumentException(sb.toString()))
    }

    @JvmStatic
    fun sendPhoto(data: MessageData, path: String) {
        try {
            val session = buildSessionInfo(data.friendUin, data.isGroup)
            val chatMessage = callStaticMethod(
                chatActivityFacadeClz,
                MethodFilter(
                    "a",
                    paramTypes = arrayOf(qqAppInterfaceClz, sessionInfoClz, String::class.java),
                    returnType = chatMessageClz
                ),
                qqAppInterface,
                session,
                path
            )
            methodSendPic.invoke(null, qqAppInterface, session, chatMessage, 0)
        } catch (e: Throwable) {
            log.e(e)
        }
    }

    const val MSG_TYPE_TIP = -1013
    const val MSG_TYPE_SHAKE = -2020
    const val MSG_TYPE_SHOW_PHOTO = -5015

    @JvmStatic
    fun sendTip(data: MessageData, text: String) {
        try {
            val msg = messageRecordFactoryClz.callStaticMethod("a", MSG_TYPE_TIP)!!
            msg.callVirtualMethod(
                "init",
                getAccountUin(),
                data.friendUin,
                data.senderUin,
                text,
                data.time,
                MSG_TYPE_TIP,
                if (data.isGroup) 1 else 0,
                data.time
            )
            setObject(msg, "shmsgseq", data.time)
            setObject(msg, "isread", true)
            callMethod(
                qqAppInterface!!,
                MethodFilter(name = "a|getMessageFacade", returnType = qqMessageFacadeClz)
            )!!.callVirtualMethod("a|addMessage", msg, getAccountUin())
        } catch (e: Throwable) {
            log.e(e)
        }
    }

    @JvmStatic
    fun sendShowPhoto(data: MessageData, path: String, type: Int) {
        try {
            val session = buildSessionInfo(data.friendUin, data.isGroup)
            val photoMsg = callStaticMethod(
                chatActivityFacadeClz,
                MethodFilter(
                    "a",
                    paramTypes = arrayOf(qqAppInterfaceClz, sessionInfoClz, String::class.java),
                    returnType = chatMessageClz
                ),
                qqAppInterface,
                session,
                path
            )!!
            val isTroop: Int = getObject(photoMsg, "istroop", Int::class.java) as Int
            val time = getObject(photoMsg, "time", Long::class.java) as Long
            val msgData = getObject(photoMsg, "msgData", ByteArray::class.java) as ByteArray

            val picRec = getClass("localpb.richMsg.RichMsg\$PicRec").newInstance()
            picRec.callVirtualMethod("mergeFrom", msgData)
            val resvAttr =
                getClass("tencent.im.msg.hummer.resv3.CustomFaceExtPb\$ResvAttr").newInstance()
            val image2show: Any = getObject(resvAttr, "msg_image_show")!!
            getObject<Any>(image2show, "int32_effect_id", null)!!.callVirtualMethod(
                "set",
                type + 40000
            )
            image2show.callVirtualMethod("setHasFlag", true)

            val byteStringMicro = newInstance(
                getClass("com.tencent.mobileqq.pb.ByteStringMicro"),
                resvAttr.callVirtualMethod("toByteArray")
            )
            getObject<Any>(picRec, "bytes_pb_reserved", null)!!.callVirtualMethod(
                "set",
                byteStringMicro
            )

            val msgData2 = picRec.callVirtualMethod("toByteArray") as ByteArray
            val finalMsg =
                getClass("com.tencent.mobileqq.data.MessageForTroopEffectPic").newInstance()
            val paramString = if (isTroop == 0) data.friendUin else data.senderUin

            finalMsg.callVirtualMethod(
                "init",
                getAccountUin(),
                paramString,
                data.senderUin,
                "QQ工具",
                1L,
                MSG_TYPE_SHOW_PHOTO,
                isTroop,
                1L
            )
            setObject(
                finalMsg,
                "msgUid",
                getObject(photoMsg, "msgUid", Long::class.java)!! + Random().nextInt()
            )
            setObject(finalMsg, "shmsgseq", getObject<Any>(photoMsg, "shmsgseq"))
            setObject(finalMsg, "isread", true)
            setObject(finalMsg, "msgData", msgData2)
            finalMsg.callVirtualMethod("doParse")
            setObject(finalMsg, "msgtype", MSG_TYPE_SHOW_PHOTO)
            chatActivityFacadeClz.callStaticMethod(
                "a",
                qqAppInterface,
                session,
                finalMsg,
                0
            )
        } catch (e: Throwable) {
            log.e(e)
        }
    }

    @JvmStatic
    fun sendShake(data: MessageData) {
        val qNum = data.friendUin
        val isGroup = data.isGroup
        try {
            val factory = messageRecordFactoryClz
            val shake = XposedHelpers.callStaticMethod(factory, "a", MSG_TYPE_SHAKE)
            XposedHelpers.setObjectField(shake, "msg", "抖动")
            val shakeMsg =
                XposedHelpers.findField(shake.javaClass, "mShakeWindowMsg").type.newInstance()
            XposedHelpers.setIntField(shakeMsg, "mType", 0)
            XposedHelpers.setIntField(shakeMsg, "mReserve", 0)
            XposedHelpers.setIntField(shakeMsg, "onlineFlag", 1)
            XposedHelpers.setBooleanField(shakeMsg, "shake", true)
            XposedHelpers.setObjectField(shake, "msgData", shakeMsg.callVirtualMethod("getBytes"))
            val curr = getAccountUin()
            val time = getSecondTimestamp(Date(System.currentTimeMillis()))
            shake.callVirtualMethod(
                "initInner",
                curr,
                qNum,
                curr,
                "value",
                time.toLong(),
                Integer.valueOf(MSG_TYPE_SHAKE),
                Integer.valueOf(if (isGroup) 1 else 0),
                time.toLong()
            )
            XposedHelpers.callMethod(
                shake,
                "parse"
            )
            qqAppInterface!!.callVirtualMethod("getMessageFacade")!!.callVirtualMethod(
                "a|addAndSendMessage",
                messageRecordFactoryClz.callStaticMethod("a", shake),
                null,
                true
            )
        } catch (e: Throwable) {
            log.e(e)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun sendPtt(
        data: MessageData,
        path: String,
        title: String = "QQ工具语音",
        showTime: String = config[PttWrongTimeHook.getId()]?.toString() ?: "0"
    ) {
        try {
            val session = buildSessionInfo(data.friendUin, data.isGroup)
            val curFriendUin = getObject<String>(session, "a|curFriendUin")
            val curType = getObject<Int>(session, "a|curType")
            val msg = callStaticMethod(
                chatActivityFacadeClz,
                MethodFilter(returnType = messageRecordClz),
                qqAppInterface,
                path,
                session,
                -2,
                1
            )!!
            setObject(msg, "c2cViaOffline", true)
            setObject(msg, "sttText", title)
            msg.callVirtualMethod("buileDefaultWaveform")
            msg.callVirtualMethod("serial")
            val uniseq = getObject(msg, "uniseq", Long::class.java)
            var c: Int
            if (showTime.isNotEmpty() && (showTime.matches("-?\\\\d+".toRegex()) || showTime.matches(
                    "^-?(\\d*):(\\d|[0-5]\\d|60)$".toRegex()
                ))
            ) {
                if (showTime.matches("^-?(\\d*):(\\d|[0-5]\\d|60)$".toRegex())) {
                    val match =
                        "^-?(\\d*):(\\d|[0-5]\\d|60)$".toRegex().find(showTime)
                            ?.next()?.groupValues!!
                    val str = match[0]
                    c = if (str.isEmpty()) Char.MIN_VALUE.toInt()
                    else str.toInt()
                    val k = c * 60 + match[1].toInt()
                    c = k
                    if (showTime.startsWith('-')) c = k * -1
                } else {
                    c = showTime.toInt()
                }
                c *= 1000
            } else {
                c = '✐'.toInt()
            }
            val bundle = Bundle()
            bundle.putInt("DiyTextId", 0)
            val voiceLength = c / 1000
            setObject(msg, "voiceLength", voiceLength)
            callStaticMethod(
                chatActivityFacadeClz,
                MethodFilter(returnType = Void.TYPE),
                qqAppInterface,
                curType,
                curFriendUin,
                path,
                1,
                false,
                c,
                1,
                0,
                3,
                true,
                0L,
                bundle,
                arrayListOf<Any>(),
                title,
                false,
                msg,
                0
            )
            callStaticMethod(
                chatActivityFacadeClz,
                MethodFilter(returnType = Void.TYPE),
                qqAppInterface,
                path,
                -3,
                1L
            )
            setObject(msg, "voiceLength", voiceLength)
            callMethod(
                qqAppInterface!!,
                MethodFilter(returnType = qqMessageFacadeClz, name = "a|getMessageFacade")
            )!!.callVirtualMethod("a|addMessage", msg, getAccountUin())
        } catch (e: Throwable) {
            log.e(e)
        }
    }

    init {
        doInit()
    }

    private lateinit var methodSendAbsStruct: Method
    private lateinit var methodSendArk: Method
    private lateinit var methodSendPic: Method
    private lateinit var methodSendText: Method
    private lateinit var sendMsgParamsClass: Class<*>

    fun buildSessionInfo(qNum: String, isGroup: Boolean = false): Any {
        val s = sessionInfoClz.newInstance()
        setObject(s, "a", qNum, String::class.java)
        setObject(s, "a", System.currentTimeMillis(), Long::class.java)
        setObject(s, "a", if (isGroup) 1 else 0, Int::class.java)
        setObject(s, "b", 32, Int::class.java)
        setObject(s, "c", 1, Int::class.java)
        setObject(s, "d", 10004, Int::class.java)
        return s
    }

    private fun doInit() {
        for (m in chatActivityFacadeClz.declaredMethods) {
            val clz = m.parameterTypes
            if (clz.size == 3) {
                if (clz[0].name == qqAppInterfaceClz.name &&
                    clz[1].name == sessionInfoClz.name
                ) {
                    if (clz[2].name == arkAppMessageClz.name && m.returnType == Boolean::class.java)
                        methodSendArk = m
                    else if (clz[2].name == absStructMsgClz.name && m.returnType == Void.TYPE)
                        methodSendAbsStruct = m
                }
            } else if (clz.size == 4) {
                if (clz[0].name == qqAppInterfaceClz.name &&
                    clz[1].name == sessionInfoClz.name &&
                    clz[2].name == messageForPicClz.name &&
                    clz[3] == Int::class.java &&
                    m.returnType == Void.TYPE
                ) {
                    methodSendPic = m
                }
            } else if (clz.size == 6) {
                if (clz[0].name == qqAppInterfaceClz.name &&
                    clz[1] == Context::class.java &&
                    clz[2].name == sessionInfoClz.name &&
                    clz[3] == String::class.java &&
                    clz[4] == ArrayList::class.java &&
                    m.returnType.name == "[J"
                ) {
                    methodSendText = m
                    sendMsgParamsClass = clz[5]
                }
            }

            if (
                ::methodSendAbsStruct.isInitialized
                && ::methodSendArk.isInitialized
                && ::methodSendPic.isInitialized
                && ::methodSendText.isInitialized
            )
                break
        }
    }
}

open class MessageData {
    @JvmField
    var senderUin: String = ""

    @JvmField
    var sendUin: String = ""

    @JvmField
    var content: String = ""

    @JvmField
    var content2: String = ""

    @JvmField
    var isGroup = false

    @JvmField
    var atMe = false

    @JvmField
    var time = -1L

    @JvmField
    var friendUin: String = ""

    @JvmField
    var nickname: String = ""

    @JvmField
    var nickName: String = ""

    @JvmField
    var sessionInfo: Any? = null

    @JvmField
    var selfUin = ""

    @JvmField
    var id = -1L

    @JvmField
    var atList: Array<String>? = null

    @JvmField
    var atLIst: Array<String>? = null

    @JvmField
    var source: String = ""

    @JvmField
    var type = 0

    var __msg: Any? = null

    companion object {
        fun getMessage(qqAppInterface: Any, messageRecord: Any): MessageData {
            val data = MessageData()
            try {
                val isTroop = getObject<Int>(messageRecord, "istroop")
                val senderUin = getObject<String>(messageRecord, "senderuin") ?: ""
                val session = sessionInfoClz.newInstance()
                setObject(session, "a", isTroop, Int::class.java)
                setObject(session, "a", senderUin, String::class.java)
                val atList = LinkedList<String>()
                try {
                    val jsonObject: JSONObject = getObject(messageRecord, "mExJsonObject")
                        ?: throw NoSuchFieldException("ignored")
                    if (jsonObject.has("troop_at_info_list")) {
                        val atMemberString = jsonObject.getString("troop_at_info_list")
                        val atMemberArray: JSONArray? = JSONArray(atMemberString)
                        if (atMemberArray != null) {
                            for (i: Int in 0..atMemberArray.length()) {
                                atList.add(atMemberArray.getJSONObject(i).getLong("uin").toString())
                            }
                        }
                    }
                } catch (ignored: Throwable) {
                }

                data.__msg = messageRecord
                data.atMe = atList.contains(getLongAccountUin().toString())
                data.atList = atList.toTypedArray()
                data.atLIst = data.atList
                data.sessionInfo = session
                data.senderUin = senderUin
                data.sendUin = data.senderUin
                data.selfUin = getObject<String>(messageRecord, "selfuin") ?: ""
                data.friendUin = getObject<String>(messageRecord, "frienduin") ?: ""
                data.time = getObject<Long>(messageRecord, "time") ?: -1L
                data.isGroup = isTroop == 1
                data.content = getObject<String>(messageRecord, "msg") ?: ""
                data.content2 = getObject<String>(messageRecord, "msg2") ?: ""
                data.id = getObject<Long>(messageRecord, "msgUid") ?: {
                    log.w("MessageData : GetMessage : 找不到 MsgId")
                    -1L
                }.invoke()
                data.nickname = contactUtilsClz.callStaticMethod(
                    "a",
                    qqAppInterface,
                    data.senderUin,
                    data.friendUin,
                    1,
                    0
                ) as? String? ?: ""
                data.nickName = data.nickname

                val clz = messageRecord.javaClass
                when {
                    clz canCastTo loader.loadClass("$PACKAGE_QQ.data.MessageForText") -> data.type =
                        1
                    clz canCastTo loader.loadClass("$PACKAGE_QQ.data.MessageForPic") -> data.type =
                        2
                    clz canCastTo loader.loadClass("$PACKAGE_QQ.data.MessageForStructing") -> {
                        data.type = 3
                        data.source = getObject<Any>(
                            messageRecord,
                            "structingMsg"
                        )?.callVirtualMethod("getXml") as? String? ?: ""
                        data.content = data.source
                    }
                    clz canCastTo loader.loadClass("$PACKAGE_QQ.data.MessageForArkApp") -> {
                        data.type = 4
                        data.source = getObject<Any>(
                            messageRecord,
                            "ark_app_message"
                        )?.callVirtualMethod("toAppXml") as? String? ?: ""
                        data.content = data.source
                    }
                    clz canCastTo loader.loadClass("$PACKAGE_QQ.data.MessageForReplyText") -> data.type =
                        5
                    clz canCastTo loader.loadClass("$PACKAGE_QQ.data.MessageForMixedMsg") -> data.type =
                        6
                    else -> data.type = 0
                }
            } catch (e: Exception) {
                log.e(e)
            }
            return data
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class UnsafeApi