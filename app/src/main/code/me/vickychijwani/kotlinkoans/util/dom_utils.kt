package me.vickychijwani.kotlinkoans.util

import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.StringWriter


class NodeIterator(val nodeList: NodeList) : Iterator<Node> {
    var current = -1
    override fun hasNext() = (current+1) < nodeList.length
    override fun next(): Node = nodeList.item(++current)
}

operator fun NodeList.iterator() = NodeIterator(this)

class AttributeIterator(val namedNodeMap: NamedNodeMap) : Iterator<Node> {
    var current = -1
    override fun hasNext() = (current+1) < namedNodeMap.length
    override fun next(): Node = namedNodeMap.item(++current)
}

operator fun NamedNodeMap.iterator() = AttributeIterator(this)

val Node.outerHTML: String
    get() {
        return when (nodeName) {
            "#comment", "#text" -> nodeValue
            else -> {
                val html = StringWriter()
                html.append("<$nodeName")
                for (attr in attributes) {
                    html.append(" ${attr.nodeName}=\"${attr.nodeValue}\"")
                }
                html.append(">")
                for (child in childNodes) {
                    html.append(child.outerHTML)
                }
                html.append("</$nodeName>")
                return html.toString()
            }
        }
    }
