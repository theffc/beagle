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

/// Defines a container that holds a listview item
final class ListViewCell: UICollectionViewCell {
    
    weak var controller: BeagleScreenViewController?
    var templateView: UIView?
    var sizeCollection: CGSize?
    var direction: UICollectionView.ScrollDirection?
    var isSizeCalculated: Bool = false
    
    func setupCell(templateView: UIView, sizeCollection: CGSize, direction: UICollectionView.ScrollDirection) {
        self.templateView = templateView
        self.direction = direction
        self.sizeCollection = sizeCollection
        contentView.addSubview(templateView)
    }
    
    override func preferredLayoutAttributesFitting(_ layoutAttributes: UICollectionViewLayoutAttributes) -> UICollectionViewLayoutAttributes {
        if !isSizeCalculated, let template = templateView, let sizeCollection = sizeCollection {
            var size = sizeCollection
            switch direction {
            case .horizontal:
                size.width = template.frame.size.width
            case .vertical:
                size.height = template.frame.size.height
            default: ()
            }
            layoutAttributes.size = size
        }
        return layoutAttributes
    }
}
