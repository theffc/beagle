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

final class ListViewUIComponent: UIView {
    
    // MARK: - Properties
    
    private var renderer: BeagleRenderer
    private var model: Model
    var validationSetOnScrollEnd = true
    var number = 0
    
    // MARK: - UIComponents
    
    lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = model.direction
        layout.minimumInteritemSpacing = 0
        layout.minimumLineSpacing = 0
        layout.estimatedItemSize = UICollectionViewFlowLayout.automaticSize
        let collection = UICollectionView(
            frame: CGRect(),
            collectionViewLayout: layout
        )
        collection.backgroundColor = .clear
        collection.register(ListViewCell.self, forCellWithReuseIdentifier: "ListViewCell")
        collection.translatesAutoresizingMaskIntoConstraints = false
        collection.showsHorizontalScrollIndicator = false
        collection.showsVerticalScrollIndicator = false
        collection.dataSource = self
        collection.delegate = self
        return collection
    }()
    
    // MARK: - Initialization
    
    init(model: Model, renderer: BeagleRenderer) {
        self.model = model
        self.renderer = renderer
        super.init(frame: .zero)
        setupViews()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupViews() {
        addSubview(collectionView)
        collectionView.anchorTo(superview: self)
    }
    
    func setListViewItems(listViewItems: [DynamicObject]) {
        self.model.listViewItems = listViewItems
        self.collectionView.reloadData()
    }
    
    private func setupCell(_ item: DynamicObject) -> UIView {
        let view = renderer.render(model.template)
        let context = Context(id: "item", value: item)
        view.setContext(context)
        let controller = renderer.controller as? BeagleScreenViewController
        controller?.configBindings()
        let containerView = UIView()
        let style = Style().positionType(.absolute)
        view.style.setup(style)
        containerView.style.setup(style)
        containerView.frame.size = collectionView.bounds.size
        containerView.addSubview(view)
        containerView.style.applyLayout()
        
        switch model.direction {
        case .horizontal:
            let viewHorizontal = UIView()
            let size = CGSize(
                width: view.bounds.size.width,
                height: collectionView.bounds.size.height
            )
            viewHorizontal.bounds.size = size
            viewHorizontal.addSubview(view)
            return viewHorizontal

        case .vertical:
            let viewVertical = UIView()
            let size = CGSize(
                width: collectionView.bounds.size.width,
                height: view.bounds.size.height
            )
            viewVertical.bounds.size = size
            viewVertical.addSubview(view)
            return viewVertical
        default: ()
        }
        
       return UIView()
        
    }
    
    private func updateCell(indexPath: IndexPath) {
        collectionView.reloadItems(at: [indexPath])
    }
    
}

// MARK: - Model
extension ListViewUIComponent {
    struct Model {
        var listViewItems: [DynamicObject]?
        var direction: UICollectionView.ScrollDirection
        var template: RawWidget
        var onScrollEnd: RawAction?
        var scrollThreshold: Int?
    }
}

// MARK: - UICollection View Delegate and DataSource Extension

extension ListViewUIComponent: UICollectionViewDataSource, UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return model.listViewItems?.count ?? 0
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        guard
            let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "ListViewCell", for: indexPath) as? ListViewCell,
            let item = model.listViewItems?[indexPath.row] else {
                return UICollectionViewCell()
        }
        
        if cell.templateView == nil {
            cell.setupCell(templateView: self.setupCell(item))
        }
        
        cell.controller = renderer.controller as? BeagleScreenViewController
        let context = Context(id: "item", value: item)
        cell.templateView?.subviews[0].setContext(context)
        cell.controller?.configBindings()
        
//        if let templateView = cell.templateView {
//            let expression: Expression<DynamicObject> = "@{item}"
//            renderer.observe(expression, andUpdateManyIn: templateView) { [weak self] _ in
//                self?.updateCell(indexPath: indexPath)
//                self?.number = indexPath.row
//            }
//        }
        
        if validationSetOnScrollEnd {
            setOnScrollEnd(index: indexPath.row)
        }
        
        return cell
    }
    
    func validatePercentage() -> Float {
        let percent = Float(model.scrollThreshold ?? 100)
        if percent > 100 && percent < 0 {
            return 1
        } else {
            return percent / 100
        }
    }
    
    func setOnScrollEnd(index: Int) {
        guard let action = model.onScrollEnd else { return }
        let numberOfItems = Float(model.listViewItems?.count ?? 0)
        let scrollThreshold = validatePercentage()
        let valuePercent = numberOfItems * scrollThreshold
        
        if Float(index) >= valuePercent {
            renderer.controller.execute(actions: [action], origin: self)
            validationSetOnScrollEnd = false
        }
    }
    
    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        insetForSectionAt section: Int
    ) -> UIEdgeInsets {
        return .zero
    }
    
    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        minimumLineSpacingForSectionAt section: Int
    ) -> CGFloat {
        return 8
    }
    
    func collectionView(
        _ collectionView: UICollectionView,
        layout collectionViewLayout: UICollectionViewLayout,
        minimumInteritemSpacingForSectionAt section: Int
    ) -> CGFloat {
        return 8
    }
    
}
