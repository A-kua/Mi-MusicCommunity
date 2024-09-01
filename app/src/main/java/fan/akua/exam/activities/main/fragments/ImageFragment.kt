package fan.akua.exam.activities.main.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import fan.akua.exam.AppState
import fan.akua.exam.activities.main.MainViewModel
import fan.akua.exam.activities.main.fragments.player.PageMode
import fan.akua.exam.databinding.FragmentImageBinding
import fan.akua.exam.player.PlayerManager
import kotlinx.coroutines.launch

class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.img.setOnClickListener {
            lifecycleScope.launch {
                AppState.switchPage(PageMode.Lyric)
            }
        }
        lifecycleScope.launch {
            PlayerManager.bitmapFlow.collect { bitmap ->
                bitmap?.let {
                    Glide.with(this@ImageFragment)
                        .load(bitmap)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .into(binding.img)
                    binding.img.startRotation()
                }
            }
        }
        lifecycleScope.launch {
            PlayerManager.pause.collect { isPause ->
                if (isPause) binding.img.pauseRotation()
                else binding.img.resumeRotation()
            }
        }
    }
}