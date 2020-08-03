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
import br.com.zup.beagle.android.logger.BeagleContextLogs
import br.com.zup.beagle.android.utils.Observer
import br.com.zup.beagle.android.utils.findParentContextWithId
import br.com.zup.beagle.android.utils.getAllParentContexts
import br.com.zup.beagle.android.utils.getContextBinding
import br.com.zup.beagle.android.utils.getContextId
import br.com.zup.beagle.android.utils.getExpressions
import br.com.zup.beagle.android.utils.setContextBinding
import br.com.zup.beagle.android.utils.setContextData

class ContextDataManager(
    private val jsonCreateTree: JsonCreateTree = JsonCreateTree(),
    private val contextDataTreeHelper: ContextDataTreeHelper = ContextDataTreeHelper(),
    private val contextPathResolver: ContextPathResolver = ContextPathResolver(),
    private val contextDataEvaluation: ContextDataEvaluation = ContextDataEvaluation()
) {

    private val contexts = mutableMapOf<Int, ContextBinding>()
    private val viewBinding = mutableMapOf<View, MutableSet<Binding<*>>>()

    fun clearContexts() {
        contexts.clear()
        viewBinding.clear()
    }

    fun addContext(view: View, context: ContextData) {
        val existingContext = contexts[view.id]

        if (existingContext != null) {
            view.setContextBinding(existingContext)
            existingContext.bindings.clear()
        } else {
            view.setContextData(context)
            view.getContextBinding()?.let {
                contexts[view.id] = it
            }
        }
    }

    fun <T> addBinding(view: View, bind: Bind.Expression<T>, observer: Observer<T?>) {
        val bindings: MutableSet<Binding<*>> = viewBinding[view] ?: mutableSetOf()
        bindings.add(Binding(
            observer = observer,
            bind = bind
        ))
        viewBinding[view] = bindings
    }

    fun linkBindingToContext() {
        viewBinding.forEach { entry ->
            val parentContexts = entry.key.getAllParentContexts()
            entry.value.forEach { binding ->
                binding.bind.value.getExpressions().forEach { expression ->
                    val contextId = expression.getContextId()
                    parentContexts[contextId]?.bindings?.add(binding)
                }
            }
        }

        viewBinding.clear()
    }

    fun getContextsFromBind(originView: View, binding: Bind.Expression<*>): List<ContextData> {
        val parentContexts = originView.getAllParentContexts()
        val contextIds = binding.value.getExpressions().map { it.getContextId() }
        return parentContexts.filterKeys { contextIds.contains(it) }.map { it.value.context }
    }

    fun updateContext(view: View, setContextInternal: SetContextInternal): Boolean {
        return view.findParentContextWithId(setContextInternal.contextId)?.let { parentView ->
            val currentContextBinding = parentView.getContextBinding()
            currentContextBinding?.let {
                setContextValue(parentView, currentContextBinding, setContextInternal)
            }
        } ?: false
    }

    private fun setContextValue(
        contextView: View,
        contextBinding: ContextBinding,
        setContextInternal: SetContextInternal
    ): Boolean {
        val path = setContextInternal.path ?: contextBinding.context.id
        val setValue = setValue(contextView, contextBinding, path, setContextInternal.value)
        if (setValue) {
            contextView.getContextBinding()?.let { newContextBinding ->
                contexts[contextView.id] = newContextBinding
                notifyBindingChanges(newContextBinding)
            }
        }
        return setValue
    }

    fun evaluateContexts() {
        contexts.forEach { entry ->
            notifyBindingChanges(entry.value)
        }
    }

    fun notifyBindingChanges(contextBinding: ContextBinding) {
        val contextData = contextBinding.context
        val bindings = contextBinding.bindings

        bindings.forEach { binding ->
            val value = contextDataEvaluation.evaluateBindExpression(
                contextData,
                contextBinding.cache,
                binding.bind,
                binding.evaluatedExpressions
            )
            binding.notifyChanges(value)
        }
    }

    private fun setValue(viewContext: View, contextBinding: ContextBinding, path: String, value: Any): Boolean {
        var context = contextBinding.context
        return if (path == context.id) {
            contextDataTreeHelper.setNewTreeInContextData(viewContext, contextBinding, value)
            true
        } else {
            try {
                val keys = contextPathResolver.getKeysFromPath(context.id, path)
                if (keys.isEmpty()) {
                    return false
                }
                context = contextDataTreeHelper.updateContextDataWithTree(
                    viewContext,
                    contextBinding,
                    jsonCreateTree,
                    keys
                )
                jsonCreateTree.walkingTreeAndFindKey(context.value, keys, value)
                true
            } catch (ex: Exception) {
                BeagleContextLogs.errorWhileTryingToChangeContext(ex)
                false
            }
        }
    }
}