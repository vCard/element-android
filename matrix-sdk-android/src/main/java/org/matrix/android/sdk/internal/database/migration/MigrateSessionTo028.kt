/*
 * Copyright (c) 2022 The Matrix.org Foundation C.I.C.
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

package org.matrix.android.sdk.internal.database.migration

import io.realm.DynamicRealm
import org.matrix.android.sdk.internal.database.model.livelocation.LiveLocationShareAggregatedSummaryEntityFields
import org.matrix.android.sdk.internal.util.database.RealmMigrator

/**
 * Migrating to:
 * Live location sharing aggregated summary
 */
internal class MigrateSessionTo028(realm: DynamicRealm) : RealmMigrator(realm, 28) {

    override fun doMigrate(realm: DynamicRealm) {
        realm.schema.get("LiveLocationShareAggregatedSummaryEntity")
                ?.takeIf { !it.hasPrimaryKey() }
                ?.addPrimaryKey(LiveLocationShareAggregatedSummaryEntityFields.EVENT_ID)
    }
}
