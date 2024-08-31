package fan.akua.exam.activities.main

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.drake.brv.PageRefreshLayout
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.BRV
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.BezierRadarHeader
import fan.akua.exam.BR
import fan.akua.exam.R
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.GirdModel
import fan.akua.exam.activities.main.model.LargeCard

import fan.akua.exam.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rv.linear().setup {
            addType<BannerModel>(R.layout.item_type_banner)
            addType<LargeCard>(R.layout.item_type_largecard)
            addType<GirdModel>(R.layout.item_type_gird)
        }
        binding.rv.bindingAdapter.setAnimation(AnimationType.SLIDE_BOTTOM)

        binding.swipe.setRefreshFooter(ClassicsFooter(this))
        binding.swipe.setRefreshHeader(BezierRadarHeader(this))

        binding.swipe.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.swipe.setOnLoadMoreListener {
            viewModel.loadNextPage()
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                Log.e("simon", "collect ${uiState.state}")
                when (uiState.state) {
                    RequestState.SUCCESS -> {
                        binding.rv.models = uiState.toRVModels()
                        binding.swipe.finishRefresh()
                        binding.swipe.finishLoadMore();
                    }

                    RequestState.ERROR -> {
                        binding.swipe.finishRefresh()
                        binding.swipe.finishLoadMore();
                    }

                    RequestState.LOADING -> {

                    }

                    RequestState.Initial -> {
                        viewModel.loadNextPage()
                    }

                    RequestState.All -> {
                        binding.swipe.setEnableLoadMore(false)
                    }
                }
            }
        }
    }
}