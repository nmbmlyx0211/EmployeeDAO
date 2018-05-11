
package assignments;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Completed by Yinxia Li.
 */

public class EmployeeDAO extends AbstractEmployeeDAO{	
	public EmployeeDAO (Connection conn){
		super(conn);
	}

	public void addDepartment(Department dept) throws DepartmentException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query1 = "select dept_id, dept_name, manager_id from department where dept_id = ? and dept_name = ? and manager_id = ?";
		String query2 = "insert into department(dept_id, dept_name, manager_id) values(?,?,?)";
		try {
				ps = conn.prepareStatement(query1);	
				ps.setInt(1, dept.getDeptId());
				ps.setString(2, dept.getDeptName());
				ps.setInt(3, dept.getManagerId());
				rs = ps.executeQuery();
				
				if(!rs.next()){
					ps = conn.prepareStatement(query2);	
					ps.setInt(1, dept.getDeptId());
					ps.setString(2, dept.getDeptName());
					ps.setInt(3, dept.getManagerId());
					ps.executeUpdate();
					conn.commit();
				}
				else if(rs.next()){
					throw new DepartmentException("department is already exist!");
				}
				rs.close();
				ps.close();
				
		} catch (SQLException e){
			e.printStackTrace();
		}

	}
	

	public void addEmployees(List<Employee> employees, Integer deptId) throws EmployeeException{
		PreparedStatement ps = null;
		ResultSet rs = null;
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		String sql1 = "select dept_id from employee where dept_id = ?";
		String sql2 = "insert into employee(employee_id, first_name, last_name, employment_date, dept_id, annual_salary)  values(?, ?, ?,?,?,?)";
		try {
				for (Employee employee :  employees) {
					ps = conn.prepareStatement(sql1);	
					ps.setInt(1, employee.getEmployeeId());
					rs = ps.executeQuery();
					if(rs.next()){
						throw new EmployeeException("employee is exist!");
					}
				}
				rs.close();
				ps.close();
				ps = conn.prepareStatement(sql2);
				for (Employee emp :  employees) {
					String sqlDate = fmt.format(emp.getEmploymentDate());					
					ps.setInt(1, emp.getEmployeeId());
					ps.setString(2,  emp.getFirstName());
					ps.setString(3, emp.getLastName());
					ps.setDate(4, java.sql.Date.valueOf(sqlDate));
					if (deptId != null) 
						ps.setInt(5, deptId);
					else
						ps.setNull(5, Types.INTEGER);
					ps.setDouble(6, emp.getAnnualSalary());
					ps.addBatch(); 
				}
				ps.executeBatch(); 
				conn.commit();	
				ps.close();				
		}catch (SQLException e){
			e.printStackTrace();
		}
	}



	public void updateEmployee(Employee employee) throws EmployeeException{
		PreparedStatement ps = null;
		String query = "update employee set first_name = ?, last_name = ?, employment_date = ?, dept_id = ?, annual_salary = ? where employee_id = ?";
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		String sqlDate = fmt.format(employee.getEmploymentDate());
		try {
				ps = conn.prepareStatement(query);	
				ps.setString(1, employee.getFirstName());
				ps.setString(2, employee.getLastName());
				ps.setDate(3, java.sql.Date.valueOf(sqlDate));
				ps.setInt(4, employee.getDept().getDeptId());
				ps.setDouble(5, employee.getAnnualSalary());
				ps.setInt(6, employee.getEmployeeId());
				ps.executeUpdate();
				conn.commit();
				ps.close();				
		}catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	
	public void deleteEmployee(Employee employee) throws EmployeeException{
		String sql1 = "select employee_id from employee";
		String sql2 = "delete from employee where employee_id = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean found = false;
		try{			
			rs = conn.createStatement().executeQuery(sql1);
			ps = conn.prepareStatement(sql2);			
			while(rs.next()){	
				int employeeId = rs.getInt(1);
				if(employeeId == employee.getEmployeeId()){
					ps.setInt(1, employee.getEmployeeId());
					ps.executeUpdate();
					found  = true;
					break;
				}
			}
			if(found  == false){
				throw new EmployeeException("employee is invalid!");
			}		
			conn.commit();
			rs.close();
			ps.close();
		}catch (SQLException e){
			e.printStackTrace();
		}	
	}
	

	
	public List<Employee> getEmployees(Set<Integer> employeeIds){
		List<Employee> employeeList = new ArrayList<>();
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select employee_id, first_name, last_name, employment_date, annual_salary from employee";
		try{
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
			    int employeeId = rs.getInt(1);
			    if(employeeIds.contains(employeeId)){
			    	String firstName = rs.getString(2);
			    	String lastName = rs.getString(3);
			    	Date employmentDate = rs.getDate(4);
			    	double annualSalary = rs.getDouble(5);
			    	Employee temp = new Employee(employeeId, firstName, lastName, employmentDate, annualSalary);
			    	employeeList.add(temp);
			    }
			}
			rs.close();
			stmt.close();
			conn.commit();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return employeeList;	
	}
	

	public void setAverageSalaries(List<Department> departments){
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select avg(annual_salary), dept_id from employee group by dept_id";
		try{
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				for(Department dept : departments){
					if(dept.getDeptId() == rs.getInt(2)){
						dept.setAverageSalary(rs.getDouble(1));
					}
				}
			}
			rs.close();
			ps.close();
			conn.commit();
		}catch(SQLException e){
			e.printStackTrace();
		}		
	}
	

	public List<Employee> getSuperiors(Employee employee){
        List<Employee> employeeList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        int empId = employee.getEmployeeId();
        String sql = "select E.employee_id, E.first_name, E.last_name, E.employment_date, E.annual_salary, "
        		+ "D1.manager_id from employee E, employee M, department D, department D1 "
        		+ "where M.dept_id = D.dept_id and D.manager_id = E.employee_id and E.dept_id = D1.dept_id and M.employee_id =?";
        try{
            ps = conn.prepareStatement(sql);
            ps.setInt(1, empId);
            rs = ps.executeQuery();
            while (rs.next()){
                int emloyeeId = rs.getInt(1);
                String firstName = rs.getString(2);
                String lastName = rs.getString(3);
                Date employmentDate = rs.getDate(4);
                double annualSalary = rs.getDouble(5);
                int managerId = rs.getInt(6);
                Employee temp = new Employee(emloyeeId, firstName, lastName, employmentDate, annualSalary);
                employeeList.add(temp);
                if(emloyeeId != managerId){
                    ps.setInt(1, emloyeeId);
                    rs.close();
                    rs = ps.executeQuery();
                }
            }
            rs.close();
            conn.commit();
        }catch (SQLException e){
        	e.printStackTrace();
        }
        return  employeeList;
    }
	
	
}