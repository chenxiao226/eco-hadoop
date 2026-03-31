package org.hit.monitor.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Service层的返回数据封装，继承自BaseResultDTO
 * 
 * 包含成功状态，错误码、错误详情、数据存储的对象
 * 
 * 
 */
public class ResultDTO<T> extends BaseResultDTO {

	protected T module;

	// 保存详细的校验错误信息
	protected Map<String, String> checkErrorInfo = new HashMap<String, String>();

	public ResultDTO(){
	}

	public ResultDTO(T module){
		this.module = module;
	}

	public T getModule() {
		return module;
	}

	public void setModule(T module) {
		this.module = module;
	}

	public void addCheckErrorInfo(String code, String message) {
		checkErrorInfo.put(code, message);
	}

	public void addCheckErrorInfos(Map<String, String> infos) {
		checkErrorInfo.putAll(infos);
	}

	public Map<String, String> getCheckErrorInfo() {
		return checkErrorInfo;
	}
	
	public ResultDTO<T> returnError(String errorMessage){
		this.success = false;
		this.errorDetail = errorMessage;
		return this;
	}
	
	public ResultDTO<T> returnSuccess(T module){
		this.success = true;
		this.module = module;
		return this;
	}
}
