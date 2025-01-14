/*
 * Copyright 2020 The Matrix.org Foundation C.I.C.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrix.android.sdk.internal.database.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.matrix.android.sdk.internal.database.model.livelocation.LiveLocationShareAggregatedSummaryEntity
import timber.log.Timber

internal open class EventAnnotationsSummaryEntity(
        @PrimaryKey
        var eventId: String = "",
        var roomId: String? = null,
        var reactionsSummary: RealmList<ReactionAggregatedSummaryEntity> = RealmList(),
        var editSummary: EditAggregatedSummaryEntity? = null,
        var referencesSummaryEntity: ReferencesAggregatedSummaryEntity? = null,
        var pollResponseSummary: PollResponseAggregatedSummaryEntity? = null,
        var liveLocationShareAggregatedSummary: LiveLocationShareAggregatedSummaryEntity? = null,
) : RealmObject() {

    /**
     * Cleanup undesired editions, done by users different from the originalEventSender
     */
    fun cleanUp(originalEventSenderId: String?) {
        originalEventSenderId ?: return

        editSummary?.editions?.filter {
            it.senderId != originalEventSenderId
        }
                ?.forEach {
                    Timber.w("Deleting an edition from ${it.senderId} of event sent by $originalEventSenderId")
                    it.deleteFromRealm()
                }
    }

    companion object
}

internal fun EventAnnotationsSummaryEntity.deleteOnCascade() {
    reactionsSummary.deleteAllFromRealm()
    editSummary?.deleteFromRealm()
    referencesSummaryEntity?.deleteFromRealm()
    pollResponseSummary?.deleteFromRealm()
    deleteFromRealm()
}
