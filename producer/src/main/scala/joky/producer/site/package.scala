package joky.producer

import joky.core.bean.EventAction.EventAction

import scala.collection.mutable.ArrayBuffer

/**
  * @Auther: zhaoxin
  * @Date: 2019/1/2 12:00
  * @Description:
  */
package object site {
    class Page(override val url: String, override val title: String, val actions: Seq[EventAction]) extends PageConfig(url, title)

    case class Site private (name: String, siteId: String, pageList: Seq[Page]) {
        val pageTree: PageTree[Page] = PageTree.createPageTree(pageList)
//        pageTree.printTree()
    }


//    val laiyifenPages = Map(
//        "index" -> Page("http://m.lyf.edu.laiyifen.com/index.html", "来伊份移动商城", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//
//        "list1" -> Page("http://m.lyf.edu.laiyifen.com/list1.html", "来伊份移动商城 - 列表页", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//        "list2" -> Page("http://m.lyf.edu.laiyifen.com/list2.html", "来伊份移动商城 - 列表页", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//        "list3" -> Page("http://m.lyf.edu.laiyifen.com/list3.html", "来伊份移动商城 - 列表页", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//        "list4" -> Page("http://m.lyf.edu.laiyifen.com/list4.html", "来伊份移动商城 - 列表页", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//        "list5" -> Page("http://m.lyf.edu.laiyifen.com/list5.html", "来伊份移动商城 - 列表页", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//
//        "order1" -> Page("http://m.lyf.edu.laiyifen.com/order1.html", "来伊份移动商城 - 订单页", Seq(EventAction.pageview, EventAction.login, EventAction.logout, EventAction.order)),
//        "order2" -> Page("http://m.lyf.edu.laiyifen.com/order2.html", "来伊份移动商城 - 订单页", Seq(EventAction.pageview, EventAction.login, EventAction.logout, EventAction.order)),
//        "order3" -> Page("http://m.lyf.edu.laiyifen.com/order3.html", "来伊份移动商城 - 订单页", Seq(EventAction.pageview, EventAction.login, EventAction.logout, EventAction.order)),
//        "order4" -> Page("http://m.lyf.edu.laiyifen.com/order4.html", "来伊份移动商城 - 订单页", Seq(EventAction.pageview, EventAction.login, EventAction.logout, EventAction.order)),
//        "order5" -> Page("http://m.lyf.edu.laiyifen.com/order5.html", "来伊份移动商城 - 订单页", Seq(EventAction.pageview, EventAction.login, EventAction.logout, EventAction.order)),
//
//        "pay" -> Page("http://m.lyf.edu.laiyifen.com/pay.html", "来伊份移动商城 - 支付页", Seq(EventAction.pageview, EventAction.login, EventAction.logout, EventAction.pay))
//    )
//
//    laiyifenPages("index").addSubPages(Seq(
//        laiyifenPages("list1").addSubPages(Seq(
//            laiyifenPages("order1"),
//            laiyifenPages("order2"),
//            laiyifenPages("order3"),
//            laiyifenPages("order4"),
//            laiyifenPages("index")
//        )),
//        laiyifenPages("list2").addSubPages(Seq(
//            laiyifenPages("order1"),
//            laiyifenPages("order2"),
//            laiyifenPages("order5"),
//            laiyifenPages("order4"),
//            laiyifenPages("index")
//        )),
//        laiyifenPages("list3").addSubPages(Seq(
//            laiyifenPages("order4"),
//            laiyifenPages("order2"),
//            laiyifenPages("order3"),
//            laiyifenPages("index")
//        )),
//        laiyifenPages("list4").addSubPages(Seq(
//            laiyifenPages("order1"),
//            laiyifenPages("order2"),
//            laiyifenPages("order3"),
//            laiyifenPages("order5"),
//            laiyifenPages("index")
//        )),
//        laiyifenPages("list5").addSubPages(Seq(
//            laiyifenPages("order1"),
//            laiyifenPages("order2"),
//            laiyifenPages("order3"),
//            laiyifenPages("order4"),
//            laiyifenPages("order5"),
//            laiyifenPages("index")
//        ))
//    ))
//
//    laiyifenPages("order1").addSubPages(Seq(laiyifenPages("pay"), laiyifenPages("index")))
//    laiyifenPages("order2").addSubPages(Seq(laiyifenPages("pay"), laiyifenPages("index")))
//    laiyifenPages("order3").addSubPages(Seq(laiyifenPages("pay"), laiyifenPages("index")))
//    laiyifenPages("order4").addSubPages(Seq(laiyifenPages("pay"), laiyifenPages("index")))
//    laiyifenPages("order5").addSubPages(Seq(laiyifenPages("pay"), laiyifenPages("index")))
//
//    laiyifenPages("pay").addSubPage(laiyifenPages("index"))
//
//
//    val laiyifenSite = Site("来一份模拟", "Jevf4ghaKT091r5E", laiyifenPages.values.toList)
//
//
//    val hualongPages = Map(
//        "index" -> Page("https://user.datatist.com/uc/accounts/index.html", "Datatist | 上海画龙信息科技", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//
//        "portal" -> Page("https://www.datatist.com/", "Datatist | 上海画龙信息科技", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//
//        "usercenter" -> Page("http://test.user.datatist.cn/uc/accounts/index.html", "Datatist | 上海画龙信息科技", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//
//        "project" -> Page("http://test.user.datatist.cn/uc/accounts/project.htm", "Datatist | 上海画龙信息科技 | 用户中心", Seq(EventAction.pageview, EventAction.login, EventAction.logout)),
//
//        "eventAnalysis" -> Page("https://analyzer.datatist.com/analyzer/index.html#/event-analysis", "Datatist | 上海画龙信息科技 | 事件分析", Seq(EventAction.pageview, EventAction.login, EventAction.logout))
//    )
//
//    hualongPages("index").addSubPage(hualongPages("portal").addSubPage(hualongPages("usercenter")))
//    hualongPages("usercenter").addSubPage(hualongPages("project").addSubPage(hualongPages("eventAnalysis")))
//
//    val hualongSite = Site("画龙模拟", "M2DHIYqc5jit8QJM", hualongPages.values.toList)
}
