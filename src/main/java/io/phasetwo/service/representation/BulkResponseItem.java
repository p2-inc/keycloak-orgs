package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import jakarta.validation.Valid;

public class BulkResponseItem {
    private @Valid int status = 0;
    private @Valid String error = null;
    private @Valid Object item = null;

    public BulkResponseItem status(int status) {
        this.status = status;
        return this;
    }

    @JsonProperty("status")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BulkResponseItem item(Object item) {
        this.item = item;
        return this;
    }

    @JsonProperty("item")
    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    public BulkResponseItem error(String error) {
        this.error = error;
        return this;
    }

    @JsonProperty("error")
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BulkResponseItem it = (BulkResponseItem) o;
        return Objects.equals(status, it.status) && Objects.equals(error, it.error)
                && Objects.equals(item, it.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, error, item);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BulkResponseItem {\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    error: ").append(toIndentedString(error)).append("\n");
        sb.append("    item: ").append(toIndentedString(item)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
