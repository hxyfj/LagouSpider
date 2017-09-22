package entity;

import java.util.List;

public class PositionResult {

	private int pageSize;

	private List<Position> result;

	private int totalCount;

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<Position> getResult() {
		return result;
	}

	public void setResult(List<Position> result) {
		this.result = result;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

}
