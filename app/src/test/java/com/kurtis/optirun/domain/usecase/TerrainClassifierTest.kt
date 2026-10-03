package com.kurtis.optirun.domain.usecase

import com.kurtis.optirun.domain.model.Route
import com.kurtis.optirun.domain.model.TerrainPref
import org.junit.Assert.*
import org.junit.Test

class TerrainClassifierTest {
    private val classifier = TerrainClassifier()
    private fun route(km: Double, ascent: Double) = Route(km, ascent, ascent, km * 6, emptyList())

    @Test fun `low climbing is flat`() =
        assertEquals(TerrainPref.FLAT, classifier.classify(route(5.0, 20.0)))      // 4 m/km

    @Test fun `high climbing is hilly`() =
        assertEquals(TerrainPref.HILLY, classifier.classify(route(5.0, 150.0)))    // 30 m/km

    @Test fun `zero distance does not crash and counts as flat`() =
        assertEquals(TerrainPref.FLAT, classifier.classify(route(0.0, 50.0)))

    @Test fun `pickBest flat chooses least climbing per km`() {
        val routes = listOf(route(5.0, 100.0), route(5.0, 10.0), route(5.0, 60.0))
        assertEquals(10.0, classifier.pickBest(routes, TerrainPref.FLAT)!!.ascentM, 0.0)
    }

    @Test fun `pickBest hilly chooses most climbing per km`() {
        val routes = listOf(route(5.0, 100.0), route(5.0, 10.0), route(5.0, 60.0))
        assertEquals(100.0, classifier.pickBest(routes, TerrainPref.HILLY)!!.ascentM, 0.0)
    }

    @Test fun `pickBest on empty list returns null`() =
        assertNull(classifier.pickBest(emptyList(), TerrainPref.FLAT))
}