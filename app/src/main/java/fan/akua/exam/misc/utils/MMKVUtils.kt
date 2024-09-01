package fan.akua.exam.misc.utils

import com.tencent.mmkv.MMKV

object MMKVUtils {

    private val mmkv = MMKV.defaultMMKV()

    fun <T> encode(key: String, value: T) {
        when (value) {
            is Int -> mmkv.encode(key, value)
            is Long -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is String -> mmkv.encode(key, value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> decode(key: String, defValue: T): T {
        return when (defValue) {
            is Int -> mmkv.decodeInt(key, defValue) as T
            is Long -> mmkv.decodeLong(key, defValue) as T
            is Float -> mmkv.decodeFloat(key, defValue) as T
            is Double -> mmkv.decodeDouble(key, defValue) as T
            is Boolean -> mmkv.decodeBool(key, defValue) as T
            is String -> mmkv.decodeString(key, defValue) as T
            else -> defValue
        }
    }
}

