package im.ene.lab.wordy.result;

import com.ibm.watson.developer_cloud.alchemy.v1.model.ImageKeyword;
import com.ibm.watson.developer_cloud.alchemy.v1.model.ImageKeywords;
import im.ene.lab.wordy.utils.Utils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.List;

/**
 * Created by eneim on 3/19/16.
 */
public class ResultItem extends RealmObject {

  public static final int STATE_INIT = -1000;
  public static final int STATE_UNKNOWN = 0;
  public static final int STATE_SUCCESS = 1;
  public static final int STATE_FAILED = -1;

  public static final String KEY_CREATED_AT = "createdAt";

  public String fileUri;
  @PrimaryKey public Long createdAt;
  public String result;
  public Integer state;

  public ResultItem() {

  }

  public ResultItem(String fileUri, Long createdAt) {
    this.fileUri = fileUri;
    this.createdAt = createdAt;
    this.state = STATE_INIT;
  }

  public ResultItem(String fileUri, Long createdAt, ImageKeywords source) {
    this(fileUri, createdAt);
    // be careful: shallow copy only
    if (source != null) {
      setImageKeywords(source.getImageKeywords());
    }
  }

  public void setState(Integer state) {
    this.state = state;
  }

  public void setImageKeywords(List<ImageKeyword> imageKeywords) {
    if (!Utils.isEmpty(imageKeywords)) {
      setResult(imageKeywords.get(0).getText());
    }
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ResultItem item = (ResultItem) o;

    if (!fileUri.equals(item.fileUri)) return false;
    if (!createdAt.equals(item.createdAt)) return false;
    if (result != null ? !result.equals(item.result) : item.result != null) return false;
    return state.equals(item.state);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + fileUri.hashCode();
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
