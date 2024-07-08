package com.refractional.nextani.utils

import com.refractional.nextani.utils.database.DbManager
import com.refractional.nextani.utils.database.model.RatedAnime

class RecommendationEngine {

    inner class Recommendation(val recc: RatedAnime) {
        val influences: ArrayList<Influence> = arrayListOf()
        var points = 0
            private set

        fun addInfluence(influencer: RatedAnime, points: Int) {
            influences.add(Influence(influencer, points))
            this.points += points
        }
        fun addInfluence(influence: Influence) {
            influences.add(influence)
            this.points += influence.points
        }
    }
    inner class Influence(val influencer: RatedAnime, val points: Int)
    companion object {
        fun generateReccs(db: DbManager): ArrayList<Recommendation> {
            val anilistReccs = db.anilistReccDao().getAll()
            val mostPopular = db.ratedAnimeDao().getMostPopular()

            return arrayListOf() //TODO: make a real function
        }
    }
}