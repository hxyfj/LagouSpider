package entity;

import java.util.List;

public class CompanyResult {

	private int pageSize;

	private List<Company> result;

	private int totalCount;

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<Company> getResult() {
		return result;
	}

	public void setResult(List<Company> result) {
		this.result = result;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

}
