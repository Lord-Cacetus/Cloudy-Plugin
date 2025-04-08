package me.kaketuz.cloudy.util.other;

public class Pair<L, R> {
    private L leftVal;
    private R rightVal;

    public Pair(L left, R right) {
        this.leftVal = left;
        this.rightVal = right;
    }

    public L getLeftVal() {
        return leftVal;
    }

    public R getRightVal() {
        return rightVal;
    }

    public void setLeftVal(L left) {
        this.leftVal = left;
    }

    public void setRightVal(R right) {
        this.rightVal = right;
    }

}
