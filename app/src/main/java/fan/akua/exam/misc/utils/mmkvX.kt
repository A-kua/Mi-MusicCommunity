package fan.akua.exam.misc.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MMKVDelegate<T>(private val key: String, private val def: T) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = MMKVUtils.decode(key, def)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
        MMKVUtils.encode(key, value)
}

