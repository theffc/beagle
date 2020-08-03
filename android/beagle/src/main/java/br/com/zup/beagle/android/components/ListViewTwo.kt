/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.components

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.zup.beagle.android.action.Action
import br.com.zup.beagle.android.context.Bind
import br.com.zup.beagle.android.context.ContextComponent
import br.com.zup.beagle.android.context.ContextData
import br.com.zup.beagle.android.utils.generateViewModelInstance
import br.com.zup.beagle.android.utils.getContextBinding
import br.com.zup.beagle.android.utils.observeBindChanges
import br.com.zup.beagle.android.utils.setContextData
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.android.view.viewmodel.ScreenContextViewModel
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.annotation.RegisterWidget
import br.com.zup.beagle.core.ServerDrivenComponent
import br.com.zup.beagle.widget.core.ListDirection
import java.util.concurrent.atomic.AtomicBoolean


@RegisterWidget
internal class ListViewTwo(
    override val context: ContextData? = null,
    val onInit: Action? = null,
    val dataSource: Bind<List<Any>>? = null,
    val direction: ListDirection,
    val template: ServerDrivenComponent,
    val onScrollEnd: Action? = null,
    val scrollThreshold: Int? = null
) : WidgetView(), ContextComponent {

    @Transient
    private val viewFactory: ViewFactory = ViewFactory()

    @Transient
    private lateinit var contextAdapter: ListViewContextAdapter2

    @Transient
    private val needToAppendList: AtomicBoolean = AtomicBoolean(false)


    override fun buildView(rootView: RootView): View {
        val recyclerView = viewFactory.makeRecyclerView(rootView.getContext())
        onInit?.execute(rootView, recyclerView)
        val orientation = toRecyclerViewOrientation()
        contextAdapter = ListViewContextAdapter2(template, viewFactory, orientation, rootView)
        recyclerView.apply {
            adapter = contextAdapter
            layoutManager = CustomLinearLayoutManager(context, orientation, false)
        }
        dataSource?.let {
            observeBindChanges(rootView, recyclerView, it) { value ->
                value?.let {
                    contextAdapter.setList(value)
                }
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val layoutManager = LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                layoutManager?.let {
                    val totalItemCount = it.itemCount
                    val lastVisible = it.findLastVisibleItemPosition()
                    var isScrollEnded = false
                    if (scrollThreshold == null)
                        isScrollEnded = !recyclerView.canScrollVertically(toRecyclerViewOrientation())
                    scrollThreshold?.let { int ->
                        isScrollEnded = (lastVisible.toFloat() / totalItemCount.toFloat()) * 100 >= int.toFloat()
                    }
                    if (isScrollEnded) {
                        needToAppendList.set(true)
                        onScrollEnd?.execute(rootView, recyclerView)
                    }
                }
            }
        })
        return recyclerView
    }

    private fun toRecyclerViewOrientation() = if (direction == ListDirection.VERTICAL) {
        RecyclerView.VERTICAL
    } else {
        RecyclerView.HORIZONTAL
    }


}

internal class ListViewContextAdapter2(
    private val template: ServerDrivenComponent,
    private val viewFactory: ViewFactory,
    private val orientation: Int,
    private val rootView: RootView
) : RecyclerView.Adapter<ContextViewHolderTwo>() {

    private var listItems: ArrayList<Any>

    init {
        listItems = ArrayList()
    }

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ContextViewHolderTwo {
        val view = viewFactory.makeBeagleFlexView(rootView.getContext()).also {
            val width = if (orientation == RecyclerView.VERTICAL)
                ViewGroup.LayoutParams.MATCH_PARENT else
                ViewGroup.LayoutParams.WRAP_CONTENT
            val layoutParams = ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.layoutParams = layoutParams
        }
        val templateClone = template
        view.addServerDrivenComponent(templateClone, rootView)
        view.setContextData(ContextData("item", listItems[position]))
        rootView.generateViewModelInstance<ScreenContextViewModel>().test()
        return ContextViewHolderTwo(view)
    }

    override fun onBindViewHolder(holder: ContextViewHolderTwo, position: Int) {
        val item = listItems[position]
        val view = holder.itemView
        view.setContextData(ContextData(id = "item", value = item))
        view.getContextBinding()?.let { contextBinding ->
            rootView.generateViewModelInstance<ScreenContextViewModel>().notifyBindingChanges(contextBinding)
        }
    }

    fun setList(list: List<Any>) {
        this.listItems = ArrayList(list)
        notifyDataSetChanged()
    }

    fun addList(list: List<Any>) {
        val initialSize = listItems.size
        listItems.addAll(list)
        notifyItemRangeInserted(initialSize, list.size)
    }

    override fun getItemCount(): Int = listItems.size
}

internal class ContextViewHolderTwo(itemView: View) : RecyclerView.ViewHolder(itemView)

class CustomLinearLayoutManager : LinearLayoutManager {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {}

    private val mMeasuredDimension = IntArray(2)
    override fun onMeasure(recycler: RecyclerView.Recycler, state: RecyclerView.State, widthSpec: Int, heightSpec: Int) {
        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val heightSize = View.MeasureSpec.getSize(heightSpec)
        var width = 0
        var height = 0
        for (i in 0 until itemCount) {
            measureScrapChild(recycler, i, View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                mMeasuredDimension)
            if (orientation == HORIZONTAL) {
                width = width + mMeasuredDimension[0]
                if (i == 0) {
                    height = mMeasuredDimension[1]
                }
            } else {
                height = height + mMeasuredDimension[1]
                if (i == 0) {
                    width = mMeasuredDimension[0]
                }
            }
        }
        when (widthMode) {
            View.MeasureSpec.EXACTLY -> width = widthSize
            View.MeasureSpec.AT_MOST, View.MeasureSpec.UNSPECIFIED -> {
            }
        }
        when (heightMode) {
            View.MeasureSpec.EXACTLY -> height = heightSize
            View.MeasureSpec.AT_MOST, View.MeasureSpec.UNSPECIFIED -> {
            }
        }
        setMeasuredDimension(width, height)
    }

    private fun measureScrapChild(recycler: RecyclerView.Recycler, position: Int, widthSpec: Int,
                                  heightSpec: Int, measuredDimension: IntArray) {
        try {
            val view = recycler.getViewForPosition(position)
            if (view != null) {
                val p = view.layoutParams as RecyclerView.LayoutParams
                val childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                    paddingLeft + paddingRight, p.width)
                val childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                    paddingTop + paddingBottom, p.height)
                view.measure(childWidthSpec, childHeightSpec)
                measuredDimension[0] = view.measuredWidth + p.leftMargin + p.rightMargin
                measuredDimension[1] = view.measuredHeight + p.bottomMargin + p.topMargin
                recycler.recycleView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = CustomLinearLayoutManager::class.java.simpleName
    }
}