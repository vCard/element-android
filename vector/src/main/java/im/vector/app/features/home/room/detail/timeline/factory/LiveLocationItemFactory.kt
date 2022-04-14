/*
 * Copyright (c) 2022 New Vector Ltd
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

package im.vector.app.features.home.room.detail.timeline.factory

import im.vector.app.core.epoxy.VectorEpoxyModel
import im.vector.app.core.resources.UserPreferencesProvider
import im.vector.app.core.utils.DimensionConverter
import im.vector.app.features.home.room.detail.timeline.helper.AvatarSizeProvider
import im.vector.app.features.home.room.detail.timeline.helper.LiveLocationEventsGroup
import im.vector.app.features.home.room.detail.timeline.helper.MessageInformationDataFactory
import im.vector.app.features.home.room.detail.timeline.helper.MessageItemAttributesFactory
import im.vector.app.features.home.room.detail.timeline.helper.TimelineMediaSizeProvider
import im.vector.app.features.home.room.detail.timeline.item.AbsMessageItem
import im.vector.app.features.home.room.detail.timeline.item.MessageLiveLocationStartItem
import im.vector.app.features.home.room.detail.timeline.item.MessageLiveLocationStartItem_
import javax.inject.Inject

class LiveLocationItemFactory @Inject constructor(
        private val userPreferencesProvider: UserPreferencesProvider,
        private val messageInformationDataFactory: MessageInformationDataFactory,
        private val messageItemAttributesFactory: MessageItemAttributesFactory,
        private val noticeItemFactory: NoticeItemFactory,
        private val dimensionConverter: DimensionConverter,
        private val timelineMediaSizeProvider: TimelineMediaSizeProvider,
        private val avatarSizeProvider: AvatarSizeProvider,
) {

    fun create(params: TimelineItemFactoryParams): VectorEpoxyModel<*>? {
        val event = params.event
        if (event.root.eventId == null) return null
        val showHiddenEvents = userPreferencesProvider.shouldShowHiddenEvents()
        val liveLocationEventGroup = params.eventsGroup?.let { LiveLocationEventsGroup(it) } ?: return null
        val attributes = buildMessageAttributes(params)

        val item = when (liveLocationEventGroup.getCurrentStatus()) {
            LiveLocationEventsGroup.LiveLocationSharingStatus.Loading  -> buildLoadingItem(highlight = params.isHighlighted, attributes = attributes)
            LiveLocationEventsGroup.LiveLocationSharingStatus.Stopped  -> buildStoppedItem()
            is LiveLocationEventsGroup.LiveLocationSharingStatus.Lived -> buildLivedItem()
            LiveLocationEventsGroup.LiveLocationSharingStatus.Unkwown  -> null
        }

        return if (item == null && showHiddenEvents) {
            // Fallback to notice item for showing hidden events
            noticeItemFactory.create(params)
        } else {
            item
        }
    }

    private fun buildMessageAttributes(params: TimelineItemFactoryParams): AbsMessageItem.Attributes {
        val informationData = messageInformationDataFactory.create(params)
        return messageItemAttributesFactory.create(null, informationData, params.callback, params.reactionsSummaryEvents)
    }

    private fun buildLoadingItem(highlight: Boolean, attributes: AbsMessageItem.Attributes): MessageLiveLocationStartItem {
        val width = timelineMediaSizeProvider.getMaxSize().first
        val height = dimensionConverter.dpToPx(MessageItemFactory.MESSAGE_LOCATION_ITEM_HEIGHT_IN_DP)

        return MessageLiveLocationStartItem_()
                .attributes(attributes)
                .mapWidth(width)
                .mapHeight(height)
                .highlighted(highlight)
                .leftGuideline(avatarSizeProvider.leftGuideline)
    }

    private fun buildStoppedItem() = null

    private fun buildLivedItem() = null
}
