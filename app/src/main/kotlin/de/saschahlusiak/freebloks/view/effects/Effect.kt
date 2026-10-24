package de.saschahlusiak.freebloks.view.effects


interface Effect {
    fun isEffected(x: Int, y: Int): Boolean
    fun render(canvas: EffectCanvas)
    fun renderShadow(canvas: EffectCanvas)
    fun isDone(): Boolean
    fun execute(elapsed: Float): Boolean
}