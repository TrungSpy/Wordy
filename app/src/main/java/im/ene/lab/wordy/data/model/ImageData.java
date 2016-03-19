package im.ene.lab.wordy.data.model;

import java.util.List;

import im.ene.lab.wordy.data.WordyObject;

/**
 * Created by eneim on 3/19/16.
 */
public class ImageData implements WordyObject {

    public final String image;  // image name

    public final List<ScoreData> scores;

    public ImageData(String image, List<ScoreData> scores) {
        this.image = image;
        this.scores = scores;
    }
}
