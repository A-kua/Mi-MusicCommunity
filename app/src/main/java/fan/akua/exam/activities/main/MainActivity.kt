package fan.akua.exam.activities.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.item.ItemBind
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.BezierRadarHeader
import fan.akua.exam.R
import fan.akua.exam.activities.main.model.BannerModel
import fan.akua.exam.activities.main.model.BaseModel
import fan.akua.exam.activities.main.model.GirdModel
import fan.akua.exam.activities.main.model.HeaderModel
import fan.akua.exam.activities.main.model.LargeCard
import fan.akua.exam.data.MusicInfo

import fan.akua.exam.databinding.ActivityMainBinding
import fan.akua.exam.utils.GenericDiffUtil
import fan.akua.exam.utils.areListsEqual
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

        binding.rv.linear().setup {
            addType<HeaderModel>(R.layout.item_type_header)
            addType<BannerModel>(R.layout.item_type_banner)
            addType<LargeCard>(R.layout.item_type_largecard)
            addType<GirdModel>(R.layout.item_type_gird)
        }
        binding.rv.bindingAdapter.setAnimation(AnimationType.SLIDE_BOTTOM)
        binding.rv.bindingAdapter.addHeader(HeaderModel(0x415411, emptyList()), animation = false)

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
                "MainActivity".logD("data update: ${uiState.state}")
                when (uiState.state) {
                    RequestState.SUCCESS -> {
                        val toRVModels = uiState.toRVModels()

                        // kotlin的判空真是奇葩！
                        if (binding.rv.models != null) {
                            val diffUtilCallback = GenericDiffUtil(
                                oldList = binding.rv.models as List<BaseModel>,
                                newList = toRVModels as List<BaseModel>,
                                areItemsTheSame = { oldItem, newItem -> oldItem.modelID == newItem.modelID },
                                areContentsTheSame = { oldItem, newItem ->
                                    oldItem.data.areListsEqual(newItem.data) { a, b ->
                                        a == b
                                    }
                                }
                            )
                            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
                            binding.rv.bindingAdapter.models = toRVModels
                            diffResult.dispatchUpdatesTo(binding.rv.bindingAdapter)
                        }else{
                            binding.rv.bindingAdapter.models = toRVModels
                        }

                        if (binding.swipe.isRefreshing) {
                            binding.swipe.finishRefresh()
                        }
                        if (binding.swipe.isLoading) {
                            binding.swipe.finishLoadMore()
                        }
                    }

                    RequestState.ERROR -> {
                        if (binding.swipe.isRefreshing) {
                            binding.swipe.finishRefresh()
                        }
                        if (binding.swipe.isLoading) {
                            binding.swipe.finishLoadMore()
                        }
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
}