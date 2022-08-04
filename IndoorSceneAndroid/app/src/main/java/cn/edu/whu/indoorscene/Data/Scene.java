package cn.edu.whu.indoorscene.Data;

/**
 * Created by Mengyun Liu on 2017/1/18.
 *
 */

public class Scene {
    private String sceneLabel;
    private double score;

    public Scene (String label, double score) {
        this.sceneLabel = label;
        this.score = score;
    }

    public Scene (String label) {
        this.sceneLabel = label;
        this.score = 0.0;
    }

    public void setScene (String label) {
        this.sceneLabel = label;
    }

    public void setScore (double score) {
        this.score = score;
    }

    public String getSceneLabel() {
        return sceneLabel;
    }

    public double getScore() {
        return score;
    }

}
