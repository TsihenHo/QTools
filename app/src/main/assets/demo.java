// 这仅仅是一个示例脚本
// 作者：子恒

import me.tsihen.qtools.BuildConfig;

// 支持QQ复读机的库（需要您的设备上安装有QQ复读机）
// 支持库的版本取决于您安装的QQ复读机的版本
import com.bug.getpost.BugHttpClient;
import com.bug.getpost.Result;
import com.bug.utils.RegexUtils;

/*
下面是一些函数等
 */
if(false){
        print("监听接口库：");

        print("onMsg(Object data)");
        "接受文本消息时会调用，参数 data 是 MessageData 类型，详细使用方法见下文";

        print("onPicMsg(Object data)");
        "接受卡片消息时会调用，参数 data 是 MessageData 类型，详细使用方法见下文";

        print("onCardMsg(Object data)");
        "接受卡片消息时会调用，参数 data 是 MessageData 类型，详细使用方法见下文";

        print("onReplyMsg(Object data)");
        "接受回复消息时会调用，参数 data 是 MessageData 类型，详细使用方法见下文";

        print("onMixedMsg(Object data)");
        "接受图文消息时会调用，参数 data 是 MessageData 类型，详细使用方法见下文";

        print("onRawMsg(Object data)");
        "接受其他原始消息时会调用，参数 data 是 MessageData 类型，详细使用方法见下文";

        print("onJoin(String group, String person)");
        "当QQ号为 person 的新成员加入了QQ号为 group 的群聊时调用";

        print("onUnload()");
        "（此函数已被禁用）";

        print("MessageData 参数字段如下");
        "int type 消息类型 1文字 2图片 3xml卡片 4json卡片 5回复消息 6图文消息 0未处理原始消息";
        "boolean atMe 是否@自己";
        "String content 文字内容，卡片消息则为源代码";
        "String friendUin 如果是群聊，为群号，否则为发送者QQ号";
        "boolean isGroup 是否群聊";
        "String nickname/nickName 昵称";
        "String senderUin/sendUin 发送者QQ";
        "long time 消息时间";
        "String[] atList/atLIst @列表";
        "String source 卡片消息的源代码";
        }
        else if(false){

        print("函数库：");

        print("void print(String text)");
        "发送日志";

        print("void toast(String text) 或 Toast(String text)");
        "发送 Toast。";

        print("MessageData createData(String isGroup, String uin)");
        "手动创建一个 MessageData。isGroup 是否群聊，uin 为QQ号码";

        print("MessageData createPrivateChatData(String group, String person)");
        "手动从群聊创建一个临时会话的 MessageData。group 是群号码，person 是QQ号码";

        print("void send(MessageData data, String text)");
        "根据 data 发送内容为 text 的消息";

        print("void send(MessageData data, String text, String[] atList)");
        "根据 data 发送内容为 text 的消息并艾特 atlist 中的成员。atList 中存放被艾特的群员的QQ号";

        print("void record(MessageData data)");
        "根据 data 撤回消息";

        print("void sendCard(MessageData data, String source)");
        "根据 data 发送卡片消息。source: 源代码";

        print("void sendPhoto(MessageData data, String path)");
        "根据 data 发送图片消息。path: 路径";

        print("void sendTip(MessageData data, String text)");
        "根据 data 发送灰色提示消息，样式和“XXX撤回了一条消息”一样。text: 内容";

        print("void sendShowPhoto(MessageData data, String path, int type)");
        "根据 data 发送秀图，type: 0-秀图 1-幻影 2-抖动 3-生日 4-爱你 5-征友";

        print("void sendShake(MessageData data)");
        "根据 data 发送窗口抖动";

        print("void sendPtt(MessageData data, String path, String title, String showTime)");
        "根据 data 发送语音消息。path:路径，title:标题，showTime:误报时长。title 和 showTime 是可选的";
        "存在BUG";

        print("void sendReply(MessageData data, String text)");
        "根据 data 发送发送回复。text:文本";

        print("void shutUp(String group, String persson, long time)");
        "禁言群组(group)中的某个人(person)，时间为 time 秒。time 为 0 时为解封";

        print("void shutUp(String group, boolean enable)");
        "对群组(group)全员禁言。enable:是否开启";

        print("ArrayList<Friend> getFriends()");
        "返回好友列表。使用时不要用泛型。Friend 字段将会在后文解释。";

        print("Friend getFriendsInfo(String uin)");
        "返回好友列表中 QQ 号为 uin 的人";

        print("Friend 字段");
        "int age 年龄";
        "String alias 别名";
        "byte isIphoneOnline 是否手机QQ在线";
        "boolean isMqqOnLine 是否Mqq在线";
        "String name 昵称";
        "String signature 签名";
        "String singerName （？）";
        "String uin QQ号";
        "byte memberLevel 账号等级";
        }
        else if(false){

        print("字段库：");

        "context 代表qq上下文";
        "mName 自己昵称";
        "mQQ 自己QQ";
        }

/**
 * 新成员入群
 */
public void onJoin(String group,String member){
        print("新成员"+member+"加入群聊"+group);

        if(!group.equals("818333976"))return;
        send(createData(group,true),"你着都着了",new String[]{member});
        }

/**
 * 文本消息
 */
public void onMsg(Object data){
        print("收到文本消息了");
        if(!data.friendUin.equals("818333976"))return;
        sendReply(data,"不要在这里发消息");
        }

/**
 * 卡片消息
 */
public void onCardMsg(Object data){}
/**
 * 图片消息
 */
public void onPicMsg(Object data){}
/**
 * 图文混合消息
 */
public void onMixedMsg(Object data){}
/**
 * 其他消息
 */
public void onRawMsg(Object data){}
/**
 * 回复消息
 */
public void onReplyMsg(Object data){}

/**
 * 加载脚本时
 */

// 一些方法的使用示例：
        if(BuildConfig.DEBUG){
        send(createData("818333976",true),"你着都着了",new String[]{"3318448676"});
        print("Default script loaded.");

        // 这个不是我的 GoLink 链接
        sendCard(createData("818333976",true),"<?xml version='1.0' encoding='UTF-8' "+
        "standalone='yes' ?><msg serviceID=\"33\" templateID=\"123\" "+
        "action=\"web\" brief=\"【链接】Golink加速器-国内首款免费游戏加速器【官方\" "+
        "sourceMsgId=\"0\" url=\"https://www.golink.com/?code=JYPYKZWN\""+
        " flag=\"8\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"2\""+
        " advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://qq.ugcimg.cn/v1/o3upv4dbs"+
        "quu39i05lpnt57nmuaae2q4lus62r1u22o1cav00k7jus7po80am2j17r004ultmqfsq/s6vskamj00lmmk"+
        "t83jce822lfg\" w=\"0\" h=\"0\" /><title>QScript XML 消息测试</titl"+
        "e><summary>XML 消息</summary></ite"+
        "m><source name=\"\" icon=\"\" action=\"\" appid=\"-1\" /></msg>");

        sendPhoto(createData("818333976",true),"/sdcard/QQColor2/vip/fullBackground/chat/imgs_touch.jpeg");
        sendShowPhoto(createData("818333976",true),"/sdcard/QQColor2/vip/fullBackground/chat/imgs_touch.jpeg",2);
        sendShake(createData("818333976",true));
        sendPtt(createData("818333976",true),"/sdcard/sommething.mp3");

        Object f=getFriendsInfo("3340792396");
        print(f.name+", "+f.alias+", "+f.singerName);
        }