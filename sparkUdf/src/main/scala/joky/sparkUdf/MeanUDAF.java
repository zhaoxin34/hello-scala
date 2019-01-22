package joky.sparkUdf;

import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;

import java.math.BigDecimal;


/**
 * @Auther: zhaoxin
 * @Date: 2019/1/21 17:23
 * @Description:
 */
public class MeanUDAF  extends AbstractGenericUDAFResolver {
    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters) throws SemanticException {
        return new Evaluator();
    }

    public static class Evaluator extends GenericUDAFEvaluator {
        private transient ObjectInspector inputOI;
        private transient ObjectInspector outputOI;

        @Override
        public ObjectInspector init(Mode m, ObjectInspector[] parameters) throws HiveException {
            super.init(m, parameters);
            inputOI = parameters[0];
            outputOI = ObjectInspectorUtils.getStandardObjectInspector(inputOI, ObjectInspectorUtils.ObjectInspectorCopyOption.JAVA);
            return outputOI;
        }

        public static class MeanAggBuffer extends AbstractAggregationBuffer {
            double value = 0;
            int count = 0;
        }

        @Override
        public MeanAggBuffer getNewAggregationBuffer() throws HiveException {
            return new MeanAggBuffer();
        }

        @Override
        public void iterate(AggregationBuffer agg, Object[] input) throws HiveException {
            if (input != null && input.length>0)
                merge(agg, input[0]);
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial) throws HiveException {
            MeanAggBuffer buffer = (MeanAggBuffer) agg;
            buffer.count += 1;
            double value = 0;
            if (partial != null) {
                if (partial instanceof IntWritable) {
                    value = ((IntWritable)partial).get();
                } else if(partial instanceof DoubleWritable) {
                    value = ((DoubleWritable)partial).get();
                }
            }

            buffer.value += value;
        }

        @Override
        public void reset(AggregationBuffer agg) throws HiveException {
            MeanAggBuffer buffer = (MeanAggBuffer) agg;
            buffer.value = 0;
            buffer.count = 0;
        }

        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            MeanAggBuffer buffer = (MeanAggBuffer) agg;
            return HiveDecimal.create(new BigDecimal(buffer.value / buffer.count));
        }

        @Override
        public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            return terminate(agg);
        }
    }
}
