package me.tsihen.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.app.UiAutomation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.*
import android.view.KeyEvent
import android.view.MotionEvent
import de.robv.android.xposed.XposedHelpers
import me.tsihen.qtools.MainHook
import me.tsihen.treflex.callStaticMethod
import me.tsihen.treflex.filter.MethodFilter
import java.io.Serializable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

private var stubActivityInit = false
internal fun initForStubActivity(ctx: Context) {
    if (stubActivityInit) return
    hookAMS()
    hookInstrumentation()
    hookHandler()
    stubActivityInit = true
}

private fun hookAMS() {
    val objSingleton =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            getStaticObject<Any>(
                getClass("android.app.ActivityManager"),
                "IActivityManagerSingleton",
                null
            )
        else getStaticObject(getClass("android.app.ActivityManagerNative"), "gDefault", null)
    if (objSingleton == null) throw NullPointerException("无法获取 singleton")

    val mInstance = getObject<Any>(objSingleton, "mInstance")!!
    val proxyInstance = Proxy.newProxyInstance(
        Thread.currentThread().contextClassLoader,
        arrayOf(getClass("android.app.IActivityManager")),
        IActivityManagerHandler(mInstance)
    )
    setObject(objSingleton, "mInstance", proxyInstance)
    try {
        val singleton = getStaticObject<Any>(
            getClass("android.app.ActivityTaskManager"),
            "IActivityTaskManagerSingleton",
            null
        )!!
        getClass("android.util.Singleton").getMethod("get")(singleton)
        val defaultTaskMgr = getObject<Any>(singleton, "mInstance")!!
        val proxy = Proxy.newProxyInstance(
            MainHook::class.java.classLoader!!,
            arrayOf(getClass("android.app.IActivityTaskManager")),
            IActivityManagerHandler(defaultTaskMgr)
        )
        setObject(singleton, "mInstance", proxy)
    } catch (e: Throwable) {
    }
}

private fun hookInstrumentation() {
    val clzActivityThread = getClass("android.app.ActivityThread")
    val objCurrentActivityThread = callStaticMethod(
        clzActivityThread,
        MethodFilter(name = "currentActivityThread")
    )
    val fieldInstrumentation = clzActivityThread.getDeclaredField("mInstrumentation")
    fieldInstrumentation.isAccessible = true
    val objInstrumentation = fieldInstrumentation[objCurrentActivityThread] as Instrumentation
    fieldInstrumentation[objCurrentActivityThread] = MyInstrumentation(objInstrumentation)
}

private fun hookHandler() {
    val activityThread =
        XposedHelpers.getStaticObjectField(
            getClass("android.app.ActivityThread"),
            "sCurrentActivityThread"
        )
            ?: throw NullPointerException("找不到 activityThread")
    val mH = XposedHelpers.getObjectField(activityThread, "mH")
        ?: throw NullPointerException("找不到 mH")
    val mHCallbackField = XposedHelpers.findField(mH.javaClass, "mCallback")
    val mHCallback = mHCallbackField[mH]
    mHCallbackField[mH] = Handler.Callback { msg ->
        try {
            if (msg.what == 100) {
                val intentField = msg.obj.javaClass.getDeclaredField("intent")
                val targetIntent =
                    (intentField.get(msg.obj) as Intent).getParcelableExtra<Intent>(TARGET_INTENT)
                if (targetIntent != null) intentField.set(msg.obj, targetIntent)
            } else if (msg.what == 159) {
                val mActivityCallbacks = getObject(msg.obj, "mActivityCallbacks", List::class.java)
                mActivityCallbacks?.forEach {
                    if (it?.javaClass?.name == "android.app.servertransaction.LaunchActivityItem") {
                        val mIntentField = it.javaClass.getDeclaredField("mIntent")
                        mIntentField.isAccessible = true
                        val intent = mIntentField[it] as Intent
                        intent.getParcelableExtra<Intent>(TARGET_INTENT)?.let { proxyIntent ->
                            mIntentField[it] = proxyIntent
                        }
                    }
                }
            }
            (mHCallback as Handler.Callback?)?.handleMessage(msg) ?: false
        } catch (t: Throwable) {
            log.e(t)
            false
        }
    }
}

inline fun <reified T : Activity> Context.startActivity(vararg args: Pair<String, Any?>) {
    this.startActivity(createIntent(this, T::class.java, args))
}

fun createIntent(
    ctx: Context,
    clz: Class<out Activity>,
    args: Array<out Pair<String, Any?>>
): Intent {
    val intent = Intent(ctx, clz)
    args.forEach {
        when (val value = it.second) {
            null -> intent.putExtra(it.first, null as Serializable?)
            is Int -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is CharSequence -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Char -> intent.putExtra(it.first, value)
            is Short -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Serializable -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            is Parcelable -> intent.putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                else -> throw IllegalArgumentException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }
            is IntArray -> intent.putExtra(it.first, value)
            is LongArray -> intent.putExtra(it.first, value)
            is FloatArray -> intent.putExtra(it.first, value)
            is DoubleArray -> intent.putExtra(it.first, value)
            is CharArray -> intent.putExtra(it.first, value)
            is ShortArray -> intent.putExtra(it.first, value)
            is BooleanArray -> intent.putExtra(it.first, value)
            else -> throw IllegalArgumentException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
        }
        return@forEach
    }
    return intent
}

@Suppress("DEPRECATION")
private class MyInstrumentation(private val mBase: Instrumentation) : Instrumentation() {
    override fun newActivity(cl: ClassLoader?, className: String?, intent: Intent?): Activity {
        try {
            return mBase.newActivity(cl, className, intent)
        } catch (e: Throwable) {
            if (className?.startsWith("me.tsihen.") == true)
                return MainHook::class.java.classLoader!!.loadClass(className)
                    .newInstance() as Activity
            throw e
        }
    }

    override fun onCreate(arguments: Bundle?) {
        mBase.onCreate(arguments)
    }

    override fun onStart() {
        mBase.onStart()
    }

    override fun onDestroy() {
        mBase.onDestroy()
    }

    override fun start() {
        mBase.start()
    }

    override fun onException(obj: Any?, e: Throwable?): Boolean {
        return mBase.onException(obj, e)
    }

    override fun sendStatus(resultCode: Int, results: Bundle?) {
        mBase.sendStatus(resultCode, results)
    }

    @SuppressLint("NewApi")
    override fun addResults(results: Bundle?) {
        mBase.addResults(results)
    }


    override fun finish(resultCode: Int, results: Bundle?) {
        mBase.finish(resultCode, results)
    }

    override fun setAutomaticPerformanceSnapshots() {
        mBase.setAutomaticPerformanceSnapshots()
    }

    override fun startPerformanceSnapshot() {
        mBase.startPerformanceSnapshot()
    }

    override fun endPerformanceSnapshot() {
        mBase.endPerformanceSnapshot()
    }

    override fun getContext(): Context? {
        return mBase.context
    }

    override fun getComponentName(): ComponentName? {
        return mBase.componentName
    }

    override fun getTargetContext(): Context? {
        return mBase.targetContext
    }


    @SuppressLint("NewApi")
    override fun getProcessName(): String? {
        return mBase.processName
    }

    override fun isProfiling(): Boolean {
        return mBase.isProfiling
    }

    override fun startProfiling() {
        mBase.startProfiling()
    }

    override fun stopProfiling() {
        mBase.stopProfiling()
    }

    override fun setInTouchMode(inTouch: Boolean) {
        mBase.setInTouchMode(inTouch)
    }

    override fun waitForIdle(recipient: Runnable?) {
        mBase.waitForIdle(recipient)
    }

    override fun waitForIdleSync() {
        mBase.waitForIdleSync()
    }

    override fun runOnMainSync(runner: Runnable?) {
        mBase.runOnMainSync(runner)
    }

    override fun startActivitySync(intent: Intent?): Activity? {
        return mBase.startActivitySync(intent)
    }

    @SuppressLint("NewApi")
    override fun startActivitySync(intent: Intent, options: Bundle?): Activity {
        return mBase.startActivitySync(intent, options)
    }


    override fun addMonitor(monitor: ActivityMonitor?) {
        mBase.addMonitor(monitor)
    }

    override fun addMonitor(
        filter: IntentFilter?,
        result: ActivityResult?,
        block: Boolean
    ): ActivityMonitor? {
        return mBase.addMonitor(filter, result, block)
    }

    override fun addMonitor(
        cls: String?,
        result: ActivityResult?,
        block: Boolean
    ): ActivityMonitor? {
        return mBase.addMonitor(cls, result, block)
    }

    override fun checkMonitorHit(monitor: ActivityMonitor?, minHits: Int): Boolean {
        return mBase.checkMonitorHit(monitor, minHits)
    }

    override fun waitForMonitor(monitor: ActivityMonitor?): Activity? {
        return mBase.waitForMonitor(monitor)
    }

    override fun waitForMonitorWithTimeout(monitor: ActivityMonitor?, timeOut: Long): Activity? {
        return mBase.waitForMonitorWithTimeout(monitor, timeOut)
    }

    override fun removeMonitor(monitor: ActivityMonitor?) {
        mBase.removeMonitor(monitor)
    }

    override fun invokeMenuActionSync(targetActivity: Activity?, id: Int, flag: Int): Boolean {
        return mBase.invokeMenuActionSync(targetActivity, id, flag)
    }

    override fun invokeContextMenuAction(targetActivity: Activity?, id: Int, flag: Int): Boolean {
        return mBase.invokeContextMenuAction(targetActivity, id, flag)
    }

    override fun sendStringSync(text: String?) {
        mBase.sendStringSync(text)
    }

    override fun sendKeySync(event: KeyEvent?) {
        mBase.sendKeySync(event)
    }

    override fun sendKeyDownUpSync(key: Int) {
        mBase.sendKeyDownUpSync(key)
    }

    override fun sendCharacterSync(keyCode: Int) {
        mBase.sendCharacterSync(keyCode)
    }

    override fun sendPointerSync(event: MotionEvent?) {
        mBase.sendPointerSync(event)
    }

    override fun sendTrackballEventSync(event: MotionEvent?) {
        mBase.sendTrackballEventSync(event)
    }

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application? {
        return mBase.newApplication(cl, className, context)
    }

    override fun callApplicationOnCreate(app: Application?) {
        mBase.callApplicationOnCreate(app)
    }

    override fun newActivity(
        clazz: Class<*>?,
        context: Context?,
        token: IBinder?,
        application: Application?,
        intent: Intent?,
        info: ActivityInfo?,
        title: CharSequence?,
        parent: Activity?,
        id: String?,
        lastNonConfigurationInstance: Any?
    ): Activity? {
        return mBase.newActivity(
            clazz,
            context,
            token,
            application,
            intent,
            info,
            title,
            parent,
            id,
            lastNonConfigurationInstance
        )
    }

    override fun callActivityOnCreate(activity: Activity?, icicle: Bundle?) {
        activity?.let { MainHook.instance.injectModuleRes(it) }
        mBase.callActivityOnCreate(activity, icicle)
    }

    override fun callActivityOnCreate(
        activity: Activity?,
        icicle: Bundle?,
        persistentState: PersistableBundle?
    ) {
        activity?.let { MainHook.instance.injectModuleRes(it) }
        mBase.callActivityOnCreate(activity, icicle, persistentState)
    }


    override fun callActivityOnDestroy(activity: Activity?) {
        mBase.callActivityOnDestroy(activity)
    }

    override fun callActivityOnRestoreInstanceState(
        activity: Activity,
        savedInstanceState: Bundle
    ) {
        mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState)
    }


    override fun callActivityOnRestoreInstanceState(
        activity: Activity,
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        mBase.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState)
    }

    override fun callActivityOnPostCreate(activity: Activity, savedInstanceState: Bundle?) {
        mBase.callActivityOnPostCreate(activity, savedInstanceState)
    }

    override fun callActivityOnPostCreate(
        activity: Activity,
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        mBase.callActivityOnPostCreate(activity, savedInstanceState, persistentState)
    }

    override fun callActivityOnNewIntent(activity: Activity?, intent: Intent?) {
        mBase.callActivityOnNewIntent(activity, intent)
    }

    override fun callActivityOnStart(activity: Activity?) {
        mBase.callActivityOnStart(activity)
    }

    override fun callActivityOnRestart(activity: Activity?) {
        mBase.callActivityOnRestart(activity)
    }

    override fun callActivityOnResume(activity: Activity?) {
        mBase.callActivityOnResume(activity)
    }

    override fun callActivityOnStop(activity: Activity?) {
        mBase.callActivityOnStop(activity)
    }

    override fun callActivityOnSaveInstanceState(activity: Activity, outState: Bundle) {
        mBase.callActivityOnSaveInstanceState(activity, outState)
    }

    override fun callActivityOnSaveInstanceState(
        activity: Activity,
        outState: Bundle,
        outPersistentState: PersistableBundle
    ) {
        mBase.callActivityOnSaveInstanceState(activity, outState, outPersistentState)
    }

    override fun callActivityOnPause(activity: Activity?) {
        mBase.callActivityOnPause(activity)
    }

    override fun callActivityOnUserLeaving(activity: Activity?) {
        mBase.callActivityOnUserLeaving(activity)
    }

    override fun startAllocCounting() {
        mBase.startAllocCounting()
    }

    override fun stopAllocCounting() {
        mBase.stopAllocCounting()
    }

    override fun getAllocCounts(): Bundle? {
        return mBase.allocCounts
    }

    override fun getBinderCounts(): Bundle? {
        return mBase.binderCounts
    }

    override fun getUiAutomation(): UiAutomation? {
        return mBase.uiAutomation
    }

    @SuppressLint("NewApi")
    override fun getUiAutomation(flags: Int): UiAutomation? {
        return mBase.getUiAutomation(flags)
    }

    @SuppressLint("NewApi")
    override fun acquireLooperManager(looper: Looper?): TestLooperManager? {
        return mBase.acquireLooperManager(looper)
    }
}

class IActivityManagerHandler(private val mOrigin: Any) : InvocationHandler {
    @Throws(Throwable::class)
    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
        if ("startActivity" == method.name && args != null) {
            var index = -1
            for (i in args.indices) {
                if (args[i] is Intent) {
                    index = i
                    break
                }
            }
            if (index != -1) {
                val raw = args[index] as Intent
                val wrapper = Intent()
                wrapper.setClassName(
                    PACKAGE_QQ,
                    "com.tencent.mobileqq.activity.photo.CameraPreviewActivity"
                )
                wrapper.putExtra(TARGET_INTENT, raw)
                args[index] = wrapper
            }
        }
        return method.invoke(mOrigin, *(args ?: arrayOf()))
    }
}
