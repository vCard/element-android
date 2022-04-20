/*
 * Copyright (c) 2021 New Vector Ltd
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

package im.vector.app.features.location

import android.Manifest
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import androidx.core.location.LocationListenerCompat
import im.vector.app.BuildConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(
        context: Context
) : LocationListenerCompat {

    private val locationManager = context.getSystemService<LocationManager>()

    interface Callback {
        fun onLocationUpdate(locationData: LocationData)
        fun onLocationProviderIsNotAvailable()
    }

    private var currentProvider: String? = null
    private val callbacks = mutableListOf<Callback>()

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun start() {
        Timber.d("## LocationTracker. start()")

        if (locationManager == null) {
            callbacks.forEach { it.onLocationProviderIsNotAvailable() }
            Timber.v("## LocationTracker. LocationManager is not available")
            return
        }

        currentProvider = null

        val criteria = getProviderCriteria()
        locationManager.getBestProvider(criteria, true)
                ?.let { provider ->
                    Timber.d("## LocationTracker. track location using $provider")

                    currentProvider = provider

                    // Notify last known location without waiting location updates
                    notifyLastKnownLocation(locationManager, provider)

                    locationManager.requestLocationUpdates(
                            provider,
                            MIN_TIME_TO_UPDATE_LOCATION_MILLIS,
                            MIN_DISTANCE_TO_UPDATE_LOCATION_METERS,
                            this
                    )
                }
                ?: run {
                    callbacks.forEach { it.onLocationProviderIsNotAvailable() }
                    Timber.v("## LocationTracker. There is no location provider available")
                }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun stop() {
        Timber.d("## LocationTracker. stop()")
        locationManager?.removeUpdates(this)
        callbacks.clear()
    }

    /**
     * Request the last known location. It will be given async through Callback.
     * Please ensure adding a callback to receive the value.
     */
    fun requestLastKnownLocation() {
        locationManager?.let { locManager ->
            currentProvider?.let {
                notifyLastKnownLocation(locManager, it)
            }
        }
    }

    private fun getProviderCriteria(): Criteria {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_MEDIUM
        return criteria
    }

    private fun notifyLastKnownLocation(locationManager: LocationManager, provider: String) {
        locationManager.getLastKnownLocation(provider)?.let { lastKnownLocation ->
            if (BuildConfig.LOW_PRIVACY_LOG_ENABLE) {
                Timber.d("## LocationTracker. lastKnownLocation: $lastKnownLocation")
            } else {
                Timber.d("## LocationTracker. lastKnownLocation: ${lastKnownLocation.provider}")
            }
            notifyLocation(lastKnownLocation)
        }
    }

    fun addCallback(callback: Callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    fun removeCallback(callback: Callback) {
        callbacks.remove(callback)
        if (callbacks.size == 0) {
            stop()
        }
    }

    override fun onLocationChanged(location: Location) {
        if (BuildConfig.LOW_PRIVACY_LOG_ENABLE) {
            Timber.d("## LocationTracker. onLocationChanged: $location")
        } else {
            Timber.d("## LocationTracker. onLocationChanged: ${location.provider}")
        }
        notifyLocation(location)
    }

    private fun notifyLocation(location: Location) {
        callbacks.forEach { it.onLocationUpdate(location.toLocationData()) }
    }

    override fun onProviderDisabled(provider: String) {
        Timber.d("## LocationTracker. onProviderDisabled: $provider")
        callbacks.forEach { it.onLocationProviderIsNotAvailable() }
    }

    private fun Location.toLocationData(): LocationData {
        return LocationData(latitude, longitude, accuracy.toDouble())
    }
}
