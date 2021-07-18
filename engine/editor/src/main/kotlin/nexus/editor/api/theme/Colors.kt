package nexus.editor.api.theme

import nexus.engine.math.Vec4

object Colors {
    private val colorCache: MutableMap<String, Vec4> = HashMap()

    /**
     * This should generate a vec4 of the color hex
     */
    fun color(hexString: String): Vec4 {
        if (colorCache.containsKey(hexString)) return colorCache[hexString]!!
        if (hexString.startsWith("#"))
            return color(hexString.substringAfter("#"))
        if (hexString.length == 6) {
            val result = Vec4(Integer.valueOf(hexString.substring(0, 2), 16) / 255f,
                Integer.valueOf(hexString.substring(2, 4), 16) / 255f,
                Integer.valueOf(hexString.substring(4, 6), 16) / 255f,
                1.0f)
            colorCache[hexString] = result
            return result
        } else if (hexString.length == 8) {
            val result = Vec4(Integer.valueOf(hexString.substring(0, 2), 16) / 255f,
                Integer.valueOf(hexString.substring(2, 4), 16) / 255f,
                Integer.valueOf(hexString.substring(4, 6), 16) / 255f,
                Integer.valueOf(hexString.substring(6, 8), 16) / 255f)
            colorCache[hexString] = result
            return result
        }
        return Vec4()
    }

}