package org.nutz.walnut.ext.entity.score;

public class ScoreIt {

    private String name;

    private long score;

    public ScoreIt() {}

    public ScoreIt(String target, long time) {
        this.setName(target);
        this.setScore(time);
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

    public void setScore(long time) {
        this.score = time;
    }

    public String toString() {
        return String.format("%s:%d", name, score);
    }

}
