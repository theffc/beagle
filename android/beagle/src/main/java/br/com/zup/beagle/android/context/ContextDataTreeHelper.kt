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

package br.com.zup.beagle.android.context

import android.view.View
import br.com.zup.beagle.android.jsonpath.JsonCreateTree
import br.com.zup.beagle.android.utils.setContextData
import java.util.LinkedList

internal class ContextDataTreeHelper {

    fun updateContextDataWithTree(
        contextBinding: ContextBinding,
        jsonCreateTree: JsonCreateTree,
        keys: LinkedList<String>,
        viewContext: View
    ): ContextData {
        var context = contextBinding.context
        val initialTree = jsonCreateTree.createInitialTree(context.value, keys.first)
        if (initialTree != context.value) {
            context = setNewTreeInContextData(viewContext, contextBinding, initialTree)
        }
        return context
    }

    fun setNewTreeInContextData(
        viewContext: View,
        contextBinding: ContextBinding,
        value: Any
    ): ContextData {
        val context = contextBinding.context
        val newContext = context.copy(value = value)
        viewContext.setContextData(contextBinding.copy(context = newContext))
        return newContext
    }
}