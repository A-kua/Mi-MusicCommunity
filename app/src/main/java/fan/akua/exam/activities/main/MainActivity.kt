package fan.akua.exam.activities.main

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.drake.brv.item.ItemBind
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.grid
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.BezierRadarHeader
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import fan.akua.exam.AppState
import fan.akua.exam.R
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.GridModel
import fan.akua.exam.activities.main.model.HeaderModel
import fan.akua.exam.activities.main.model.LargeCardModel
import fan.akua.exam.activities.main.model.TitleModel
import fan.akua.exam.databinding.ActivityMainBinding
import fan.akua.exam.fragments.PlayerFragment
import fan.akua.exam.player.PlayerManager
import fan.akua.exam.utils.akuaEdgeToEdge
import fan.akua.exam.utils.logD
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var fragment: PlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        akuaEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 小细节哦~ 防止配置变更重新添加的
        if (savedInstanceState == null) {
            fragment = PlayerFragment()
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragmentHost, fragment, fragment.javaClass.name)
                .hide(fragment)
                .commit()
        } else {
            fragment =
                supportFragmentManager.findFragmentByTag(fragment.javaClass.name) as PlayerFragment
        }

        initialViewState()
        initialEvent()
        initialPanel()

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
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

    override fun onBackPressed() {
        if (binding.slidingLayout.panelState == PanelState.EXPANDED) {
            binding.slidingLayout.panelState = PanelState.COLLAPSED
            return
        }
        super.onBackPressed()
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
        binding.rv.bindingAdapter.setAnimation(AkuaItemAnimation())
        binding.rv.bindingAdapter.animationRepeat = true
    }

    private fun initialEvent() {
        binding.swipe.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.swipe.setOnLoadMoreListener {
            viewModel.loadNextPage()
        }

        lifecycleScope.launch {
            AppState.openFlow.collect {
                binding.slidingLayout.panelState = PanelState.EXPANDED
            }
        }
        lifecycleScope.launch {
            AppState.closeFlow.collect {
                binding.slidingLayout.panelState = PanelState.COLLAPSED
            }
        }
    }

    private fun initialPanel() {
        binding.slidingLayout.addPanelSlideListener(object :
            SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                val alpha = (slideOffset * 10).coerceAtMost(1f)
                binding.panel.root.alpha = 1 - alpha
            }

            override fun onPanelStateChanged(
                panel: View,
                previousState: PanelState,
                newState: PanelState
            ) {
                // 开始展开，则展示fragment
                if (previousState == PanelState.COLLAPSED && newState == PanelState.DRAGGING) {
                    if (fragment.isAdded && fragment.isHidden) {
                        supportFragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .show(fragment)
                            .commit();
                    }
                    // 全部展开，隐藏panel
                } else if (previousState == PanelState.DRAGGING && newState == PanelState.EXPANDED) {
                    binding.panel.root.visibility = View.INVISIBLE
                    // 全部收起，隐藏fragment
                } else if (previousState == PanelState.DRAGGING && newState == PanelState.COLLAPSED) {
                    supportFragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .hide(fragment)
                        .commit()
                    // 开始收起，展示panel
                } else if (previousState == PanelState.EXPANDED && newState == PanelState.DRAGGING) {
                    binding.panel.root.visibility = View.VISIBLE
                }
            }
        })

        lifecycleScope.launch {
            PlayerManager.bitmapFlow.collect { bitmap ->
                bitmap?.let {
                    binding.panel.root.findViewById<ImageView>(R.id.panel_img)
                        .setImageBitmap(bitmap)
                }
            }
        }
        lifecycleScope.launch {
            PlayerManager.pause.collect { isPause ->
                binding.panel.root.findViewById<ImageButton>(R.id.panel_play_pause)
                    .setImageResource(if (isPause) R.drawable.ic_pausing else R.drawable.ic_playing)
            }
        }
        lifecycleScope.launch {
            PlayerManager.currentSong.collect { song ->
                song?.let {
                    binding.panel.root.findViewById<TextView>(R.id.panel_music_name).text =
                        it.songName
                    binding.panel.root.findViewById<TextView>(R.id.panel_music_author).text =
                        it.author
                }
            }
        }
    }
}