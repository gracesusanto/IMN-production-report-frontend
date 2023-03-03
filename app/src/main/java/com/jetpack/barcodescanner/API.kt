package com.jetpack.barcodescanner

import android.util.Log
import androidx.compose.ui.text.toLowerCase
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

private const val TAG = "API"

var URL = "http://192.168.68.143:8000"

//var URL = "http://192.168.18.137:8000"
//var URL = "http://192.168.0.224:8000"
//var URL = "http://192.168.0.218:8000"
//var URL = "http://192.168.0.202:8000"

class API {
    companion object {

        fun getDetail(
            category: Category,
            toolingId: String,
            ResponseListener: ((response: JSONObject) -> Unit)? = null,
            ErrorListener: ((error: VolleyError) -> Unit)? = null
        ) {
            Log.d(TAG, "tooling request: \"$URL/${category.name.lowercase()}/$toolingId\"")

            val req = JsonObjectRequest(
                Request.Method.GET, "$URL/${category.name.lowercase()}/$toolingId", null,
                { response ->
                    Log.i(TAG, "tooling response: $response")
                    ResponseListener?.invoke(response)
                },
                { error ->
                    error.message?.let { Log.e(TAG, "tooling error: $it") }
                    ErrorListener?.invoke(error)
                }
            ).setRetryPolicy(
                DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                    -1, // Disable retry
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            ).setShouldCache(false)
            NetworkManager.getInstance().add(req)
        }

        fun getMesinStatus(
            mesinId: String,
            ResponseListener: ((response: JSONObject) -> Unit)? = null,
            ErrorListener: ((error: VolleyError) -> Unit)? = null
        ) {
            Log.i(TAG, "mesin status request: $URL/mesin/status/$mesinId")
            val req = JsonObjectRequest(
                Request.Method.GET, "$URL/mesin/status/$mesinId", null,
                { response ->
                    Log.i(TAG, "mesin status response: $response")
                    ResponseListener?.invoke(response)
                },
                { error ->
                    error.message?.let { Log.e(TAG, "mesin status error: $it") }
                    ErrorListener?.invoke(error)
                }
            ).setRetryPolicy(
                DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                    -1, // Disable retry
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            ).setShouldCache(false)
            NetworkManager.getInstance().add(req)
        }

        fun getOperatorStatus(
            operatorId: String,
            ResponseListener: ((response: JSONObject) -> Unit)? = null,
            ErrorListener: ((error: VolleyError) -> Unit)? = null
        ) {
            val req = JsonObjectRequest(
                Request.Method.GET, "$URL/operator/status/$operatorId", null,
                { response ->
                    Log.i(TAG, "operator status response: $response")
                    ResponseListener?.invoke(response)
                },
                { error ->
                    error.message?.let { Log.e(TAG, "operator status error: $it") }
                    ErrorListener?.invoke(error)
                }
            ).setRetryPolicy(
                DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                    -1, // Disable retry
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            ).setShouldCache(false)
            NetworkManager.getInstance().add(req)
        }

        fun postActivity(
            type: String,
            toolingId: String,
            mesinId: String,
            operatorId: String,
            categoryDowntime: String?,
            output: Int?,
            reject: Int?,
            rework: Int?,
            coilNo: String?,
            lotNo: String?,
            packNo: String?,
            ResponseListener: ((response: JSONObject) -> Unit)? = null,
            ErrorListener: ((error: VolleyError) -> Unit)? = null
        ) {
            val body = JSONObject()
            body.put("type", type)
            body.put("tooling_id", toolingId)
            body.put("mesin_id", mesinId)
            body.put("operator_id", operatorId)
            body.put("output", output)
            body.put("reject", reject)
            body.put("rework", rework)
            if (!coilNo.isNullOrEmpty()) {
                body.put("coil_no", coilNo)
            }
            if (!lotNo.isNullOrEmpty()) {
                body.put("lot_no", lotNo)
            }
            if (!packNo.isNullOrEmpty()) {
                body.put("pack_no", packNo)
            }
            if (!categoryDowntime.isNullOrEmpty()) {
                body.put("category_downtime", categoryDowntime)
            }
            Log.d(TAG, body.toString())
            val req = JsonObjectRequest(
                Request.Method.POST, "$URL/activity", body,
                {
                    Log.d(TAG, "activity response: $it")
                    ResponseListener?.invoke(it)
                },
                { error ->
                    error.message?.let { Log.e(TAG, "activity: $it") }
                    ErrorListener?.invoke(error)
                }
            ).setRetryPolicy(
                DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                    -1, // Disable retry
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            ).setShouldCache(false)
            NetworkManager.getInstance().add(req)
        }

        fun checkOperatorStatus(
            toolingId: String,
            mesinId: String,
            operatorId: String,
            ResponseListener: ((response: JSONObject) -> Unit)? = null,
            ErrorListener: ((error: VolleyError) -> Unit)? = null
        ) {
            val body = JSONObject()
            body.put("tooling_id", toolingId)
            body.put("mesin_id", mesinId)
            body.put("operator_id", operatorId)
            Log.d(TAG, "/operator-status body $body")
            val req = JsonObjectRequest(
                Request.Method.POST, "$URL/operator-status", body,
                {
                    Log.d(TAG, "/operator-status response: $it")
                    ResponseListener?.invoke(it)
                },
                { error ->
                    error.message?.let { Log.e(TAG, "/operator-status: $it") }
                    ErrorListener?.invoke(error)
                }
            ).setRetryPolicy(
                DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                    -1, // Disable retry
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            ).setShouldCache(false)
            NetworkManager.getInstance().add(req)
        }

        fun getListMesinStatus(
            ResponseListener: ((response: JSONObject) -> Unit)? = null,
            ErrorListener: ((error: VolleyError) -> Unit)? = null
        ) {
            val req = JsonObjectRequest(
                Request.Method.GET, "$URL/mesin-status-all", null,
                { response ->
                    Log.i(TAG, "/mesin-status-all response: $response")
                    ResponseListener?.invoke(response)
                },
                { error ->
                    error.message?.let { Log.e(TAG, "/mesin-status-all error: $it") }
                    ErrorListener?.invoke(error)
                }
            ).setRetryPolicy(
                DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 10,
                    -1, // Disable retry
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            ).setShouldCache(false)
            NetworkManager.getInstance().add(req)
        }
    }
}