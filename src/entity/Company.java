package entity;

public class Company {
	/** 公司id */
	private int companyId;

	/** 公司名 */
	private String companyName;

	/** 公司特点 */
	private String companyFeatures;

	/** 公司标签 */
	private String companyLabels;

	/** 发展阶段 */
	private String financeStage;

	/** 领域 */
	private String industryField;

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyFeatures() {
		return companyFeatures;
	}

	public void setCompanyFeatures(String companyFeatures) {
		this.companyFeatures = companyFeatures;
	}

	public String getCompanyLabels() {
		return companyLabels;
	}

	public void setCompanyLabels(String companyLabels) {
		this.companyLabels = companyLabels;
	}

	public String getFinanceStage() {
		return financeStage;
	}

	public void setFinanceStage(String financeStage) {
		this.financeStage = financeStage;
	}

	public String getIndustryField() {
		return industryField;
	}

	public void setIndustryField(String industryField) {
		this.industryField = industryField;
	}

	@Override
	public String toString() {
		return "companyId:" + companyId + ",companyName:" + companyName + ",companyFeatures:" + companyFeatures
				+ ",companyLabels:" + companyLabels + ",financeStage:" + financeStage + ",industryField:"
				+ industryField;
	}

}
