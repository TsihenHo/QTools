package me.tsihen.config

import me.tsihen.util.defaultDirPath
import java.io.*

class ConfigManager private constructor(val file: File) {
    companion object {
        @JvmStatic
        val config: ConfigManager by lazy {
            ConfigManager(File("$defaultDirPath${File.separator}config.dat").apply { if (!this.exists()) createNewFile() })
        }
    }

    @Suppress("UNCHECKED_CAST")
    private var hashMap: java.util.HashMap<Serializable, Serializable> = if (file.exists()) {
        try {
            val objIn = ObjectInputStream(FileInputStream(file))
            objIn.readObject() as HashMap<Serializable, Serializable>
        } catch (ignored: Throwable) {
            hashMapOf()
        }
    } else {
        hashMapOf()
    }

    operator fun set(key: Serializable, value: Serializable) {
        hashMap[key] = value
        save()
    }

    operator fun get(key: Serializable) = hashMap[key]

    @Suppress("UNCHECKED_CAST")
    fun <T: Serializable, R: Serializable> getOr(key: T, default: R): R {
        return hashMap[key] as R? ?: default
    }

    private fun save() {
        if (!file.exists()) file.createNewFile()
        val objOut = ObjectOutputStream(FileOutputStream(file))
        objOut.writeObject(hashMap)
        objOut.close()
    }
}