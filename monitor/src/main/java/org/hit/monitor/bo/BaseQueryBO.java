package org.hit.monitor.bo;

import java.io.Serializable;

/**
 * 公用分页查询条件BO
 */
public class BaseQueryBO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1475981471261733960L;
	private int pageSize = 20;// 每页数量
	private int pageNo = 1;// 页码
	private int record;// 结果总记录数

	public int getPageSize() {
		if (pageSize < 0) {
			return 20;// 默认一页20个
		}
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageNo() {
		if (pageNo < 1) {
			return 1;
		}
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageIndex() {
		return (getPageNo() - 1) * getPageSize();
	}

	public int getRecord() {
		if (record < 0) {
			return 0;
		}
		return record;
	}

	/**
	 * 设置总记录数
	 */
	public void setRecord(int record) {
		this.record = record;
	}

	/**
	 * 获取总页数
	 */
	public int getTotalPages() {
		// 如果pageSize=0 json转该对象在次会出现除0bug,所以=0时直接返回无穷大
		if (getPageSize() == 0) return Integer.MAX_VALUE;
		if (record % getPageSize() == 0) {
			return record / getPageSize();
		} else {
			return record / getPageSize() + 1;
		}
	}
}
