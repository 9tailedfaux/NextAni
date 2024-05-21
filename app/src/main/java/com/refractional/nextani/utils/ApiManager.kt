package com.refractional.nextani.utils

import android.content.Context
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ApiManager(context: Context) {

    private val volley = Volley.newRequestQueue(context)
    fun refreshUserData(username: String = "9tailedfaux", onSuccess: (JSONObject) -> Unit = {}, onError: (VolleyError) -> Unit = {}, onComplete: () -> Unit = {}) {
        val request = request(
            {
                println(it)
                onSuccess(it)
            },
            {
                println(it.message)
                onError(it)
            },
            onComplete,
            userQuery(username, 1)
        )
        volley.add(request)
    }

    fun refreshRelations(aniID: String) {

    }

    companion object {
        val baseURL = "https://graphql.anilist.co"
        val userListQuery = "query {\n" +
                "  Page(page: 1) {\n" +
                "    mediaList(userName: \"9tailedfaux\", type: ANIME, sort: SCORE_DESC, status: COMPLETED) {\n" +
                "      media {\n" +
                "        title {\n" +
                "          userPreferred\n" +
                "        }\n" +
                "        status\n" +
                "        format\n" +
                "        seasonYear\n" +
                "        episodes\n" +
                "        popularity\n" +
                "        score\n" +
                "        recommendations {\n" +
                "          edges {\n" +
                "            node {\n" +
                "              rating\n" +
                "              mediaRecommendation {\n" +
                "                id\n" +
                "                title {\n" +
                "                  userPreferred\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    pageInfo {\n" +
                "      hasNextPage\n" +
                "    }\n" +
                "  }\n" +
                "}"

        fun userQuery(username: String, pageNum: Int) =
                "query {\n" +
                "  Page(page: $pageNum) {\n" +
                "    users(name: \"$username\") {\n" +
                "      id\n" +
                "      statistics {\n" +
                "        anime {\n" +
                "          scores (sort: MEAN_SCORE_DESC) {\n" +
                "            score\n" +
                "            mediaIds\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"

        fun request(
            onSuccess: (JSONObject) -> Unit,
            onError: (VolleyError) -> Unit,
            onComplete: () -> Unit,
            query: String
        ) = object : StringRequest(
            Method.POST, baseURL,
            {
                onSuccess(JSONObject(it))
                onComplete()
            },
            {
                onError(it)
                onComplete()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    Pair("query", query),
                )
            }
        }
    }
}