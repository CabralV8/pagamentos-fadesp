package com.fadesp.pagamento.infrastructure.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Problem {

    private OffsetDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private Map<String, List<String>> validationErrors;

    public Problem() {
    }

    public static Builder builder() {
        return new Builder();
    }

    // getters e setters (ou use Lombok se preferir)

    public static class Builder {
        private final Problem p = new Problem();

        public Builder timestamp(OffsetDateTime t) {
            p.timestamp = t;
            return this;
        }

        public Builder status(Integer s) {
            p.status = s;
            return this;
        }

        public Builder error(String e) {
            p.error = e;
            return this;
        }

        public Builder message(String m) {
            p.message = m;
            return this;
        }

        public Builder path(String u) {
            p.path = u;
            return this;
        }

        public Builder validationErrors(Map<String, List<String>> v) {
            p.validationErrors = v;
            return this;
        }

        public Problem build() {
            return p;
        }
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, List<String>> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, List<String>> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
