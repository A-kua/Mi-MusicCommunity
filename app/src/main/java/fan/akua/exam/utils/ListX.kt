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