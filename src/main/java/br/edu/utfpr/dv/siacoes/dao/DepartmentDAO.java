package br.edu.utfpr.dv.siacoes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.edu.utfpr.dv.siacoes.log.UpdateEvent;
import br.edu.utfpr.dv.siacoes.model.Department;

public class DepartmentDAO {

	public List<Department> listAll(boolean onlyActive) throws SQLException{

		// Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		String query = "SELECT department.*, campus.name AS campusName " +
					"FROM department "+
					"INNER JOIN campus ON campus.idCampus=department.idCampus " + 
					(onlyActive ? " WHERE department.active=1" : "") + " ORDER BY department.name";
		try (Connection conn = ConnectionDAO.getInstance().getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);) {
			List<Department> list = new ArrayList<Department>();

			while(rs.next()){
					list.add(this.loadObject(rs));
			}
			
			return list;
		}
	}

	public Department findById(int id) throws SQLException{

		// Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		String query = "SELECT department.*, campus.name AS campusName " +
				"FROM department "+
                "INNER JOIN campus ON campus.idCampus=department.idCampus " +
				"WHERE idDepartment = ?";
		try (Connection conn = ConnectionDAO.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);) {
				stmt.setInt(1, id);
			
				try(ResultSet rs = stmt.executeQuery();){
					if(rs.next()){
						return this.loadObject(rs);
					}else{
						return null;
					}
				}

		}
	}
	
	public List<Department> listByCampus(int idCampus, boolean onlyActive) throws SQLException{
		
		// Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		String query = "SELECT department.*, campus.name AS campusName " +
					"FROM department "+
					"INNER JOIN campus ON campus.idCampus=department.idCampus " +
					"WHERE department.idCampus = ? " + (onlyActive ? " AND department.active=1" : "") + " ORDER BY department.name";
		try (Connection conn = ConnectionDAO.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);) {
				// Passando para Statement o idCampus
				stmt.setInt(1, idCampus);

			try(ResultSet rs = stmt.executeQuery();){
				List<Department> list = new ArrayList<Department>();
				while(rs.next()){
					list.add(this.loadObject(rs));
				}
				return list;
			}
		}
	}
	
	public int save(int idUser, Department department) throws SQLException{
		boolean insert = (department.getIdDepartment() == 0);

		// Colocando INSERT e UPDATE em funções separadas para melhor entendimento do código
		if (insert) {
			return insertDepartment(idUser, department);
		} else {
			return updateDepartment(idUser, department);
		}
	}

	// Criando método privado para UPDATE
	private int insertDepartment(int idUser, Department department) throws SQLException{
		String sql = "INSERT INTO department(idCampus, name, logo, active, site, fullName, initials) VALUES(?, ?, ?, ?, ?, ?, ?)";
		
		// Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		try(Connection conn = ConnectionDAO.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);){
			stmt.setInt(1, department.getCampus().getIdCampus());
			stmt.setString(2, department.getName());
			if(department.getLogo() == null){
				stmt.setNull(3, Types.BINARY);
			}else{
				stmt.setBytes(3, department.getLogo());	
			}
			stmt.setInt(4, department.isActive() ? 1 : 0);
			stmt.setString(5, department.getSite());
			stmt.setString(6, department.getFullName());
			stmt.setString(7, department.getInitials());

			// Usanddo executeUpdate para garantir o UPDATE
			// o Método executeUpdate é destinado para INSERT, UPDATE e DELETE
			int rows = stmt.executeUpdate();
			if(rows == 0){
				throw new SQLException("Falha ao fazer update no banco, id não encontrado.");
			}

			try(ResultSet rs = stmt.getGeneratedKeys()) {
				if(rs.next()){
				    department.setIdDepartment(rs.getInt(1));
				}

				new UpdateEvent(conn).registerInsert(idUser, department);

				return department.getIdDepartment();
			}
		}
	}
	
	// Criando método privado para UPDATE
	private int updateDepartment(int idUser, Department department) throws SQLException{
		String sql = "UPDATE department SET idCampus=?, name=?, logo=?, active=?, site=?, fullName=?, initials=? WHERE idDepartment=?";

		// Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
		// Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		try(Connection conn = ConnectionDAO.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);){
			stmt.setInt(1, department.getCampus().getIdCampus());
			stmt.setString(2, department.getName());
			if(department.getLogo() == null){
				stmt.setNull(3, Types.BINARY);
			}else{
				stmt.setBytes(3, department.getLogo());	
			}
			stmt.setInt(4, department.isActive() ? 1 : 0);
			stmt.setString(5, department.getSite());
			stmt.setString(6, department.getFullName());
			stmt.setString(7, department.getInitials());
			stmt.setInt(8, department.getIdDepartment());
			

			// Usanddo executeUpdate para garantir o UPDATE
			// o Método executeUpdate é destinado para INSERT, UPDATE e DELETE
			int rows = stmt.executeUpdate();
			if(rows == 0){
				throw new SQLException("Falha ao fazer update no banco, id não encontrado.");
			}
			
			new UpdateEvent(conn).registerUpdate(idUser, department);
			
			return department.getIdDepartment();
		}
	}
	
	private Department loadObject(ResultSet rs) throws SQLException{
		Department department = new Department();
		
		department.setIdDepartment(rs.getInt("idDepartment"));
		department.getCampus().setIdCampus(rs.getInt("idCampus"));
		department.setName(rs.getString("name"));
		department.setFullName(rs.getString("fullName"));
		department.setLogo(rs.getBytes("logo"));
		department.setActive(rs.getInt("active") == 1);
		department.setSite(rs.getString("site"));
		department.getCampus().setName(rs.getString("campusName"));
		department.setInitials(rs.getString("initials"));
		
		return department;
	}
	
}