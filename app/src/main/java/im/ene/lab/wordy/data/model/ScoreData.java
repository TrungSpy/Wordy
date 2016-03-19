package im.ene.lab.wordy.data.model;

/**
 * Created by eneim on 3/19/16.
 */
public class ScoreData {

    public final String classifier_id;

    public final String name;

    public final Float score;

    public ScoreData(String classifier_id, String name, float score) {
        this.classifier_id = classifier_id;
        this.name = name;
        this.score = score;
    }
}
