package com.dt.sparkUdf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: zhaoxin
 * @Date: 2019/1/27 10:14
 * @Description:
 */
public class FunnelAlg {

    static final Log LOG = LogFactory.getLog(FunnelAlg.class.getName());
    // 最小步index，必须是0
    static final int STEP_MIN= 0;
    static final int maxCount = 1000000;
    /**
     * 计算漏斗
     * @param funnelObject list[[step, time]]
     * @param maxStep 最大的步数 包括
     * @param convertTime 转化时间
     * @param cleanFunnelObject 是否清除传入的漏斗对象
     * @return 最大完成步数
     */
    public static int countFunnel2(ArrayList<ArrayList<Object>> funnelObject,  final int maxStep, final long convertTime, boolean cleanFunnelObject) {

        if (funnelObject == null || funnelObject.isEmpty())
            return STEP_MIN;

        int maxStepCount = STEP_MIN;

        LinkedList<Long>[] stepGrade = new LinkedList[maxStep + 1];

        for (int i = STEP_MIN; i <= maxStep; i++) {
            stepGrade[i] = new LinkedList<>();
        }

        for (List<Object> stepInfo : funnelObject) {
            Integer step = (Integer) stepInfo.get(0);
            Long time = (Long) stepInfo.get(1);
            if (step > maxStep || step < STEP_MIN || time < 0)
                continue;
            stepGrade[step].add(time);
        }

        if (cleanFunnelObject)
            funnelObject.clear();

        if (stepGrade[0].size() == 0)
            return maxStepCount;

        LinkedList<LinkedList<Long>> successPath = new LinkedList<>();


        for (int i = STEP_MIN; i <= maxStep; i++) {
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
                        if ((time - onePath.getLast()) > 0 && (time - onePath.getFirst()) <= convertTime) {
                            LinkedList<Long> newPath = new LinkedList<>(onePath);
                            newPath.add(time);
                            successPathNew.add(newPath);
                            maxStepCount = i;
                            if (maxStepCount == maxStep)
                                return maxStepCount;
                        }
                    }
                } while (!successPath.isEmpty());
                successPath = successPathNew;
            }
        }

        return maxStepCount;
    }

    // unuse
    public static enum Direction {
        UP,
        DOWN,
        RIGHT
    }

    /**
     * 计算漏斗
     * @param funnelObject list[[step, time]]
     * @param maxStep 最大的步数 包括
     * @param convertTime 转化时间
     * @param cleanFunnelObject 是否清除传入的漏斗对象
     * @return 最大完成步数
     */
    public static int countFunnel(ArrayList<ArrayList<Object>> funnelObject,  final int maxStep, final long convertTime, boolean cleanFunnelObject) {


        if (funnelObject == null || funnelObject.isEmpty())
            return FunnelCount.FunnelEvaluator.STEP_FAILED;

        int maxStepCount = STEP_MIN;

        // 把漏斗按照每一步排序
        Map<Integer, LinkedList<Long>> stepGrade = funnelObject.stream()
                .filter(f -> (Integer)f.get(0) >= STEP_MIN && (Integer)f.get(0) <= maxStep)
                .collect(Collectors.groupingBy(
                        f -> (Integer) f.get(0),
                        Collectors.mapping(f -> (Long) f.get(1), Collectors.toCollection(LinkedList::new))));
//                                Collectors.collectingAndThen(Collectors.toList(), l -> l.stream().sorted().collect(Collectors.toCollection(LinkedList::new))))
//                ));

        if (cleanFunnelObject)
            funnelObject.clear();

        if (stepGrade == null || stepGrade.isEmpty() || stepGrade.get(0) == null || stepGrade.get(0).size() == 0)
            return FunnelCount.FunnelEvaluator.STEP_FAILED;

        int maxGrade = 0;
        stepGrade.put(0, stepGrade.get(0).stream().sorted().collect(Collectors.toCollection(LinkedList::new)));
        // 剪枝
        for (int i = STEP_MIN + 1; i <= maxStep; i++) {
            Long prevMinTime = stepGrade.get(i-1).get(0);
            LinkedList<Long> currentStep = stepGrade.get(i);

            //
            if (currentStep == null || currentStep.isEmpty()) {
                break;
            }

            currentStep = currentStep.stream().filter(time -> time > prevMinTime).sorted().collect(Collectors.toCollection(LinkedList::new));

            if (currentStep.size() <= 0)
                break;

            stepGrade.put(i, currentStep);
            maxGrade = i;
        }

        int counts = 0;
        outer: do {
            long beginTime = stepGrade.get(STEP_MIN).removeFirst();
            long lastTime;
            for (int i = STEP_MIN + 1; i <= maxGrade && i < stepGrade.size(); i++) {
                counts ++;

                if (i == STEP_MIN + 1)
                    lastTime = beginTime;
                else
                    lastTime = stepGrade.get(i - 1).getFirst();

                LinkedList<Long> currentStep = stepGrade.get(i);
                if (currentStep == null || currentStep.isEmpty())
                    break outer;

                while (!currentStep.isEmpty() && currentStep.getFirst() <= lastTime) {
                    currentStep.removeFirst();
                }

                if (currentStep.isEmpty())
                    break outer;

                // success
                long duration = currentStep.getFirst() - beginTime;
                if (duration > 0 && duration < convertTime) {
                    maxStepCount = Math.max(i, maxStepCount);
                    if (maxStepCount >= maxGrade)
                        break outer;
                }
            }
        } while(!stepGrade.get(STEP_MIN).isEmpty());

//        System.out.println("counts = " + counts);

        return maxStepCount;
    }
}
