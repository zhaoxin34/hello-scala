package joky.spark.de.entity.helper;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public enum OperatorType {
    EQ("=", false, "="),
    NE("≠", false, "!="),
    LIKE("匹配", false, "like"),
    NOT_LIKE("不匹配", false, "not like"),
    GT(">", false, ">"),
    LT("<", false, "<"),
    GTE(">=", false, ">="),
    LTE("<=", false, "<="),
    IS_NOT_NULL("非空", true, "is not null"),
    NUL("为空", true, "is null"),
    IN("包含", true, "in"),
    NOT_IN("包含", true, "not in"),
    BETWEEN("范围", true, "between");

    String displayName;
    String express;
    boolean noValue;

    OperatorType(String displayName, boolean noValue, String express) {
        this.displayName = displayName;
        this.noValue = noValue;
        this.express = express;
    }

    public String getExpress() {
        return express;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isNoValue() {
        return noValue;
    }
}
