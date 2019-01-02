package joky.producer.site

import joky.producer.PageConfig

import scala.collection.mutable.ArrayBuffer

case class PageTree private (value: String, subTree: ArrayBuffer[PageTree], pageList: ArrayBuffer[PageConfig]) {
    def addValue(path: Seq[String], page: PageConfig): Unit = {
        if (path.nonEmpty) {
            val value = path.head
            if (value == this.value) {
                addValue(path.slice(1, path.size), page)
            }
            else {
                subTree.find(v => v.value == value) match {
                    case Some(aTree: PageTree) =>
                        aTree.addValue(path.slice(1, path.size), page)
                    case None =>
                        val aTree = PageTree(value, ArrayBuffer(), ArrayBuffer())
                        aTree.addValue(path.slice(1, path.size), page)
                        subTree += aTree
                }
            }
        } else {
            pageList += page
        }
    }

    def printTree(level: Int = 0): Unit = {
        println("\t" * level + this.value + "|" + this.pageList)
        this.subTree.foreach(_tree => _tree.printTree(level + 1))
    }
}

object PageTree {
    def createPageTree(pageList: Seq[PageConfig]): PageTree = {
        val tree = PageTree("ROOT", ArrayBuffer[PageTree](), ArrayBuffer[PageConfig]())

        pageList.foreach(page => {
                val pattern = "(http[s]?://.*?)(/.*)".r
                page.url match {
                    case pattern(host: String, uri: String) => {
                        val path = if (uri.endsWith("/")) uri else uri.substring(0, uri.lastIndexOf("/"))
                        val fullPath = Seq("ROOT", host)  ++  path.split("/").filter(_.nonEmpty)
//                        val page = uri.substring(uri.lastIndexOf("/"), uri.length)
//                        println(fullPath)
                        tree.addValue(fullPath, page)
                    }
                    case _ =>
                }
            })

        tree
    }
}