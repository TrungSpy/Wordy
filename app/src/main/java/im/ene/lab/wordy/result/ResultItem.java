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
  public static final int STATE_EDITED = 2;

  public static final String KEY_CREATED_AT = "createdAt";

  public String filePath;
  @PrimaryKey public Long createdAt;
  public String result;
  public Integer state;

  public ResultItem() {
  }

  public ResultItem(String filePath, Long createdAt) {
    this.filePath = filePath;
    this.createdAt = createdAt;
    this.state = STATE_INIT;
  }

  public ResultItem(String filePath, Long createdAt, ImageKeywords source) {
    this(filePath, createdAt);
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

    return filePath.equals(item.filePath) && createdAt.equals(item.createdAt) && (result != null
        ? result.equals(item.result) : item.result == null && state.equals(item.state));
  }

  @Override public int hashCode() {
    int result1 = filePath.hashCode();
    result1 = 31 * result1 + createdAt.hashCode();
    result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
    result1 = 31 * result1 + state.hashCode();
    return result1;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
