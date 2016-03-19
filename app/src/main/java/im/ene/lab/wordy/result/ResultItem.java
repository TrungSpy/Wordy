package im.ene.lab.wordy.result;

import com.ibm.watson.developer_cloud.alchemy.v1.model.ImageKeywords;

/**
 * Created by eneim on 3/19/16.
 */
public class ResultItem extends ImageKeywords {

    public final String fileUri;

    public ResultItem(String fileUri) {
        this.fileUri = fileUri;
    }
}
