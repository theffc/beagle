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

import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.lifecycle.Observer
import br.com.zup.beagle.android.components.utils.viewExtensionsViewFactory
import br.com.zup.beagle.android.utils.generateViewModelInstance
import br.com.zup.beagle.android.utils.toView
import br.com.zup.beagle.android.view.BeagleViewState
import br.com.zup.beagle.android.view.ScreenRequest
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.android.view.custom.BeagleFlexView
import br.com.zup.beagle.android.view.mapper.toBeagleViewState
import br.com.zup.beagle.android.view.viewmodel.BeagleViewModel
import br.com.zup.beagle.android.view.viewmodel.ViewState
import br.com.zup.beagle.android.widget.RootView
import br.com.zup.beagle.android.widget.WidgetView
import br.com.zup.beagle.annotation.RegisterWidget
import br.com.zup.beagle.core.ServerDrivenComponent

@RegisterWidget
data class LazyComponent(
    val path: String,
    val initialState: ServerDrivenComponent
) : WidgetView() {

    @Transient
    private val viewFactory: ViewFactory = ViewFactory()

    override fun buildView(rootView: RootView): View {
        val beagleViewModel = rootView.generateViewModelInstance<BeagleViewModel>()
        return viewFactory.makeBeagleFlexView(rootView.getContext()).apply {
            addServerDrivenComponent(initialState, rootView)
            beagleViewModel.fetchComponent(ScreenRequest(path)).observe(rootView.getLifecycleOwner(), Observer { state ->
                if (state is ViewState.DoRender) {
                    removeAllViewsInLayout()
                    addServerDrivenComponent(state.component, rootView)
                }
            })
        }
    }
}
