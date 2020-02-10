package de.saschahlusiak.freebloks.model

import org.junit.Assert.*
import org.junit.Test

class ShapeTest {
    @Test
    fun test_shapes_count() {
        val all = Shape.All
        assertEquals(Shape.COUNT, all.size)
    }

    @Test
    fun test_shape_0() {
        val shape = Shape.get(0)

        assertEquals(0, shape.number)
        assertEquals(1, shape.size)

        assertEquals(Mirrorable.Not, shape.mirrorable)
        assertEquals(Rotatable.Not, shape.rotatable)
        assertEquals(1, shape.points)

        assertEquals(1, shape.getStoneField(0, 0, false, Rotation.None))
        assertEquals(1, shape.getStoneField(0, 0, true, Rotation.None))
        assertEquals(1, shape.getStoneField(0, 0, false, Rotation.Right))
        assertEquals(8, shape.getStoneField(0, 1, false, Rotation.None))
        assertEquals(8, shape.getStoneField(1, 0, false, Rotation.None))
    }

    @Test
    fun test_shape_19() {
        val shape = Shape.get(19)
        assertEquals(5, shape.points)
        assertEquals(4, shape.size)
        assertEquals(19, shape.number)
        assertEquals(Mirrorable.Important, shape.mirrorable)
        assertEquals(Rotatable.Four, shape.rotatable)

        assertEquals(2, shape.getStoneField(1, 1, false, Rotation.None))
        assertEquals(2, shape.getStoneField(1, 1, false, Rotation.Right))
        assertEquals(0, shape.getStoneField(1, 1, false, Rotation.Half))
        assertEquals(1, shape.getStoneField(1, 1, false, Rotation.Left))

        assertEquals(2, shape.getStoneField(1, 1, true, Rotation.None))
        assertEquals(2, shape.getStoneField(1, 1, true, Rotation.Right))
        assertEquals(1, shape.getStoneField(1, 1, true, Rotation.Half))
        assertEquals(0, shape.getStoneField(1, 1, true, Rotation.Left))

        assertTrue(shape.isStone(1, 1, false, Rotation.None))
        assertFalse(shape.isCorner(1, 1, false, Rotation.None))
        assertFalse(shape.isFree(1, 1, false, Rotation.None))

        assertFalse(shape.isStone(1, 1, false, Rotation.Half))
        assertFalse(shape.isCorner(1, 1, false, Rotation.Half))
        assertTrue(shape.isFree(1, 1, false, Rotation.Half))

        assertTrue(shape.isStone(1, 1, false, Rotation.Left))
        assertTrue(shape.isCorner(1, 1, false, Rotation.Left))
        assertFalse(shape.isFree(1, 1, false, Rotation.Left))
    }
}