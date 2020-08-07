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

public struct ListView: RawWidget, HasContext, AutoInitiableAndDecodable {
    
    public var context: Context?
    public let onInit: [RawAction]
    public let dataSource: Expression<[DynamicObject]>
    public let direction: Direction?
    public let template: RawComponent
    public let onScrollEnd: [RawAction]?
    public let scrollThreshold: Int?
    public var widgetProperties: WidgetProperties
    
// sourcery:inline:auto:ListView.Init
    public init(
        context: Context? = nil,
        onInit: [RawAction],
        dataSource: Expression<[DynamicObject]>,
        direction: Direction? = nil,
        template: RawComponent,
        onScrollEnd: [RawAction]? = nil,
        scrollThreshold: Int? = nil,
        widgetProperties: WidgetProperties = WidgetProperties()
    ) {
        self.context = context
        self.onInit = onInit
        self.dataSource = dataSource
        self.direction = direction
        self.template = template
        self.onScrollEnd = onScrollEnd
        self.scrollThreshold = scrollThreshold
        self.widgetProperties = widgetProperties
    }
// sourcery:end
}

extension ListView {
    public enum Direction: String, Decodable {
           
        case vertical = "VERTICAL"
        case horizontal = "HORIZONTAL"
    }
}
