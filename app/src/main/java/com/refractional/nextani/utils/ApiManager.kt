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
        val request = request(
            onSuccess = {
                val list = it.getJSONObject("data")
                    .getJSONObject("Page")
                    .getJSONArray("mediaList")

                //if media list is empty
                if (list.length() < 1) {
                    onSuccess()
                    onComplete()
                } else {
                    for (i in 0..<list.length()) {
                        val entry = list.getJSONObject(i)
                        val parsed = parseMedia(entry = entry)!!
                        ratedAnimeDao.deleteId(parsed.id)
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
                //parse it
                val media = it.getJSONObject("data").getJSONObject("Media")
                val parsed = parseMedia(_media = media)!!
                ratedAnimeDao.deleteId(parsed.id)
                ratedAnimeDao.insertAll(parsed)
                onSuccess(parsed)
            },
            query = singleAnimeQuery(id),
            onError = {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                Log.e("Fetch and update media by ID", it.message ?: "no error message")
            }
        )
    }

    /**
     * @param entry the entry JSON object. null by default
     * @param _media the media JSON object. null by default
     * @return parsed RatedAnime object. Returns null if both parameters are null or not provided. Returns null if JSON objects are formatted unexpectedly
     */
    private fun parseMedia(entry: JSONObject? = null, _media: JSONObject? = null): RatedAnime? {
        try {
            if (entry == null && _media == null) return null
            val media = if (entry != null) entry.getJSONObject("media") else _media!!

            return RatedAnime(
                id = media.getInt("id"),
                rating = entry?.getDouble("score"),
                avgScore = media.getInt("averageScore"),
                status = entry?.getString("status"),
                type = media.getString("type"),
                format = media.getString("format"),
                title = media.getJSONObject("title").getString("userPreferred"),
                popularity = media.getInt("popularity"),
                year = media.getInt("seasonYear"),
                airStatus = media.getString("status"),
                imgUrl = media.getJSONObject("coverImage").getString("extraLarge"),
                color = media.getJSONObject("coverImage").getString("color"),
                episodes = media.getInt("episodes")
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
                "  Page(page: $pageNum) {\n" +
                "    mediaList (userName: $username, type: ANIME) {\n" +
                "      userId\n" +
                "      score\n" +
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