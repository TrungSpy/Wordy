package im.ene.lab.wordy.data.model;

import im.ene.lab.wordy.data.WordyObject;

/**
 * Created by eneim on 3/19/16.
 * <p/>
 * Sample object
 * <p/>
 * {
 *      "name": "tiger",
 *      "classifier_id": "tiger_1234",
 *      "created": "2015-03-25T12:00:00",
 *      "owner": "ss324f-23sf65-sdf321-dfsgh87j"
 * }
 */
public class Classifier implements WordyObject {

    public final String classifier_id;

    public final String name;

    public final String created;

    public final String owner;

    public Classifier(String classifier_id, String name, String created, String owner) {
        this.classifier_id = classifier_id;
        this.name = name;
        this.created = created;
        this.owner = owner;
    }
}
