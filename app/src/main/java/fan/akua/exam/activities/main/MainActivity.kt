package fan.akua.exam.activities.main

import android.annotation.SuppressLint
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
import com.drake.brv.listener.ItemDifferCallback
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
import fan.akua.exam.data.MusicInfo
import fan.akua.exam.misc.GridItemDecoration
import fan.akua.exam.misc.utils.akuaEdgeToEdge
import fan.akua.exam.misc.utils.areListsEqual
import fan.akua.exam.misc.utils.dp
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
        binding.rv.bindingAdapter.setDifferModels(toRVModels, false)
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
        if (binding.slidingLayout == null) {
            when (slidingViewState.state) {
                PanelState.EXPANDED -> {
                    if (fragment.isAdded && fragment.isHidden) {
                        supportFragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .show(fragment)
                            .commit()
                    }
                    viewModel.panelHide()
                }

                PanelState.COLLAPSED -> {
                    supportFragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .hide(fragment)
                        .commit()
                    viewModel.panelShow()
                }

                PanelState.ANCHORED, PanelState.HIDDEN, PanelState.DRAGGING -> {}
            }

        } else {
            binding.slidingLayout?.let {
                if (it.panelState != slidingViewState.state) {
                    it.panelState = slidingViewState.state
                }
            }
        }

        previousSlidingViewState = slidingViewState
    }

    /**
     * 处理Panel的状态
     */
    private var previousMainPanelState: MainPanelState? = null

    @SuppressLint("SetTextI18n")
    private fun parseMainPanelState(panelState: MainPanelState) {
        if (binding.panel == null) return
        if (previousMainPanelState != null)
            if (previousMainPanelState == panelState) return

        binding.panel?.let { panel ->
            if (panelState.visible) {
                panel.root.visibility = VISIBLE
            } else {
                panel.root.visibility = INVISIBLE
            }

            panelState.bitmap?.let {
                panel.root.findViewById<ImageView>(R.id.panel_img)
                    .setImageBitmap(it)
            }

            panel.root.findViewById<ImageButton>(R.id.panel_play_pause)
                .setImageResource(if (panelState.isPause) R.drawable.ic_pausing else R.drawable.ic_playing)

            panelState.songBean?.let {
                panel.root.findViewById<TextView>(R.id.panel_music_name).text =
                    it.songName
                panel.root.findViewById<TextView>(R.id.panel_music_author).text =
                    "-${it.author}"
            }
        }
        previousMainPanelState = panelState
    }

    override fun onBackPressed() {
        binding.slidingLayout?.let {
            if (it.panelState == PanelState.EXPANDED) {
                it.panelState = PanelState.COLLAPSED
                return
            }
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
        binding.rv.addItemDecoration(GridItemDecoration(8.dp(this).toInt()))
        // 下拉刷新，上拉加载样式
        binding.swipe.setRefreshFooter(ClassicsFooter(this))
        binding.swipe.setRefreshHeader(BezierRadarHeader(this))
        // 添加头部搜索
        binding.rv.bindingAdapter.addHeader(HeaderModel(), animation = true)
        binding.rv.bindingAdapter.setAnimation(AkuaItemAnimation())
        binding.rv.bindingAdapter.animationRepeat = true
        binding.rv.bindingAdapter.itemDifferCallback = object : ItemDifferCallback {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                if (oldItem is BannerModel && newItem is BannerModel) {
                    return true
                } else if (oldItem is GridModel && newItem is GridModel) {
                    return oldItem.musicInfo.id == newItem.musicInfo.id
                } else if (oldItem is LargeCardModel && newItem is LargeCardModel) {
                    return oldItem.data.moduleConfigId == newItem.data.moduleConfigId
                }
                return false
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                if (oldItem is BannerModel && newItem is BannerModel) {
                    return oldItem.data.areListsEqual(newItem.data) { aItem, bItem ->
                        !(aItem.id == bItem.id && aItem.musicName == bItem.musicName && aItem.author == bItem.author && aItem.coverUrl == bItem.coverUrl)
                    }
                } else if (oldItem is GridModel && newItem is GridModel) {
                    val aMusicInfo = oldItem.musicInfo
                    val bMusicInfo = newItem.musicInfo
                    return aMusicInfo.id == bMusicInfo.id && aMusicInfo.musicName == bMusicInfo.musicName && aMusicInfo.author == bMusicInfo.author && aMusicInfo.coverUrl == bMusicInfo.coverUrl
                } else if (oldItem is LargeCardModel && newItem is LargeCardModel) {
                    return oldItem.data.musicInfoList.areListsEqual(newItem.data.musicInfoList) { aItem, bItem ->
                        !(aItem.id == bItem.id && aItem.musicName == bItem.musicName && aItem.author == bItem.author && aItem.coverUrl == bItem.coverUrl)
                    }
                }
                return false
            }
        }
        if (binding.slidingLayout == null) return
        binding.slidingLayout?.let { sliding ->
            sliding.addPanelSlideListener(object :
                SlidingUpPanelLayout.PanelSlideListener {
                override fun onPanelSlide(panel: View, slideOffset: Float) {
                    val alpha = (slideOffset * 10).coerceAtMost(1f)
                    binding.panel?.let {
                        it.root.alpha = 1 - alpha
                    }
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