package joky.event.creator.impl

import joky.core.bean.EventAction.EventAction
import joky.event.creator.EventCreatorConfig.PageConfig

class Page(override val url: String, override val title: String, val actions: Seq[EventAction]) extends PageConfig(url, title)
