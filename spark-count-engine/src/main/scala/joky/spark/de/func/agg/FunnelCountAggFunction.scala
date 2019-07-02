//package joky.spark.de.func.agg
//
//import org.apache.spark.sql.Row
//import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
//import org.apache.spark.sql.types._
//
//object FunnelCountAggFunction {
//    val STEP_COLUMN_NAME = "STEP"
//    val TIME_COLUMN_NAME = "EVENT_TIME"
//}
//
//class FunnelCountAggFunction extends UserDefinedAggregateFunction {
//    import FunnelCountAggFunction._
//
//    val MIN_STEP = 0
//    val MAX_STEP = 10
//
//    // This is the input fields for your aggregate function.
//    override def inputSchema: org.apache.spark.sql.types.StructType =
//        StructType(
//            StructField(STEP_COLUMN_NAME, IntegerType) ::
//            StructField(TIME_COLUMN_NAME, LongType) :: Nil)
//
//    // This is the internal fields you keep for computing your aggregate.
//    override def bufferSchema: StructType = StructType(
//        MIN_STEP to MAX_STEP map(step => {
//            StructField(STEP_COLUMN_NAME + step, ArrayType(LongType), nullable = false)
//        })
//    )
//
//
//    //        StructField(STEP_COLUMN_NAME, LongType) ::
//    // This is the output type of your aggregatation function.
//    override def dataType: DataType = IntegerType()
//
//    override def deterministic: Boolean = true
//
//    // This is the initial value for your buffer schema.
//    override def initialize(buffer: MutableAggregationBuffer): Unit = {
//        buffer(0) = 0L
//        buffer(1) = 1.0
//    }
//
//    // This is how to update your buffer schema given an input.
//    override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
//        buffer(0) = buffer.getAs[Long](0) + 1
//        buffer(1) = buffer.getAs[Double](1) * input.getAs[Double](0)
//    }
//
//    // This is how to merge two objects with the bufferSchema type.
//    override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
//        buffer1(0) = buffer1.getAs[Long](0) + buffer2.getAs[Long](0)
//        buffer1(1) = buffer1.getAs[Double](1) * buffer2.getAs[Double](1)
//    }
//
//    // This is where you output the final value, given the final value of your bufferSchema.
//    override def evaluate(buffer: Row): Any = {
//        math.pow(buffer.getDouble(1), 1.toDouble / buffer.getLong(0))
//    }
//}
//
