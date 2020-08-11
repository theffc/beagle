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
import androidx.core.view.get
import androidx.lifecycle.Observer
import br.com.zup.beagle.core.ServerDrivenComponent
import br.com.zup.beagle.android.extensions.once
import br.com.zup.beagle.android.testutil.RandomData
import br.com.zup.beagle.android.view.BeagleViewState
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.android.view.custom.BeagleFlexView
import br.com.zup.beagle.android.view.viewmodel.BeagleViewModel
import br.com.zup.beagle.android.view.viewmodel.ViewState
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertTrue

private val URL = RandomData.httpUrl()

class LazyComponentTest : BaseComponentTest() {

    private val initialStateView: View = mockk()
    private val initialState: ServerDrivenComponent = mockk()
    private val beagleView: BeagleFlexView = mockk(relaxed = true)
    private val viewModel: BeagleViewModel = mockk()

    private lateinit var lazyComponent: LazyComponent

    override fun setUp() {
        super.setUp()

        prepareViewModelMock(viewModel)
        every { anyConstructed<ViewFactory>().makeBeagleFlexView(any()) } returns beagleView

        lazyComponent = LazyComponent(URL, initialState)
    }

    @Test
    fun build_should_call_make_a_BeagleView() {
        val actual = lazyComponent.buildView(rootView)

        assertTrue(actual is BeagleFlexView)
    }

    @Test
    fun build_should_add_initialState_and_trigger_updateView() {
        // Given
        val component = mockk<ServerDrivenComponent>()
        val observer = slot<Observer<ViewState>>()
        every { viewModel.fetchComponent(any()).observe(any(), capture(observer)) }

        // When
        lazyComponent.buildView(rootView)
        observer.captured.onChanged(ViewState.DoRender(screenId =  null, component = component))

        // Then
        verify(exactly = once()) { beagleView.addServerDrivenComponent(initialState, rootView) }
        verify(exactly = once()) { viewModel.fetchComponent(any()) }
    }
}