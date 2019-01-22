package joky.sparkUdf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.*;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @Auther: zhaoxin
 * @Date: 2019/1/22 16:52
 * @Description:
 */
public class FunnelCount extends AbstractGenericUDAFResolver {
    private static final int PARAMETER_COUNT = 2;

    static final Log LOG = LogFactory.getLog(FunnelCount.class.getName());

    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters) throws SemanticException {
        if (parameters.length != PARAMETER_COUNT) {
            throw new UDFArgumentTypeException(parameters.length - 1,
                    "Exactly 2 arguments is expected.");
        }

        // 两个参数都只支持int或long
        switch (((PrimitiveTypeInfo) parameters[0]).getPrimitiveCategory()) {
            case INT:
            case LONG:
                break;
            default:
                throw new UDFArgumentTypeException(0,
                        "Only int or long type arguments are accepted but "
                                + parameters[0].getTypeName() + " is passed.");
        }

        switch (((PrimitiveTypeInfo) parameters[1]).getPrimitiveCategory()) {
            case INT:
            case LONG:
                break;
            default:
                throw new UDFArgumentTypeException(1,
                        "Only int or long type arguments are accepted but "
                                + parameters[1].getTypeName() + " is passed.");
        }

        return new FunnelEvaluator();
    }

    public static class FunnelEvaluator extends GenericUDAFEvaluator {
        private final int STEP_MIN = 0;
        private final int STEP_MAX = 9;

        // map
        private transient PrimitiveObjectInspector stepOi;
        private transient PrimitiveObjectInspector stepTimeLongOi;

//        private transient StructObjectInspector soi;
//        private transient StructField stepField;
//        private transient StructField timeField;
//        protected transient ArrayList<Object[]> partialResult;

        // map output
        private transient StandardListObjectInspector listOi;

        @Override
        public ObjectInspector init(Mode mode, ObjectInspector[] parameters) throws HiveException {
            assert (parameters.length == PARAMETER_COUNT);
            super.init(mode, parameters);

            // init map
            if (mode == Mode.PARTIAL1 || mode == Mode.COMPLETE) {
                stepOi = (PrimitiveObjectInspector) parameters[0];
                stepTimeLongOi = (PrimitiveObjectInspector) parameters[1];

            } else {
                listOi = (StandardListObjectInspector) parameters[0];
            }

            // init output
            if (mode == Mode.PARTIAL1 || mode == Mode.PARTIAL2) {
                ArrayList<ObjectInspector> foi = new ArrayList<ObjectInspector>(2);
                foi.add(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
                foi.add(PrimitiveObjectInspectorFactory.javaLongObjectInspector);

                ArrayList<String> fname = new ArrayList<String>(2);
                fname.add("step");
                fname.add("time");

                listOi = ObjectInspectorFactory.getStandardListObjectInspector(ObjectInspectorFactory.getStandardStructObjectInspector(fname, foi));
                return listOi;
            } else {
                return PrimitiveObjectInspectorFactory.javaIntObjectInspector;
            }
        }

        public static class FunnelAggBuffer extends AbstractAggregationBuffer {
            ArrayList<ArrayList<Object>> funnelList = new ArrayList<>();
        }

        @Override
        public FunnelAggBuffer getNewAggregationBuffer() throws HiveException {
            return new FunnelAggBuffer();
        }

        @Override
        public void iterate(AggregationBuffer agg, Object[] input) throws HiveException {
            assert (input.length == PARAMETER_COUNT);
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;
            ArrayList<Object> step = new ArrayList<>(2);
            step.add(PrimitiveObjectInspectorUtils.getInt(input[0], stepOi));
            step.add(PrimitiveObjectInspectorUtils.getLong(input[1], stepTimeLongOi));
            fb.funnelList.add(step);
        }


        @Override
        public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;
            return fb.funnelList;
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial) throws HiveException {
            ArrayList<ArrayList<Object>> stepList = (ArrayList<ArrayList<Object>>) partial;
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;
            fb.funnelList.addAll(stepList);
        }

        @Override
        public void reset(AggregationBuffer agg) throws HiveException {
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;
            fb.funnelList.clear();
        }

        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            long convertTime = 1000 * 60 * 10;
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;
            LinkedList<Long>[] stepGrade = new LinkedList[STEP_MAX + 1];

            for (int i = STEP_MIN; i <= STEP_MAX; i++) {
                stepGrade[i] = new LinkedList<>();
            }

            for (ArrayList<Object> stepInfo : fb.funnelList) {
                int step = (int) stepInfo.get(0);
                long time = (long) stepInfo.get(1);
                if (step > STEP_MAX || step < STEP_MIN || time < 0)
                    continue;
                stepGrade[step].add(time);
                LOG.warn(step + ":" + time);
            }
            fb.funnelList.clear();

            if (stepGrade[0].size() == 0)
                return 0;

            LinkedList<LinkedList<Long>> successPath = new LinkedList<>();

            int maxStep = STEP_MIN;

            for (int i = STEP_MIN; i <= STEP_MAX; i++) {
                if (i == STEP_MIN) {
                    for (long time : stepGrade[i]) {
                        LinkedList<Long> path = new LinkedList<>();
                        path.add(time);
                        successPath.add(path);
                    }
                } else {
                    if (successPath.isEmpty())
                        break;
                    LOG.warn(successPath);
                    LinkedList<LinkedList<Long>> successPathNew = new LinkedList<>();
                    do {
                        LinkedList<Long> onePath = successPath.remove();
                        for (long time : stepGrade[i]) {
                            if ((time - onePath.getLast()) >= 0 && (time - onePath.getFirst()) <= convertTime) {
                                LinkedList<Long> newPath = new LinkedList<>(onePath);
                                newPath.add(time);
                                successPathNew.add(newPath);
                                maxStep = i;
                            }
                        }
                    } while (!successPath.isEmpty());
                    successPath = successPathNew;
                }
            }

            return maxStep;
        }

    }
}
