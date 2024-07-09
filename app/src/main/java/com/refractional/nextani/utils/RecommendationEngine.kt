package com.refractional.nextani.utils

import com.refractional.nextani.utils.database.DbManager
import com.refractional.nextani.utils.database.model.RatedAnime

class RecommendationEngine {

    class Recommendation(val recc: RatedAnime) {
        val influences: ArrayList<Influence> = arrayListOf()
        var points = 0
            private set

        fun addInfluence(influencer: RatedAnime, points: Int) {
            val influence = influences.firstOrNull {
                it.influencer.id == influencer.id
            } ?: Influence(influencer).also {
                influences.add(it)
            }
            influence.addPoints(points)
            this.points += points
        }
    }
    class Influence(val influencer: RatedAnime) {
        var points: Int = 0
            private set

        fun addPoints(points: Int) {
            this.points += points
        }
    }
    companion object {
        fun generateReccs(db: DbManager): ArrayList<Recommendation> {
            val anilistReccs = db.anilistReccDao().getAll()
            val topPopularity = db.ratedAnimeDao().getMostPopular().popularity
            val reccs: ArrayList<Recommendation> = arrayListOf()

            anilistReccs.forEach { anilistRecc ->
                val points = calculatePoints(
                    topPopularity = topPopularity,
                    source = anilistRecc.source,
                    reccPopularity = anilistRecc.recc.popularity,
                    rating = anilistRecc.rating
                )
                val recommendation = reccs.firstOrNull {
                    it.recc.id == anilistRecc.recc.id
                } ?: Recommendation(anilistRecc.recc).also {
                    reccs.add(it)
                }
                recommendation.addInfluence(anilistRecc.source, points)
            }

            return reccs
        }

        private fun calculatePoints(
            topPopularity: Int,
            source: RatedAnime,
            reccPopularity: Int,
            rating: Int
        ): Int {
            val sourcePopRatio = topPopularity / source.popularity
            val reccPopRatio = topPopularity / reccPopularity
            val sourceRatingBoosted = rating * sourcePopRatio
            val reccRatingBoosted = rating * reccPopRatio
            val boostedRating = (sourceRatingBoosted + reccRatingBoosted)
            return boostedRating * ((source.rating ?: 50).takeIf { it > 0 } ?: 50 )
        }
    }
}