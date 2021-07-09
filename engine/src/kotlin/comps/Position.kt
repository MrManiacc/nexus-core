package marx.engine.comps

import marx.engine.math.*

interface Position {
    object X : Comp, Position {
        override val idx: Int = 0
    }

    object Y : Comp, Position {
        override val idx: Int = 1
    }

    object Z : Comp, Position {
        override val idx: Int = 2
    }

    object W : Comp, Position {
        override val idx: Int = 3
    }

}