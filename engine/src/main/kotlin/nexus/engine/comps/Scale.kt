package nexus.engine.comps

import nexus.engine.math.*

interface Scale {

    object Width : Comp, Scale {
        override val idx: Int = 0
    }

    object Height : Comp, Scale {
        override val idx: Int = 1
    }

    object Depth : Comp, Scale {
        override val idx: Int = 2
    }
}