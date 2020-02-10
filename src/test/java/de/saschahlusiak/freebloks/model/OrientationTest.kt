package de.saschahlusiak.freebloks.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationTest {
    @Test
    fun test_rotatedLeft_four() {
        var o = Orientation()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.None, o.rotation)

        o = o.rotatedLeft()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.Left, o.rotation)

        o = o.rotatedLeft()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.Half, o.rotation)

        o = o.rotatedLeft()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.Right, o.rotation)

        o = o.rotatedLeft()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.None, o.rotation)
    }

    @Test
    fun test_rotatedLeft_two() {
        var o = Orientation()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.None, o.rotation)

        o = o.rotatedLeft(Rotatable.Two)
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.Right, o.rotation)

        o = o.rotatedLeft(Rotatable.Two)
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.None, o.rotation)
    }

    @Test
    fun test_rotatedRight_four() {
        var o = Orientation()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.None, o.rotation)

        o = o.rotatedRight()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.Right, o.rotation)

        o = o.rotatedRight()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.Half, o.rotation)

        o = o.rotatedRight()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.Left, o.rotation)

        o = o.rotatedRight()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.None, o.rotation)
    }

    @Test
    fun test_rotatedRight_two() {
        var o = Orientation()
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.None, o.rotation)

        o = o.rotatedRight(Rotatable.Two)
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.Right, o.rotation)

        o = o.rotatedRight(Rotatable.Two)
        assertEquals(false, o.mirrored)
        assertEquals(Rotation.None, o.rotation)
    }

    @Test
    fun test_flip_around() {
        val o = Orientation()

        assertEquals(o, o.rotatedRight().rotatedRight().mirroredHorizontally().mirroredVertically())
        assertEquals(o, o.rotatedLeft().rotatedLeft().mirroredVertically().mirroredHorizontally())
        assertEquals(o, o.mirroredHorizontally().rotatedRight().mirroredVertically().rotatedLeft())
        assertEquals(o, o.rotatedLeft().mirroredHorizontally().rotatedLeft().mirroredHorizontally())
    }
}