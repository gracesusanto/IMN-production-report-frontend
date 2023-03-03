package com.jetpack.barcodescanner

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * A singleton to store the RequestQueue for Volley network requests.
 *
 * @property context the Android context.
 * @constructor Creates a NetworkManager for Volley network requests.
 */
class NetworkManager(context: Context) {
    private var queue: RequestQueue = Volley.newRequestQueue(context)

    companion object {
        private var instance: NetworkManager? = null

        /**
         * Get the NetworkManager instance or create it if it does not exist.
         *
         * @param context the Android context.
         * @return the NetworkManager.
         */
        @Synchronized
        fun getInstance(context: Context? = null): NetworkManager {
            if (instance == null) {
                instance = NetworkManager(context!!)
            }
            return instance as NetworkManager
        }
    }

    /**
     * A helper method for RequestQueue add().
     *
     * @param req a Volley network request.
     */
    @Synchronized
    fun <T : Any?> add(req: Request<T>) {
        queue.add(req)
    }
}
