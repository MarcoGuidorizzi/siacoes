package br.edu.utfpr.dv.siacoes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.edu.utfpr.dv.siacoes.model.BugReport;
import br.edu.utfpr.dv.siacoes.model.BugReport.BugStatus;
import br.edu.utfpr.dv.siacoes.model.Module;
import br.edu.utfpr.dv.siacoes.model.User;

public class BugReportDAO {

    public BugReport findById(int id) throws SQLException{

		// Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		String query = "SELECT bugreport.*, \"user\".name " + 
				"FROM bugreport INNER JOIN \"user\" ON \"user\".idUser=bugreport.idUser " +
				"WHERE idBugReport = ?";
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
	
	public List<BugReport> listAll() throws SQLException{

        // Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		String query = "SELECT bugreport.*, \"user\".name " +
					"FROM bugreport INNER JOIN \"user\" ON \"user\".idUser=bugreport.idUser " +
					"ORDER BY status, reportdate";
		try (Connection conn = ConnectionDAO.getInstance().getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);) {
			List<BugReport> list = new ArrayList<BugReport>();

			while(rs.next()){
                list.add(this.loadObject(rs));
			}
			
			return list;
		}
	}
	
	public int save(BugReport bug) throws SQLException{
		boolean insert = (bug.getIdBugReport() == 0);

        // Colocando INSERT e UPDATE em funções separadas para melhor entendimento do código
		if (insert) {
			return insertBugReport(bug);
		} else {
			return updateBugReport(bug);
		}
	}

	// Criando método privado para INSERT
    private int insertBugReport(BugReport bug) throws SQLException{
        String sql = "INSERT INTO bugreport(idUser, module, title, description, reportDate, type, status, statusDate, statusDescription) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try(Connection conn = ConnectionDAO.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);){
			stmt.setInt(1, bug.getUser().getIdUser());
			stmt.setInt(2, bug.getModule().getValue());
			stmt.setString(3, bug.getTitle());
			stmt.setString(4, bug.getDescription());
			stmt.setDate(5, new java.sql.Date(bug.getReportDate().getTime()));
			stmt.setInt(6, bug.getType().getValue());
			stmt.setInt(7, bug.getStatus().getValue());
			if(bug.getStatus() == BugStatus.REPORTED){
				stmt.setNull(8, Types.DATE);
			}else{
				stmt.setDate(8, new java.sql.Date(bug.getStatusDate().getTime()));
			}
			stmt.setString(9, bug.getStatusDescription());
			
			// Usando executeUpdate para garantir o INSERT
			// o método executeUpdate é destinado para INSERT, UPDATE e DELETE
			int rows = stmt.executeUpdate();
			if(rows == 0){
                throw new SQLException("Falha ao inserir no banco.");
			}

			try(ResultSet rs = stmt.getGeneratedKeys()){
                if(rs.next()){
                    bug.setIdBugReport(rs.getInt(1));
                }

                return bug.getIdBugReport();
            }
		}
	}
        
	// Criando método privado para UPDATE
    private int updateBugReport(BugReport bug) throws SQLException{
        String sql = "UPDATE bugreport SET idUser=?, module=?, title=?, description=?, reportDate=?, type=?, status=?, statusDate=?, statusDescription=? WHERE idBugReport=?";
		
		try(Connection conn = ConnectionDAO.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);){
			stmt.setInt(1, bug.getUser().getIdUser());
			stmt.setInt(2, bug.getModule().getValue());
			stmt.setString(3, bug.getTitle());
			stmt.setString(4, bug.getDescription());
			stmt.setDate(5, new java.sql.Date(bug.getReportDate().getTime()));
			stmt.setInt(6, bug.getType().getValue());
			stmt.setInt(7, bug.getStatus().getValue());
			if(bug.getStatus() == BugStatus.REPORTED){
				stmt.setNull(8, Types.DATE);
			}else{
				stmt.setDate(8, new java.sql.Date(bug.getStatusDate().getTime()));
			}
			stmt.setString(9, bug.getStatusDescription());
			stmt.setInt(10, bug.getIdBugReport());
			
			// Usando executeUpdate para garantir o UPDATE
			// o método executeUpdate é destinado para INSERT, UPDATE e DELETE
			int rows = stmt.executeUpdate();
			if(rows == 0){
				throw new SQLException("Falha ao fazer update no banco, id não encontrado.");
			}

			return bug.getIdBugReport();
		}
	}

	
	private BugReport loadObject(ResultSet rs) throws SQLException{
		BugReport bug = new BugReport();
		
		bug.setIdBugReport(rs.getInt("idBugReport"));
		bug.setUser(new User());
		bug.getUser().setIdUser(rs.getInt("idUser"));
		bug.getUser().setName(rs.getString("name"));
		bug.setModule(Module.SystemModule.valueOf(rs.getInt("module")));
		bug.setTitle(rs.getString("title"));
		bug.setDescription(rs.getString("description"));
		bug.setReportDate(rs.getDate("reportDate"));
		bug.setType(BugReport.BugType.valueOf(rs.getInt("type")));
		bug.setStatus(BugReport.BugStatus.valueOf(rs.getInt("status")));
		bug.setStatusDate(rs.getDate("statusDate"));
		bug.setStatusDescription(rs.getString("statusDescription"));
		
		return bug;
	}

}