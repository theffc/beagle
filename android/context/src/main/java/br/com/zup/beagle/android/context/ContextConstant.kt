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

import com.squareup.moshi.Moshi

object ContextConstant {
    val EXPRESSION_REGEX = "@\\{([^)]+)\\}".toRegex()

    lateinit var moshi: Moshi
    var memoryMaximumCapacity: Int = 0
}

internal object HandleEventDeprecatedConstants {
    const val HANDLE_EVENT_DEPRECATED_MESSAGE: String =
        "Use handleEvent without eventName and eventValue or with ContextData for create a implicit context"
    const val HANDLE_EVENT_POINTER: String = "handleEvent(rootView, origin, action)"
    const val HANDLE_EVENT_ACTIONS_POINTER: String = "handleEvent(rootView, origin, actions)"
}