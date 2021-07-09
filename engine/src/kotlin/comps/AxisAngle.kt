package marx.engine.comps

import marx.engine.math.*

interface AxisAngle {

    object AxisX : Comp, AxisAngle {
        override val idx: Int = 0
    }

    object AxisY : Comp, AxisAngle {
        override val idx: Int = 1
    }

    object AxisZ : Comp, AxisAngle {
        override val idx: Int = 2
    }

    object Angle : Comp, AxisAngle {
        override val idx: Int = 3
    }
}