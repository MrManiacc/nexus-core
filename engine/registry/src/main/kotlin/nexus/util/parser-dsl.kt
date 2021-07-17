package nexus.util

import org.w3c.dom.Node
import org.w3c.dom.NodeList


fun NodeList.forEach(block: Node.() -> Unit) {
    for (i in 0 until this.length) {
        val node = this.item(i)
        node.apply(block)
    }
}


fun NodeList.findChildrenWithName(name: String): List<Node> {
    val result = ArrayList<Node>()
    this.forEach {
        if (this.nodeName == name) {
            result.add(this)
        }
    }
    return result
}

fun NodeList.findChildWithName(name: String): Node? = findChildrenWithName(name).getOrNull(0)