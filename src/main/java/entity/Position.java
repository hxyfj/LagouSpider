package entity;

public class Position {

	/** 职位id */
	private int positionId;

	/** 职位名称 */
	private String positionName;

	/** 职位类型 */
	private String positionType;

	/** 职位诱惑 */
	private String positionAdvantage;

	/** 城市 */
	private String city;

	/** 行政区 */
	private String district;

	/** 公司id */
	private int companyId;

	/** 学历要求 */
	private String education;

	/** 工作性质 */
	private String jobNature;

	/** 工资范围 */
	private String salary;
	
	/** 工资范围解析后得到最低工资 */
	private int salaryMin;
	
	/** 工资范围解析后得到最高工资 */
	private int salaryMax;

	/** 工作经验 */
	private String workYear;

	/** 进一步抓取内容：职位描述 */
	private String positionDescription;

	/** 进一步抓取内容：工作地址 */
	private String positionAddress;

	public int getPositionId() {
		return positionId;
	}

	public void setPositionId(int positionId) {
		this.positionId = positionId;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = positionName;
	}

	public String getPositionType() {
		return positionType;
	}

	public void setPositionType(String positionType) {
		this.positionType = positionType;
	}

	public String getPositionAdvantage() {
		return positionAdvantage;
	}

	public void setPositionAdvantage(String positionAdvantage) {
		this.positionAdvantage = positionAdvantage;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public String getJobNature() {
		return jobNature;
	}

	public void setJobNature(String jobNature) {
		this.jobNature = jobNature;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

	public int getSalaryMin() {
		return salaryMin;
	}

	public void setSalaryMin(int salaryMin) {
		this.salaryMin = salaryMin;
	}

	public int getSalaryMax() {
		return salaryMax;
	}

	public void setSalaryMax(int salaryMax) {
		this.salaryMax = salaryMax;
	}

	public String getWorkYear() {
		return workYear;
	}

	public void setWorkYear(String workYear) {
		this.workYear = workYear;
	}

	public String getPositionDescription() {
		return positionDescription;
	}

	public void setPositionDescription(String positionDescription) {
		this.positionDescription = positionDescription;
	}

	public String getPositionAddress() {
		return positionAddress;
	}

	public void setPositionAddress(String positionAddress) {
		this.positionAddress = positionAddress;
	}

	@Override
	public String toString() {
		return "positionId:" + positionId + ",positionName:" + positionName + ",positionAdvantage:" + positionAdvantage
				+ ",city:" + city + ",companyId:" + companyId + ",education:" + education + ",jobNature:" + jobNature
				+ ",salary:" + salary + ",workYear:" + workYear;
	}

}
