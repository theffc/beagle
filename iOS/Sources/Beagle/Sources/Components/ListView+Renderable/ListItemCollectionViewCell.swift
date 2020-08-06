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
    var isHeightCalculated: Bool = false
    
    func setupCell(templateView: UIView) {
        self.templateView = templateView
        contentView.bounds.size = templateView.bounds.size
        contentView.addSubview(templateView)
        templateView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
//        templateView.anchor(top: contentView.topAnchor, left: contentView.leftAnchor)
//        templateView.bottomAnchor.constraint(lessThanOrEqualTo: contentView.bottomAnchor).isActive = true
//        templateView.rightAnchor.constraint(lessThanOrEqualTo: contentView.rightAnchor).isActive = true
    }
    
    override func preferredLayoutAttributesFitting(_ layoutAttributes: UICollectionViewLayoutAttributes) -> UICollectionViewLayoutAttributes {
        if !isHeightCalculated, let template = templateView {
            layoutAttributes.size = template.bounds.size
            isHeightCalculated = true
        }
        return layoutAttributes
    }
}
