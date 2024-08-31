package fan.akua.exam

import org.junit.Test

class KtTest {
    data class Item(val type: Int, val data: String)

    fun insertItemOnTypeChange(items: List<Item>): List<Item> {
        val result = mutableListOf<Item>()

        for (item in items) {
            // 如果 result 是空的，直接添加第一个 item
            if (result.isEmpty() || result.last().type != item.type) {
                // 在类型变化时插入新的 Item，data 为当前 item 的 data
                result.add(Item(type = 555, data = item.data))
            }
            // 添加当前 item
            result.add(item)
        }

        return result
    }

    @Test
    fun main() {
        val items = listOf(
            Item(type = 1, data = "data1"),
            Item(type = 1, data = "data2"),
            Item(type = 2, data = "data3"),
            Item(type = 3, data = "data4")
        )

        val updatedItems = insertItemOnTypeChange(items)
        println(updatedItems)
    }
}