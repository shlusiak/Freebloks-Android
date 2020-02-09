package de.saschahlusiak.freebloks.model

enum class Mirrorable {
    /**
     * Mirroring does not make sense because the stone is symmetrical
     */
    Not,

    /**
     * Mirroring yields a shape that can also be achieved by rotating
     */
    Optional,

    /**
     * Mirroring yields a new unique shape and is important
     */
    Important
}