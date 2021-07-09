package marx.engine.math

interface Comp {
    val idx: Int
    object None : Comp {
        override val idx: Int = -1
    }
}
