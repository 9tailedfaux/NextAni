package com.refractional.nextani.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.VolleyError
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
        onError: (VolleyError) -> Unit = {},
        onComplete: () -> Unit = {},
        pageNum: Int = 1
    ) {
        ratedAnimeDao.deleteAll()
        db.anilistReccDao().deleteAll()
        val request = request(
            onSuccess = {
                val list = it.getJSONObject("data")
                    .getJSONObject("Page")
                    .getJSONArray("mediaList")

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
                        val entry = list.getJSONObject(i)
                        val parsed = parseMedia(entry = entry)!!
                        ratedAnimeDao.insertAll(parsed)

                        parseRecs(
                            parent = parsed,
                            edges = entry.getJSONObject("media").getJSONObject("recommendations").getJSONArray("edges")
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
                Log.e("Refresh user data", it.message ?: "no error message")
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
                val media = it.getJSONObject("data")
                    .getJSONObject("Page")
                    .getJSONObject("media")

                val parsed = parseMedia(media = media)!!
                ratedAnimeDao.insertAll(parsed)

                onSuccess(parsed)
                onComplete()
            },
            onError = {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                Log.e("fetch most popular", it.message ?: "no error message")
            },
            query = MOST_POPULAR_QUERY
        ).also { volley.add(it) }
    }

    private fun parseRecs(parent: RatedAnime, edges: JSONArray) {
        for (i in 0..<edges.length()) {

            val node = edges.getJSONObject(i).getJSONObject("node")
            val id = node.getJSONObject("mediaRecommendation").getInt("id")

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
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                Log.e("Fetch and update media by ID", it.message ?: "no error message")
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
            val myMedia = if (entry != null) entry.getJSONObject("media") else media!!

            return RatedAnime(
                id = myMedia.getInt("id"),
                rating = entry?.getInt("score"),
                avgScore = myMedia.getInt("averageScore"),
                status = entry?.getString("status"),
                type = myMedia.getString("type"),
                format = myMedia.getString("format"),
                title = myMedia.getJSONObject("title").getString("userPreferred"),
                popularity = myMedia.getInt("popularity"),
                year = myMedia.getInt("seasonYear"),
                airStatus = myMedia.getString("status"),
                imgUrl = myMedia.getJSONObject("coverImage").getString("extraLarge"),
                color = myMedia.getJSONObject("coverImage").getString("color"),
                episodes = myMedia.getInt("episodes")
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
        const val MOST_POPULAR_QUERY = "query {\n" +
                "  Page(page: 1, perPage: 1) {\n" +
                "    media(sort: POPULARITY_DESC) {\n" +
                "      popularity\n" +
                "    }\n" +
                "  }\n" +
                "}"

        fun request(
            onSuccess: (JSONObject) -> Unit,
            onError: (VolleyError) -> Unit = {},
            onComplete: () -> Unit = {},
            query: String
        ) = object : StringRequest(
            Method.POST, BASEURL,
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