package im.ene.lab.wordy.data.response;

import java.util.List;

import im.ene.lab.wordy.data.model.ImageData;

/**
 * Created by eneim on 3/19/16.
 */
public class ClassifyResponse {

    public final List<ImageData> images;

    public ClassifyResponse(List<ImageData> images) {
        this.images = images;
    }
}
