package joky.sparkUdf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Auther: zhaoxin
 * @Date: 2019/1/27 10:14
 * @Description:
 */
public class FunnelAlg {

    static final Log LOG = LogFactory.getLog(FunnelAlg.class.getName());

    /**
     * 计算漏斗
     * @param funnelObject list[[step, time]]
     * @param maxStep 最大的步数 包括
     * @param convertTime 转化时间
     * @param cleanFunnelObject 是否清除传入的漏斗对象
     * @return 最大完成步数
     */
    public static int countFunnel(ArrayList<ArrayList<Object>> funnelObject,  final int maxStep, final long convertTime, boolean cleanFunnelObject) {

        // 最小步index，必须是0
        final int minStep = 0;

        if (funnelObject == null || funnelObject.isEmpty())
            return minStep;

        int maxStepCount = minStep;

        LinkedList<Long>[] stepGrade = new LinkedList[maxStep + 1];

        for (int i = minStep; i <= maxStep; i++) {
            stepGrade[i] = new LinkedList<>();
        }

        for (List<Object> stepInfo : funnelObject) {
            int step = (int) stepInfo.get(0);
            long time = (long) stepInfo.get(1);
            if (step > maxStep || step < minStep || time < 0)
                continue;
            stepGrade[step].add(time);
        }

        if (cleanFunnelObject)
            funnelObject.clear();

        if (stepGrade[0].size() == 0)
            return maxStepCount;

        LinkedList<LinkedList<Long>> successPath = new LinkedList<>();


        for (int i = minStep; i <= maxStep; i++) {
            if (i == minStep) {
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
}
