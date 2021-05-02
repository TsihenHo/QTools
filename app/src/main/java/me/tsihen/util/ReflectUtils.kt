package me.tsihen.util

import me.tsihen.treflex.filter.FieldFilter
import me.tsihen.treflex.filter.MethodFilter
import me.tsihen.treflex.readField
import me.tsihen.treflex.readStaticField
import me.tsihen.treflex.writeField

lateinit var loader: ClassLoader
fun getClass(
    name: String
): Class<*> = loader.loadClass(name)

fun getClassOrNull(
    name: String
): Class<*>? = try {
    getClass(name)
} catch (e: Exception) {
    null
}

@Suppress("UNCHECKED_CAST")
fun <T> getObject(
    obj: Any,
    name: String,
    type: Class<T>? = null
): T? {
    return readField(
        obj,
        filter = FieldFilter(
            beStatic = false,
            name = name,
            type = type
        ),
        findInParent = true
    ) as T?
}

fun <T> getObjectOrNull(
    obj: Any,
    name: String,
    type: Class<T>? = null
): T? = try {
    getObject(obj, name, type)
} catch (e: NoSuchFieldException) {
    null
}

@Suppress("UNCHECKED_CAST")
fun <T> setObject(
    obj: Any,
    name: String,
    value: T?,
    type: Class<T>? = null
) {
    writeField(
        obj,
        filter = FieldFilter(
            beStatic = false,
            name = name,
            type = type
        ),
        findInParent = true,
        value = value
    )
}

@Suppress("UNCHECKED_CAST")
fun <T> getStaticObject(
    clz: Class<*>,
    name: String,
    type: Class<T>? = null
): T? {
    return readStaticField(
        clz,
        filter = FieldFilter(
            beStatic = true,
            name = name,
            type = type
        )
    ) as T?
}

fun Any.callVirtualMethod(
    name: String,
    vararg args: Any?
): Any? = me.tsihen.treflex.callMethod(this, MethodFilter(name = name), *args)

fun Class<*>.callStaticMethod(
    name: String,
    vararg args: Any?
): Any? = me.tsihen.treflex.callStaticMethod(this, MethodFilter(name = name), *args)