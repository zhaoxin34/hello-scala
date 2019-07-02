package joky.spark.de.entity.helper;

/**
 * @Auther: zhaoxin
 * @Date: 2019/4/17 17:54
 * @Description:
 */
public enum TimeUnit {
    DAY(24 * 60 * 60),
    HOUR(60 * 60),
    WEEK(7 * 24 * 60 * 60),
    MINUTE(60),
    SECOND(1);

    int senconds;

    TimeUnit(int senconds) {
        this.senconds = senconds;
    }

    public int getSenconds() {
        return senconds;
    }
}
