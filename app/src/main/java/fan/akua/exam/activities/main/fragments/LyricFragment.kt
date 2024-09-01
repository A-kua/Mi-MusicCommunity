package fan.akua.exam.activities.main.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fan.akua.exam.AppState
import fan.akua.exam.activities.main.fragments.player.PageMode
import fan.akua.exam.databinding.FragmentLyricBinding
import fan.akua.exam.player.PlayerManager
import fan.akua.exam.misc.utils.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class LyricFragment : Fragment() {

    private lateinit var binding: FragmentLyricBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLyricBinding.inflate(inflater)
        binding.lyricViewX.run {
            setCurrentColor(Color.WHITE)
        }
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            PlayerManager.currentSong.collect { song ->
                song?.let {
                    loadLyric(song.lyricUrl)
                }
            }
        }
        lifecycleScope.launch {
            PlayerManager.progress.collect { progress ->
                binding.lyricViewX.updateTime(progress)
            }
        }

        // 暂时先暴力一下
        binding.lyricViewX.setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP)
                lifecycleScope.launch {
                    AppState.switchPage(PageMode.Image)
                }
            return@setOnTouchListener true
        }

}


private fun loadLyric(url: String) {
    lifecycleScope.launch(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response: Response = OkHttpClient().newCall(request).execute()
            if (response.isSuccessful) {
                val responseData = response.body()?.string()
                responseData?.let {
                    withContext(Dispatchers.Main) {
                        binding.lyricViewX.loadLyric(responseData.trimIndent())
                    }
                }
            }
        } catch (_: Exception) {

        }
    }
}
}