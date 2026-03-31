package org.hit.monitor.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service层的返回数据封装，继承自BaseResultDTO
 * 
 * 包含成功状态，错误码、错误详情、列表数据
 * 
 */
public class BatchResultDTO<T> extends BaseResultDTO {

	private Map<Long, String> faileds = new HashMap<Long, String>();// 失败列表
	private List<T> module;

	public List<T> getModule() {
		return module;
	}

	public void setModule(List<T> module) {
		this.module = module;
	}

	public boolean isExist() {
		return null != module && module.size() > 0;
	}

	/**
	 * 添加操作失败记录到失败列表
	 */
	public void addFailed(Long id, String errorCode) {
		setResultCode(errorCode);
		faileds.put(id, errorCode);
	}

	/**
	 * 获取操作失败记录以及对应失败的原因
	 */
	public Map<Long, String> getFaileds() {
		return faileds;
	}
	
	public BatchResultDTO<T> returnError(String errorMessage){
		this.success = false;
		this.errorDetail = errorMessage;
		return this;
	}
	
	public BatchResultDTO<T> returnSuccess(List<T> module){
		this.success = true;
		this.module = module;
		return this;
	}
}
