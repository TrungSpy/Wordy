package im.ene.lab.wordy.data.response;

import java.util.List;

import im.ene.lab.wordy.data.model.Classifier;

/**
 * Created by eneim on 3/19/16.
 */
public class ClassifiersResponse {

    public final List<Classifier> classifiers;

    public ClassifiersResponse(List<Classifier> classifiers) {
        this.classifiers = classifiers;
    }
}
