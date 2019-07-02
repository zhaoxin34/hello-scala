package joky.spark.de.entity

import joky.spark.de.entity.helper.TimeUnit

case class Step(filter: Filter, count: Int = 1, period: Int = 0, peirodTimeUnit: TimeUnit = TimeUnit.SECOND, notDone: Boolean = false) extends BaseEntity {

    override def valid: ValidResult = {
        this match {
            case Step(f, _, _, _, _) if f == null || !f.valid.success => ValidResult(false, s"filter not valid $f")
            case Step(_, c, _, _, _) if c <=0 => ValidResult(false, "count cannot <= 0")
            case Step(_, _, p, _, _) if p < 0 => ValidResult(false, "period cannot < 0")
            case Step(_, _, _, p, _) if p == null => ValidResult(false, "peirodTimeUnit cannot be null")
        }
    }
}
