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

import UIKit
import Beagle
import BeagleSchema

struct ListViewScreen: DeeplinkScreen {
    
    init(path: String, data: [String: String]?) {
    }
    
    func screenController() -> UIViewController {
        return Beagle.screen(.declarative(screen))
    }
    
    var screen: Screen {
        return Screen(
            navigationBar: NavigationBar(title: "ListView"),
            child: Container(
                children: [
                    listView
                ],
                widgetProperties: WidgetProperties(
                    style: Style(
                        flex: Flex().grow(1)
                    )
                )
            )
        )
    }
    
    var image = Image(
        .value(.local("imageBeagle")),
        widgetProperties: WidgetProperties(
            style: Style(
                size: Size().width(100).height(100)
            )
        )
    )
    
    var listView = ListView(
        context: Context(
            id: "initialContext",
            value: ""
        ),
        onInit: [SendRequest(
            url: "https://api.themoviedb.org/3/genre/movie/list?api_key=02a08061d7eead16928726e26cb3203c&language=en-US",
            method: .get,
            onSuccess: [
                SetContext(
                    contextId: "initialContext",
                    value: "@{onSuccess.data.genres}"
                )
            ]
        )],
        dataSource: Expression("@{initialContext}"),
        direction: .horizontal,
        template: Container(
            children: [
                Text(
                    "@{item.name}",
                    widgetProperties: WidgetProperties(
                        style: Style(
                            backgroundColor: "#ffa36c"
                        )
                    )
                )
            ],
            widgetProperties: WidgetProperties(
                style: Style(
                    backgroundColor: "#0f4c75"
                )
            )
        ),
        onScrollEnd: [SendRequest(url: "")],
        scrollThreshold: 80,
        widgetProperties: WidgetProperties(
            style: Style(
                backgroundColor: "#cedebd",
                flex: Flex().grow(1)
            )
        )
    )
}
