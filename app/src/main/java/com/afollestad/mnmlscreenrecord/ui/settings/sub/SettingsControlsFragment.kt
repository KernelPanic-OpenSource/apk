/**
 * Designed and developed by Aidan Follestad (@afollestad)
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
package com.afollestad.mnmlscreenrecord.ui.settings.sub

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import androidx.preference.SwitchPreference
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.misc.toUri
import com.afollestad.mnmlscreenrecord.common.permissions.PermissionChecker
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_ALWAYS_SHOW_CONTROLS
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_FLOATING_CONTROLS
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SCREEN_OFF
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_STOP_ON_SHAKE
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.engine.permission.OverlayExplanationCallback
import com.afollestad.mnmlscreenrecord.engine.permission.OverlayExplanationDialog
import com.afollestad.mnmlscreenrecord.ui.settings.base.BaseSettingsFragment
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
class SettingsControlsFragment : BaseSettingsFragment(), OverlayExplanationCallback {
  companion object {
    private const val DRAW_OVER_OTHER_APP_PERMISSION = 68
  }

  private val permissionChecker by inject<PermissionChecker>()

  private val stopOnScreenOffPref by inject<Pref<Boolean>>(name = PREF_STOP_ON_SCREEN_OFF)
  private val alwaysShowControlsPref by inject<Pref<Boolean>>(
      name = PREF_ALWAYS_SHOW_CONTROLS
  )
  private val stopOnShakePref by inject<Pref<Boolean>>(name = PREF_STOP_ON_SHAKE)
  private val floatingControlsPref by inject<Pref<Boolean>>(name = PREF_FLOATING_CONTROLS)

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) {
    setPreferencesFromResource(R.xml.settings_controls, rootKey)

    setupAlwaysShowControlsPref()
    setupStopOnScreenOffPref()
    setupStopOnShakePref()
    setupFloatingControlsPref()
  }

  private fun setupAlwaysShowControlsPref() {
    val alwaysShowControlsEntry =
      findPreference(PREF_ALWAYS_SHOW_CONTROLS) as SwitchPreference
    alwaysShowControlsEntry.setOnPreferenceChangeListener { _, newValue ->
      alwaysShowControlsPref.set(newValue as Boolean)
      true
    }
    alwaysShowControlsPref.observe()
        .distinctUntilChanged()
        .subscribe { alwaysShowControlsEntry.isChecked = it }
        .attachLifecycle(this)
  }

  private fun setupFloatingControlsPref() {
    val floatingControlsEntry =
      findPreference(PREF_FLOATING_CONTROLS) as SwitchPreference
    floatingControlsEntry.setOnPreferenceChangeListener { _, newValue ->
      if (permissionChecker.hasOverlayPermission()) {
        floatingControlsPref.set(newValue as Boolean)
        true
      } else {
        OverlayExplanationDialog.show(this@SettingsControlsFragment)
        false
      }
    }
    floatingControlsPref.observe()
        .subscribe { floatingControlsEntry.isChecked = it }
        .attachLifecycle(this)
  }

  override fun onShouldAskForOverlayPermission() {
    val intent = Intent(
        ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:${activity?.packageName}".toUri()
    )
    startActivityForResult(
        intent,
        DRAW_OVER_OTHER_APP_PERMISSION
    )
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION && permissionChecker.hasOverlayPermission()) {
      floatingControlsPref.set(true)
    }
  }

  private fun setupStopOnScreenOffPref() {
    val stopOnScreenOffEntry = findPreference(PREF_STOP_ON_SCREEN_OFF) as SwitchPreference
    stopOnScreenOffEntry.setOnPreferenceChangeListener { _, newValue ->
      stopOnScreenOffPref.set(newValue as Boolean)
      true
    }
    stopOnScreenOffPref.observe()
        .distinctUntilChanged()
        .subscribe { stopOnScreenOffEntry.isChecked = it }
        .attachLifecycle(this)
  }

  private fun setupStopOnShakePref() {
    val stopOnShakeEntry = findPreference(PREF_STOP_ON_SHAKE) as SwitchPreference
    stopOnShakeEntry.setOnPreferenceChangeListener { _, newValue ->
      stopOnShakePref.set(newValue as Boolean)
      true
    }
    stopOnShakePref.observe()
        .distinctUntilChanged()
        .subscribe { stopOnShakeEntry.isChecked = it }
        .attachLifecycle(this)
  }
}
