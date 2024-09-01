package fan.akua.exam.activities.main

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout.INVISIBLE
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import com.sothree.slidinguppanel.SlidingUpPanelLayout.VISIBLE
import fan.akua.exam.R
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.GridModel
import fan.akua.exam.activities.main.model.HeaderModel
import fan.akua.exam.activities.main.model.LargeCardModel
import fan.akua.exam.activities.main.model.TitleModel
import fan.akua.exam.misc.anims.AkuaItemAnimation
import fan.akua.exam.databinding.ActivityMainBinding
import fan.akua.exam.activities.main.fragments.player.PlayerFragment
import fan.akua.exam.misc.utils.akuaEdgeToEdge
import kotlinx.coroutines.launch

/**
 * MVI架构，Activity只负责接收用户点击UI发出的Intent，以及ViewModel发生的Event。
 * 收到Intent后，交给ViewModel，由ViewModel更新数据，然后发出Event。
 * 关注点分离，单一数据源，唯一可信源。
 */
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
        resumeChildFragment(savedInstanceState)

        initialViews()
        initialIntent()

        lifecycleScope.launch {
            viewModel.uiState.collect { (recyclerViewState, mainPanelState, slidingViewState) ->
                parseRecyclerViewState(recyclerViewState)
                parseMainPanelState(mainPanelState)
                parseSlidingViewState(slidingViewState)
            }
        }

    }

    /**
     * 尝试恢复Fragment
     */
    private fun resumeChildFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            fragment = PlayerFragment()
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragmentHost, fragment, PlayerFragment::class.qualifiedName)
                .hide(fragment)
                .commit()
        } else {
            fragment =
                supportFragmentManager.findFragmentByTag(PlayerFragment::class.qualifiedName) as PlayerFragment
        }
    }

    /**
     * 处理RecyclerView的状态
     */
    private var previousRecyclerViewState: RecyclerViewState? = null
    private fun parseRecyclerViewState(recyclerViewState: RecyclerViewState) {
        if (previousRecyclerViewState != null)
            if (previousRecyclerViewState == recyclerViewState) return
        when (recyclerViewState.state) {
            RequestState.LOADING -> {

            }

            RequestState.SUCCESS -> {
                stopRefreshAndLoad()
                binding.state.showContent()
            }

            RequestState.ERROR -> {
                stopRefreshAndLoad()
                binding.state.showError()
            }

            RequestState.Initial -> {
                viewModel.loadNextPage()
            }

            RequestState.All -> {
                binding.swipe.setEnableRefresh(true)
                binding.swipe.setEnableLoadMore(false)
            }
        }

        val toRVModels = recyclerViewState.toRVModels(resources = resources)
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
        previousRecyclerViewState = recyclerViewState
    }

    /**
     * 处理SlidingLayout的状态
     */
    private var previousSlidingViewState: SlidingViewState? = null
    private fun parseSlidingViewState(slidingViewState: SlidingViewState) {
        if (previousSlidingViewState != null)
            if (previousSlidingViewState == slidingViewState) return
        if (binding.slidingLayout.panelState != slidingViewState.state) {
            binding.slidingLayout.panelState = slidingViewState.state
        }
        previousSlidingViewState = slidingViewState
    }

    /**
     * 处理Panel的状态
     */
    private var previousMainPanelState: MainPanelState? = null
    private fun parseMainPanelState(panelState: MainPanelState) {
        if (previousMainPanelState != null)
            if (previousMainPanelState == panelState) return

        if (panelState.visible) {
            binding.panel.root.visibility = VISIBLE
        } else {
            binding.panel.root.visibility = INVISIBLE
        }

        panelState.bitmap?.let {
            binding.panel.root.findViewById<ImageView>(R.id.panel_img)
                .setImageBitmap(it)
        }

        binding.panel.root.findViewById<ImageButton>(R.id.panel_play_pause)
            .setImageResource(if (panelState.isPause) R.drawable.ic_pausing else R.drawable.ic_playing)

        panelState.songBean?.let {
            binding.panel.root.findViewById<TextView>(R.id.panel_music_name).text =
                it.songName
            binding.panel.root.findViewById<TextView>(R.id.panel_music_author).text =
                it.author
        }
        previousMainPanelState = panelState
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

    /**
     * 初始化View
     */
    private fun initialViews() {
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
                            .commit()
                    }
                    // 全部展开，隐藏panel
                } else if (previousState == PanelState.DRAGGING && newState == PanelState.EXPANDED) {
                    viewModel.panelHide()
                    viewModel.slidingShow()
                    // 全部收起，隐藏fragment
                } else if (previousState == PanelState.DRAGGING && newState == PanelState.COLLAPSED) {
                    supportFragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .hide(fragment)
                        .commit()
                    viewModel.slidingHide()
                    // 开始收起，展示panel
                } else if (previousState == PanelState.EXPANDED && newState == PanelState.DRAGGING) {
                    viewModel.panelShow()
                }
            }
        })
    }

    /**
     * 初始化主动意图
     */
    private fun initialIntent() {
        binding.swipe.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.swipe.setOnLoadMoreListener {
            viewModel.loadNextPage()
        }
    }

}