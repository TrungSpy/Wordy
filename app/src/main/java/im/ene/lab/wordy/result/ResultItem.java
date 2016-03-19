package im.ene.lab.wordy.result;

import com.ibm.watson.developer_cloud.alchemy.v1.model.ImageKeywords;
import im.ene.lab.wordy.utils.Utils;

/**
 * Created by eneim on 3/19/16.
 */
public class ResultItem extends ImageKeywords {

  public final String fileUri;

  public final Long createdAt;

  public ResultItem(String fileUri, Long createdAt) {
    this.fileUri = fileUri;
    this.createdAt = createdAt;
  }

  public ResultItem(String fileUri, Long createdAt, ImageKeywords source) {
    this(fileUri, createdAt);
    // be careful: shallow copy only
    if (source != null) {
      setImageKeywords(source.getImageKeywords());
      setUrl(source.getUrl());
      setTotalTransactions(source.getTotalTransactions());
    }
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ResultItem item = (ResultItem) o;

    return fileUri.equals(item.fileUri) && (Utils.isEmpty(getImageKeywords()) ? Utils.isEmpty(
        item.getImageKeywords()) : getImageKeywords().equals(item.getImageKeywords())) && (
        getTotalTransactions() == null ? item.getTotalTransactions() == null
            : getTotalTransactions().equals(item.getTotalTransactions())) && (getUrl() == null ?
        item.getUrl()
            == null : getUrl().equals(item.getUrl()));
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + fileUri.hashCode();
    return result;
  }
}
