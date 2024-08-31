package fan.akua.exam.activities.main

import android.graphics.RenderEffect
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.drake.brv.item.ItemBind
import com.drake.brv.listener.OnHoverAttachListener
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.BezierRadarHeader
import fan.akua.exam.R
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.GridModel
import fan.akua.exam.activities.main.model.HeaderModel
import fan.akua.exam.activities.main.model.LargeCardModel
import fan.akua.exam.activities.main.model.TitleModel

import fan.akua.exam.databinding.ActivityMainBinding
import fan.akua.exam.utils.logD
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialViewState()
        initialEvent()

        binding.rv.bindingAdapter.setAnimation(AkuaItemAnimation())
        binding.rv.bindingAdapter.animationRepeat = true

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                "MainActivity".logD("data update: ${uiState.state}")
                when (uiState.state) {
                    RequestState.SUCCESS -> {
                        val toRVModels = uiState.toRVModels(resources = resources)
                        if (binding.rv.models != null) {
                            val merge = MainDataMerge.merge(
                                binding.rv.bindingAdapter.models as List<ItemBind>,
                                toRVModels
                            )
                            binding.rv.bindingAdapter.models = toRVModels

                            merge.dispatchUpdatesTo(binding.rv.bindingAdapter)
                        } else {
                            binding.rv.bindingAdapter.models = toRVModels
                        }

                        stopRefreshAndLoad()
                        binding.state.showContent()
                    }

                    RequestState.ERROR -> {
                        stopRefreshAndLoad()
                        binding.state.showError()
                    }

                    RequestState.LOADING -> {

                    }

                    RequestState.Initial -> {
                        viewModel.loadNextPage()
                    }

                    RequestState.All -> {
                        binding.swipe.setEnableRefresh(true)
                        binding.swipe.setEnableLoadMore(false)
                    }
                }
            }
        }
    }

    private fun stopRefreshAndLoad() {
        if (binding.swipe.isRefreshing) {
            binding.swipe.finishRefresh()
        }
        if (binding.swipe.isLoading) {
            binding.swipe.finishLoadMore()
        }
    }

    private fun initialViewState() {
        binding.rv.grid(spanCount = 2).setup {
            addType<HeaderModel>(R.layout.item_type_header)
            addType<BannerModel>(R.layout.item_type_banner)
            addType<LargeCardModel>(R.layout.item_type_largecard)
            addType<GridModel>(R.layout.item_type_grid)
            addType<TitleModel>(R.layout.item_type_title)
        }

        // 为不同类型的Item设置占用的宽度
        val gridLayoutManager = binding.rv.layoutManager as GridLayoutManager
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (val model = binding.rv.bindingAdapter.getModel<ItemBind>(position)) {
                    is GridModel -> model.spanCount
                    else -> 2
                }
            }
        }
        // 下拉刷新，上拉加载样式
        binding.swipe.setRefreshFooter(ClassicsFooter(this))
        binding.swipe.setRefreshHeader(BezierRadarHeader(this))
        // 添加头部搜索
        binding.rv.bindingAdapter.addHeader(HeaderModel(), animation = true)
    }

    private fun initialEvent() {
        binding.swipe.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.swipe.setOnLoadMoreListener {
            viewModel.loadNextPage()
        }
    }
}