package fan.akua.exam.utils

fun <T> List<T>.areListsEqual(list2: List<T>, areContentsTheSame: (T, T) -> Boolean): Boolean {
    if (size != list2.size) {
        return false
    }

    for (i in indices) {
        if (!areContentsTheSame(this[i], list2[i])) {
            return false
        }
    }

    return true
}

/**
 * 在Item的类型发生改变时插入一个新Item
 */
fun <T> List<T>.insertItemOnTypeChange(
    typeChecker: (T, T) -> Boolean,
    dataMaker: (T) -> T
): List<T> {
    val result = mutableListOf<T>()

    for (item in this) {
        if (result.isEmpty() || !typeChecker.invoke(result.last(), item)) {
            result.add(dataMaker.invoke(item))
        }
        result.add(item)
    }

    return result
}