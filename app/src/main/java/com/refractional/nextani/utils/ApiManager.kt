package com.refractional.nextani.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.refractional.nextani.utils.database.DbManager
import com.refractional.nextani.utils.database.model.AnilistRecc
import com.refractional.nextani.utils.database.model.RatedAnime
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ApiManager(private val context: Context, private val db: DbManager) {

    private val volley = Volley.newRequestQueue(context)
    private val ratedAnimeDao = db.ratedAnimeDao()
    //FIXME my username is in here as a default
    fun refreshUserData(
        username: String = "9tailedfaux",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {},
        onComplete: () -> Unit = {},
        pageNum: Int = 1
    ) {
        ratedAnimeDao.deleteAll()
        db.anilistReccDao().deleteAll()
        val request = request(
            onSuccess = {
                val list = it.optJSONObject("data")
                    ?.optJSONObject("Page")
                    ?.optJSONArray("mediaList")

                if (list == null) {
                    val tag = "refreshUserData request onSuccess"
                    val msg = "JSON parsing error on $it"
                    onError("$msg in $tag")
                    Log.e(tag, msg)
                    onComplete()
                    return@request
                }

                //if media list is empty
                if (list.length() < 1) {
                    fetchMostPopular(
                        onSuccess = {
                            onSuccess()
                        },
                        onComplete = onComplete
                    )
                } else {
                    for (i in 0..<list.length()) {
                        val entry = list.optJSONObject(i)
                        val parsed = parseMedia(entry = entry)

                        if (parsed == null) {
                            val tag = "refreshUserData request onSuccess"
                            val msg = "JSON parsing error on $list"
                            onError("$msg in $tag")
                            Log.e(tag, msg)
                            onComplete()
                            return@request
                        }

                        ratedAnimeDao.insertAll(parsed)

                        parseRecs(
                            parent = parsed,
                            edges = entry.optJSONObject("media")?.optJSONObject("recommendations")?.optJSONArray("edges")
                        )
                    }
                    refreshUserData(
                        username = username,
                        onSuccess = onSuccess,
                        onError = onError,
                        onComplete = onComplete,
                        pageNum = pageNum + 1
                    )
                }
            },
            onError = {
                Log.e("Refresh user data", it)
                onError(it)
                onComplete()
            },
            query = userListQuery(username, 1)
        )
        volley.add(request)
    }

    private fun fetchMostPopular(
        onSuccess: (RatedAnime) -> Unit = {},
        onComplete: () -> Unit = {},
    ) {
        request(
            onSuccess = {
                val media = it.optJSONObject("data")
                    ?.optJSONObject("Page")
                    ?.optJSONObject("media")

                val parsed = parseMedia(media = media)

                if (parsed == null) {
                    onComplete()
                    Log.e("fetchMostPopular request onSuccess", "JSON parsing error on $it")
                    return@request
                }

                ratedAnimeDao.insertAll(parsed)

                onSuccess(parsed)
                onComplete()
            },
            onError = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                Log.e("fetchMostPopular request onError", it)
            },
            query = MOST_POPULAR_QUERY
        ).also { volley.add(it) }
    }

    private fun parseRecs(parent: RatedAnime, edges: JSONArray?) {
        if (edges == null) return

        for (i in 0..<edges.length()) {

            val node = edges.optJSONObject(i).optJSONObject("node")
            val id = node?.optJSONObject("mediaRecommendation")?.getIntOrNull("id")

            if (id == null) {
                Log.e("parseReccs", "JSON parse error on ${node ?: "edges[$i]"}")
                return
            }

            fetchAndUpdateMediaById(
                id,
                onSuccess = {
                    val recc = AnilistRecc(
                        source = parent,
                        recc = it,
                        rating = node.getInt("rating")
                    )

                    db.anilistReccDao().insertAll(recc)
                }
            )
        }
    }

    private fun fetchAndUpdateMediaById(
        id: Int,
        onSuccess: (RatedAnime) -> Unit = {}
    ) {
        request(
            onSuccess = {
                val media = it.getJSONObject("data").getJSONObject("Media")
                val parsed = parseMedia(media = media)!!
                ratedAnimeDao.insertAll(parsed)
                onSuccess(parsed)
            },
            query = singleAnimeQuery(id),
            onError = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                Log.e("Fetch and update media by ID", it)
            }
        ).also { volley.add(it) }
    }

    /**
     * @param entry the entry JSON object. null by default
     * @param media the media JSON object. null by default
     * @return parsed RatedAnime object. Returns null if both parameters are null or not provided. Returns null if JSON objects are formatted unexpectedly
     */
    private fun parseMedia(entry: JSONObject? = null, media: JSONObject? = null): RatedAnime? {
        try {
            if (entry == null && media == null) return null
            val myMedia = if (entry != null) entry.optJSONObject("media") else media!!

            return RatedAnime(
                id = myMedia.getInt("id"),
                rating = entry?.getIntOrNull("score"),
                avgScore = myMedia.getIntOrNull("averageScore"),
                status = entry?.getStringOrNull("status"),
                type = myMedia.getStringOrNull("type"),
                format = myMedia.getStringOrNull("format"),
                title = myMedia.optJSONObject("title")?.getStringOrNull("userPreferred"),
                popularity = myMedia.optInt("popularity"),
                year = myMedia.getIntOrNull("seasonYear"),
                airStatus = myMedia.getStringOrNull("status"),
                imgUrl = myMedia.optJSONObject("coverImage")?.getStringOrNull("extraLarge"),
                color = myMedia.optJSONObject("coverImage")?.getStringOrNull("color"),
                episodes = myMedia.getIntOrNull("episodes")
            )
        } catch (e: JSONException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            Log.e("parseMedia", e.message ?: "json exception. no message provided")
            return null
        }

    }

    companion object {
        const val BASEURL = "https://graphql.anilist.co"
        fun userListQuery(username: String, pageNum: Int) =
            "query {\n" +
                "  Page(page: $pageNum, perPage: 50) {\n" +
                "    mediaList (userName: $username, type: ANIME) {\n" +
                "      userId\n" +
                "      score (format: POINT_100)\n" +
                "      status\n" +
                "      media {\n" +
                "        id\n" +
                "        type\n" +
                "        format\n" +
                "        title {\n" +
                "          userPreferred\n" +
                "        }\n" +
                "        popularity\n" +
                "        averageScore\n" +
                "        seasonYear\n" +
                "        status\n" +
                "        episodes\n" +
                "        coverImage {\n" +
                "          extraLarge\n" +
                "          color\n" +
                "        }\n" +
                "        recommendations {\n" +
                "          edges {\n" +
                "            node {\n" +
                "              rating\n" +
                "              mediaRecommendation {\n" +
                "                id\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
        fun singleAnimeQuery(id: Int) = "query {\n" +
                "  Media (id: $id) {\n" +
                "    id\n" +
                "    title {\n" +
                "      userPreferred\n" +
                "    }\n" +
                "    type\n" +
                "    format\n" +
                "    popularity\n" +
                "    averageScore\n" +
                "    seasonYear\n" +
                "    status\n" +
                "    episodes\n" +
                "    coverImage {\n" +
                "      extraLarge\n" +
                "      color\n" +
                "    }\n" +
                "  }\n" +
                "}"

        const val MOST_POPULAR_QUERY = "query {\n" +
                "  Page(page: 1, perPage: 1) {\n" +
                "    media(sort: POPULARITY_DESC, type: ANIME) {\n" +
                "      popularity\n" +
                "    }\n" +
                "  }\n" +
                "}"

        fun request(
            onSuccess: (JSONObject) -> Unit,
            onError: (String) -> Unit = {},
            onComplete: () -> Unit = {},
            query: String
        ) = object : StringRequest(
            Method.POST, BASEURL,
            {
                val result = JSONObject(it)
                if (result.has("errors")) {
                    onError(result.optJSONArray("errors")?.optJSONObject(0)?.optString("message") ?: "unknown graphql query error")
                } else {
                    onSuccess(result)
                }
                onComplete()
            },
            {
                onError(it.message ?: "unknown http error")
                onComplete()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    Pair("query", query),
                )
            }
        }

        fun JSONObject.getIntOrNull(name: String): Int? {
            return try {
                getInt(name)
            } catch (e: JSONException) {
                null
            }
        }

        fun JSONObject.getStringOrNull(name: String): String? {
            return try {
                getString(name)
            } catch (e: JSONException) {
                null
            }
        }
    }
}