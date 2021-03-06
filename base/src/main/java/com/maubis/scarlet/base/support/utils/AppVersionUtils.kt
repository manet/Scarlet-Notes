package com.maubis.scarlet.base.support.utils

import com.maubis.scarlet.base.BuildConfig
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.main.sheets.WhatsNewBottomSheet.Companion.WHATS_NEW_UID
import java.util.*

const val KEY_LAST_KNOWN_APP_VERSION = "KEY_LAST_KNOWN_APP_VERSION"
const val KEY_LAST_SHOWN_WHATS_NEW = "KEY_LAST_SHOWN_WHATS_NEW"
const val KEY_INSTANCE_ID = "KEY_INSTANCE_ID"

fun getCurrentVersionCode(): Int {
  return BuildConfig.VERSION_CODE
}

/**
 * Returns app version if it's guaranteed the user an app version. (Stored in the app version variable)
 * If the user has notes it is assumed that the user was at-least at the last version. returns : -1
 * If nothing can be concluded it's 0 (assumes new user)
 */
fun getLastUsedAppVersionCode(): Int {
  val appVersion = CoreConfig.instance.store().get(KEY_LAST_KNOWN_APP_VERSION, 0)
  return when {
    appVersion > 0 -> appVersion
    notesDb.getCount() > 0 -> -1
    else -> 0
  }
}

fun shouldShowWhatsNewSheet(): Boolean {
  val lastShownWhatsNew = CoreConfig.instance.store().get(KEY_LAST_SHOWN_WHATS_NEW, 0)
  if (lastShownWhatsNew >= WHATS_NEW_UID) {
    // Already shown the latest
    return false
  }

  val lastUsedAppVersion = getLastUsedAppVersionCode()

  // Update the values independent of the decision
  CoreConfig.instance.store().put(KEY_LAST_SHOWN_WHATS_NEW, WHATS_NEW_UID)
  CoreConfig.instance.store().put(KEY_LAST_KNOWN_APP_VERSION, getCurrentVersionCode())

  // New users don't need to see the whats new screen
  return lastUsedAppVersion != 0
}

fun getInstanceID(): String {
  val deviceId = CoreConfig.instance.store().get(KEY_INSTANCE_ID, "")
  if (deviceId.isBlank()) {
    val newDeviceId = UUID.randomUUID().toString()
    CoreConfig.instance.store().put(KEY_INSTANCE_ID, newDeviceId)
    return newDeviceId
  }
  return deviceId
}