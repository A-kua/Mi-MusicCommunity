package fan.akua.exam.data

data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val data: T
)