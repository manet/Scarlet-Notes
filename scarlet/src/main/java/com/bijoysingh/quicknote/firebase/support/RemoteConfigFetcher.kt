package com.bijoysingh.quicknote.firebase.support

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.maubis.scarlet.base.config.*

const val REMOTE_CONFIG_URL = "https://material-notes-63563.firebaseapp.com/config/config.txt"
const val REMOTE_CONFIG_REFETCH_TIME_MS = 7 * 24 * 60 * 60 * 1000
const val KEY_REMOTE_CONFIG_FETCH_TIME = "KEY_REMOTE_CONFIG_FETCH_TIME"

class RemoteConfigFetcher() : IRemoteConfigFetcher {

  override fun tryFetching(context: Context) {
    val lastFetched = CoreConfig.instance.store().get(KEY_REMOTE_CONFIG_FETCH_TIME, 0L)
    if (System.currentTimeMillis() > lastFetched + REMOTE_CONFIG_REFETCH_TIME_MS) {
      fetchConfig(context)
    }
  }

  fun fetchConfig(context: Context) {
    val request = object : StringRequest(
        Request.Method.GET,
        REMOTE_CONFIG_URL,
        Response.Listener { response -> onSuccess(response) },
        Response.ErrorListener { _ -> }) {}
    request.retryPolicy = DefaultRetryPolicy(
        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    request.setShouldCache(false)
    Volley.newRequestQueue(context).add(request)
  }

  fun onSuccess(response: String?) {
    if (response === null) {
      return
    }

    CoreConfig.instance.store().put(KEY_REMOTE_CONFIG_FETCH_TIME, System.currentTimeMillis())
    try {
      val config = Gson().fromJson<RemoteConfig>(response, RemoteConfig::class.java)
      CoreConfig.instance.store().put(KEY_RC_LITE_VERSION, config.rc_lite_production_version ?: 0)
      CoreConfig.instance.store().put(KEY_RC_FULL_VERSION, config.rc_full_production_version ?: 0)
    } catch (exception: Exception) {
      return
    }
  }
}