package joky.event.creator.impl

import joky.event.creator.EventCreatorConfig.PageConfig

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

case class PageTree[T <: PageConfig] private(father: Option[PageTree[T]], value: String, subTree: ArrayBuffer[PageTree[T]], pageList: ArrayBuffer[T]) {

    /**
      * 获得根节点
      * @return
      */
    def getRoot: PageTree[T] = {
        var node = this
        while(!node.isRoot && node.father.nonEmpty) node = node.father.get
        node
    }

    /**
      * 当前是否是根节点
      * @return
      */
    def isRoot: Boolean =  {
        PageTree.ROOT_VALUE == value
    }

    /**
      * 添加页
      * @param path  路径
      * @param page 页面
      */
    private def addValue(path: Seq[String], page: T): Unit = {
        if (path.nonEmpty) {
            val value = path.head
            if (value == this.value) {
                addValue(path.slice(1, path.size), page)
            }
            else {
                subTree.find(v => v.value == value) match {
                    case Some(aTree: PageTree[T]) =>
                        aTree.addValue(path.slice(1, path.size), page)
                    case None =>
                        val aTree = PageTree[T](Some(this), value, ArrayBuffer(), ArrayBuffer())
                        aTree.addValue(path.slice(1, path.size), page)
                        subTree += aTree
                }
            }
        } else {
            pageList += page
        }
    }

    @tailrec
    private def _printTree(pageTreeList: Seq[PageTree[T]], level: Int =0): Unit = {
        if  (pageTreeList.isEmpty)
            return
        pageTreeList.foreach(pageTree => {
            println("\t" * level + pageTree.value)
        })
        _printTree(pageTreeList.flatMap(_.subTree), level + 1)
    }

    def printTree(level: Int = 0): Unit = {
        println("\t" * level + this.value )
        _printTree(this.subTree)
    }

    override def toString: String =  {
        value
    }
}

object PageTree {
    private val ROOT_VALUE = "ROOT"

    def createPageTree[T <: PageConfig] (pageList: Seq[T]): PageTree[T] = {
        val tree = PageTree[T](None, ROOT_VALUE, ArrayBuffer[PageTree[T]](), ArrayBuffer[T]())

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
