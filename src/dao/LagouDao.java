package dao;

import java.util.List;

import entity.Company;
import entity.Position;

public interface LagouDao {

	public Position getPosition(int positionId);

	public void addPosition(Position position);

	public List<Integer> getPositionIds();

	public void updatePosition(int positionId, String positionDescription, String positionAddress);

	public void deletePosition(int positionId);

	public Company getCompany(int companyId);

	public void addCompany(Company company);

}
