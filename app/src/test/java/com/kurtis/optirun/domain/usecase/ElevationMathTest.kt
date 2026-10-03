package com.kurtis.optirun.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ElevationMathTest {
    @Test fun `small noise counts as zero climb`() =
        assertEquals(0.0, ElevationMath.smoothedAscent(listOf(10.0, 12.0, 9.0, 11.0, 10.0, 13.0)), 0.0)

    @Test fun `single hill counts its height`() =
        assertEquals(20.0, ElevationMath.smoothedAscent(listOf(0.0, 10.0, 20.0, 10.0, 0.0)), 0.0)

    @Test fun `steady climb is counted within threshold`() =
        assertEquals(20.0, ElevationMath.smoothedAscent((0..10).map { it * 2.0 }), 5.0)

    @Test fun `descent mirrors ascent on a loop`() =
        assertEquals(20.0, ElevationMath.smoothedDescent(listOf(0.0, 10.0, 20.0, 10.0, 0.0)), 0.0)

    @Test fun `too few points returns zero`() =
        assertEquals(0.0, ElevationMath.smoothedAscent(listOf(5.0)), 0.0)
}