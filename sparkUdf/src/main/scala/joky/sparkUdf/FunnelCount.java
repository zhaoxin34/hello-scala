package joky.sparkUdf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;

import java.util.ArrayList;

/**
 * @Auther: zhaoxin
 * @Date: 2019/1/22 16:52
 * @Description: https://blog.csdn.net/zyz_home/article/details/79889519
 */
public class FunnelCount extends AbstractGenericUDAFResolver {
    private static final int PARAMETER_COUNT = 4;

    static final Log LOG = LogFactory.getLog(FunnelCount.class.getName());

    @Override
    public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters) throws SemanticException {
        if (parameters.length != PARAMETER_COUNT) {
            throw new UDFArgumentTypeException(parameters.length - 1,
                    "Exactly 3 arguments is expected.");
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

        switch (((PrimitiveTypeInfo) parameters[2]).getPrimitiveCategory()) {
            case INT:
            case LONG:
                break;
            default:
                throw new UDFArgumentTypeException(2,
                        "Only int or long type arguments are accepted but "
                                + parameters[2].getTypeName() + " is passed.");
        }

        return new FunnelEvaluator();
    }

    public static class FunnelEvaluator extends GenericUDAFEvaluator {
        private final int STEP_MAX = 9;

        public static final int STEP_FAILED = -10000;

        private final int PARAM_STEP_NO = -20000;

        // 如果有初始步骤，初始步骤填-1
        private final int HAS_INIT_STEP = 1;
        private final int INIT_STEP_NO = -1;

        private final int MAX_BUFFER_COUNT = 10000;

        // map
        private transient PrimitiveObjectInspector stepOi;
        private transient PrimitiveObjectInspector stepTimeOi;
        private transient PrimitiveObjectInspector convertTimeOi; // 转化时间秒
        private transient PrimitiveObjectInspector hasInitStep;

//        private transient StructObjectInspector soi;
//        private transient StructField stepField;
//        private transient StructField timeField;
//        protected transient ArrayList<Object[]> partialResult;

        // map output
        private transient StandardListObjectInspector listOi;
        private transient Mode mode;

        @Override
        public ObjectInspector init(Mode mode, ObjectInspector[] parameters) throws HiveException {
            super.init(mode, parameters);

//            System.out.println(" CollectMaxUDAF.init() - Mode= " + mode.name());
//            for (int i = 0; i < parameters.length; ++i) {
//                System.out.println(" ObjectInspector[ " + i + " ] = " + parameters[0]);
//            }

//            if (parameters.length > 2) {
//                if (parameters[2] instanceof WritableConstantLongObjectInspector) {
//                    WritableConstantLongObjectInspector nvOI = (WritableConstantLongObjectInspector) parameters[2];
//                    convertTime = nvOI.getWritableConstantValue().get();
//                }
//                else if (parameters[2] instanceof WritableConstantIntObjectInspector) {
//                    WritableConstantIntObjectInspector nvOI = (WritableConstantIntObjectInspector) parameters[2];
//                    convertTime = nvOI.getWritableConstantValue().get();
//                }
//                else {
//                    throw new HiveException("Number of values must be a constant int or long. but input is" + parameters[2].getClass().getCanonicalName());
//                }
//            }
            this.mode = mode;

            // init map
            if (mode == Mode.PARTIAL1 || mode == Mode.COMPLETE) {
                stepOi = (PrimitiveObjectInspector) parameters[0];
                stepTimeOi = (PrimitiveObjectInspector) parameters[1];
                convertTimeOi = (PrimitiveObjectInspector) parameters[2];
                hasInitStep = (PrimitiveObjectInspector) parameters[3];

            } else {
                listOi = (StandardListObjectInspector) parameters[0];
            }

            // init output
            if (mode == Mode.PARTIAL1 || mode == Mode.PARTIAL2) {
                ArrayList<ObjectInspector> foi = new ArrayList<ObjectInspector>(2);
                foi.add(PrimitiveObjectInspectorFactory.javaIntObjectInspector);
                foi.add(PrimitiveObjectInspectorFactory.javaIntObjectInspector);

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
            // add convert time first
            if (fb.funnelList.isEmpty() && (input[2] instanceof IntWritable || input[2] instanceof LongWritable)) {
                ArrayList<Object> stepConvertTime = new ArrayList<>(2);
                stepConvertTime.add(PARAM_STEP_NO);
                stepConvertTime.add(PrimitiveObjectInspectorUtils.getInt(input[2], convertTimeOi));
                fb.funnelList.add(stepConvertTime);

                ArrayList<Object> isHasInitStep = new ArrayList<>(2);
                isHasInitStep.add(PARAM_STEP_NO);
                isHasInitStep.add(PrimitiveObjectInspectorUtils.getInt(input[3], hasInitStep));
                fb.funnelList.add(isHasInitStep);
            }

            // if size excceed MAX_BUFFER_COUNT, reduce to half size
            if (fb.funnelList.size() >= MAX_BUFFER_COUNT) {
                fb.funnelList = halfTheFunnelList(fb.funnelList);
                fb.funnelList.clear();
            }
            ArrayList<Object> step = new ArrayList<>(2);
            step.add(PrimitiveObjectInspectorUtils.getInt(input[0], stepOi));
            step.add(PrimitiveObjectInspectorUtils.getInt(input[1], stepTimeOi));
            fb.funnelList.add(step);
        }

        /**
         * half the lsit, exclude convert time(index,0) and has init(index, 1)
         * @param list
         * @return
         */
        private ArrayList<ArrayList<Object>> halfTheFunnelList(ArrayList<ArrayList<Object>> list) {
            ArrayList<ArrayList<Object>> newBuffer = new ArrayList<>(list.size());
            for (int i=0; i < list.size(); i++) {
                if (i <= 1 || i % 2 == 0) {
                    newBuffer.add(list.get(i));
                }
            }
            LOG.warn(mode + "|half the count. from " + list.size() + " to " + newBuffer.size());
            return newBuffer;
        }


        @Override
        public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;
//            System.out.println("===========terminatePartial============");
//            System.out.println(fb.funnelList);
//            System.out.println("============terminatePartial===========");
            return fb.funnelList;
        }

        @Override
        public void merge(AggregationBuffer agg, Object partial) throws HiveException {
            ArrayList<ArrayList<Object>> stepList = (ArrayList<ArrayList<Object>>) partial;
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;

//            if (stepList.size() + fb.funnelList.size() > MAX_BUFFER_COUNT) {
//                stepList = halfTheFunnelList(stepList);
//                fb.funnelList = halfTheFunnelList(fb.funnelList);
//            }
            fb.funnelList.addAll(stepList);
//            System.out.println(fb.funnelList);
        }

        @Override
        public void reset(AggregationBuffer agg) throws HiveException {
//            System.out.println("reset");
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;
            fb.funnelList.clear();
        }

        @Override
        public Object terminate(AggregationBuffer agg) throws HiveException {
            FunnelAggBuffer fb = (FunnelAggBuffer) agg;

//            System.out.println("===========terminate============");
//            System.out.println(fb.funnelList);
//            System.out.println("===========terminate============");

            if (fb.funnelList.size() >= 2) {
                ArrayList<Object> convertTimeStep = fb.funnelList.get(0);
                int convertTime = (int) convertTimeStep.get(1);

                ArrayList<Object> hasInitStep = fb.funnelList.get(1);

                int hasInit = (int) hasInitStep.get(1);

                boolean hasInitData = fb.funnelList.stream().map(step -> (int)step.get(0)).anyMatch(s -> s == INIT_STEP_NO);
                if (hasInit == HAS_INIT_STEP) {
                    if (!hasInitData) {
//                        System.out.println("===========terminate============11111");
                        return STEP_FAILED;
                    }
                    // 包含初始步骤，并且做了初始步骤
                    else {
                        int funnelCount = FunnelAlg.countFunnel(fb.funnelList, STEP_MAX, convertTime, true);
                        if (funnelCount == STEP_FAILED) {
//                            System.out.println("===========terminate============222222");
                            return INIT_STEP_NO;
                        }
                        else {
//                            System.out.println("===========terminate============3333333");
                            return funnelCount;
                        }
                    }
                }
                else {
                    return FunnelAlg.countFunnel(fb.funnelList, STEP_MAX, convertTime, true);
                }
            } else
                return STEP_FAILED;
        }

    }
}
