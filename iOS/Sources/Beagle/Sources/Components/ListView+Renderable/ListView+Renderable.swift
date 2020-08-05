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
import BeagleSchema

extension ListView.Direction {
    
    func toUIKit() -> UICollectionView.ScrollDirection {
        switch self {
        case .horizontal:
            return .horizontal
        case .vertical:
            return .vertical
        }
    }
    
}

extension ListView: ServerDrivenComponent {

    public func toView(renderer: BeagleRenderer) -> UIView {
        guard let widget = template as? RawWidget else {
            return UIView()
        }
        let template = widget
        
        let view = ListViewUIComponent(
            model: ListViewUIComponent.Model(
                listViewItems: nil,
                direction: direction?.toUIKit() ?? .vertical,
                template: template,
                onScrollEnd: onScrollEnd,
                scrollThreshold: scrollThreshold
            ),
            renderer: renderer
        )
        
        renderer.controller.execute(actions: [onInit], origin: view)
        renderer.observe(dataSource, andUpdateManyIn: view) {
            if let listItems = $0 {
                view.setListViewItems(listViewItems: listItems)
            }
        }
        view.style.setup(widgetProperties.style)
        return view
    }
    
}
