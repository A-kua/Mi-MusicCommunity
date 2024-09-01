package fan.akua.exam.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import fan.akua.exam.AppState
import fan.akua.exam.databinding.FragmentImageBinding
import fan.akua.exam.databinding.FragmentPlayerBinding
import fan.akua.exam.player.PlayerManager
import kotlinx.coroutines.launch

class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding
    private lateinit var animator: ValueAnimator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(inflater)
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 9 * 1000L
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                binding.img.rotation = animatedValue
            }
            repeatCount = ValueAnimator.INFINITE
        }
        return binding.root
    }

    override fun onDestroyView() {
        animator.cancel()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.img.setOnClickListener {
            lifecycleScope.launch {
                AppState.switchPage(false)
            }
        }
        lifecycleScope.launch {
            PlayerManager.bitmapFlow.collect { bitmap ->
                bitmap?.let {
                    binding.img.setImageBitmap(bitmap)
                    if (animator.isStarted)
                        animator.resume()
                }
            }
        }
        lifecycleScope.launch {
            PlayerManager.pause.collect { isPause ->
                if (isPause) {
                    animator.pause()
                } else {
                    if (animator.isPaused)
                        animator.resume()
                    else
                        animator.start()
                }
            }
        }
    }
}