package joky.event.creator.impl

/**
  * @Auther: zhaoxin
  * @Date: 2019/4/10 14:49
  * @Description:
  */
case class Site private(name: String, siteId: String, pageList: Seq[Page]) {
    val pageTree: PageTree[Page] = PageTree.createPageTree(pageList)
}
