package org.nutz.walnut.ext.entity.score;

public class ScoreIt {

    private String name;

    private long score;

    public ScoreIt() {}

    public ScoreIt(String target, long score) {
        this.setName(target);
        this.setScore(score);
    }

    public String getName() {
        return name;
    }

    public void setName(String target) {
        this.name = target;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String toString() {
        return String.format("%s:%d", name, score);
    }

}
