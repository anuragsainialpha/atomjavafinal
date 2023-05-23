package com.api.apollo.atom.dto.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ApiResponse {

  private String message;
  private Object data;
  private long timestamp;
  private int statusCode;
	//Added by Inspirage Tech - eLR
	private String opsId;

  public ApiResponse() {
  }

  public ApiResponse(HttpStatus status, String message) {
    this.message = message;
    this.statusCode = status.value();
    this.timestamp = System.currentTimeMillis();
  }

  public ApiResponse(HttpStatus status, String message, Object data) {
    this.message = message;
    this.data = data;
    this.statusCode = status.value();
    this.timestamp = System.currentTimeMillis();
  }

	//Added by Inspirage Tech - eLR
	public ApiResponse(HttpStatus status, String message, Object data, String opsId) {
		this.opsId = opsId;
		this.message = message;
		this.data = data;
		this.statusCode = status.value();
		this.timestamp = System.currentTimeMillis();
	}

	private ApiResponse(ApiResponseBuilder responseBuilder) {
		this.setMessage(responseBuilder.message);
		this.setData(responseBuilder.data);
		this.setTimestamp(responseBuilder.timestamp);
		this.setStatusCode(responseBuilder.status.value());
		this.setOpsId(responseBuilder.opsId);
	}

  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof ApiResponse))
      return false;
    final ApiResponse other = (ApiResponse) o;
    if (!other.canEqual(this))
      return false;

    final Object this$message = this.getMessage();
    final Object other$message = other.getMessage();
    if (this$message == null ? other$message != null : !this$message.equals(other$message))
      return false;
    final Object this$data = this.getData();
    final Object other$data = other.getData();
    if (this$data == null ? other$data != null : !this$data.equals(other$data))
      return false;

    if (this.getTimestamp() != other.getTimestamp())
      return false;
    if (this.getStatusCode() != other.getStatusCode())
      return false;
		final Object this$opsId = this.getOpsId();
		final Object other$opsId = other.getOpsId();
		return this$opsId == null ? other$opsId == null : this$opsId.equals(other$opsId);
	}

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $message = this.getMessage();
    result = result * PRIME + ($message == null ? 43 : $message.hashCode());
    final Object $data = this.getData();
    result = result * PRIME + ($data == null ? 43 : $data.hashCode());
    final long $timestamp = this.getTimestamp();
    result = result * PRIME + (int) ($timestamp >>> 32 ^ $timestamp);
    result = result * PRIME + this.getStatusCode();
		final Object $opsId = this.getOpsId();
		result = result * PRIME + ($opsId == null ? 43 : $opsId.hashCode());
    return result;
  }

  protected boolean canEqual(Object other) {
    return other instanceof ApiResponse;
  }

  public String toString() {
    return "ApiResponse(message=" + this.getMessage() + ", data=" + this.getData()
        + ", timestamp=" + this.getTimestamp() + ", statusCode=" + this.getStatusCode() + ")";
  }

  @JsonInclude(Include.NON_NULL)
  public static class ApiResponseBuilder {
    private HttpStatus status;
    private String message;
    private Object data;
    private long timestamp = System.currentTimeMillis();
		//Added by Inspirage Tech - eLR
		private String opsId;

    public ApiResponse build() {
      return new ApiResponse(this);
    }

		public ApiResponseBuilder setMessage(String message) {
			this.message = message;
			return this;
		}

		public ApiResponseBuilder setOpsId(String opsId) {
			this.opsId = opsId;
			return this;
		}

    public ApiResponseBuilder setData(Object data) {
      this.data = data;
      return this;
    }

    public ApiResponseBuilder setStatus(HttpStatus status) {
      this.status = status;
      return this;
    }
  }
}
