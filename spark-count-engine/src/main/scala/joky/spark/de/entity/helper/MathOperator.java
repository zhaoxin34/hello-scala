package joky.spark.de.entity.helper;

public enum MathOperator {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/");

    private String operator;

    MathOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
