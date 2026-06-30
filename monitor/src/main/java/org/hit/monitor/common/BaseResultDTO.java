package org.hit.monitor.common;

/**
 * Service层的返回数据封装
 * 
 * 包含成功状态错误码和错误详情
 * 
 */
public class BaseResultDTO {

	protected boolean success = true;
	private String resultCode;
	protected String errorDetail;

	public String getErrorDetail() {
		return errorDetail;
	}

	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isFailed() {
		return !success;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	
	public BaseResultDTO returnError(String errorMessage){
		this.success = false;
		this.errorDetail = errorMessage;
		return this;
	}
	
	public BaseResultDTO returnSuccess(){
		this.success = true;
		return this;
	}

}
