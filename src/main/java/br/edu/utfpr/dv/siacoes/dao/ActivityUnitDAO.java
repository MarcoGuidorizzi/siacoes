package br.edu.utfpr.dv.siacoes.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.edu.utfpr.dv.siacoes.log.UpdateEvent;
import br.edu.utfpr.dv.siacoes.model.ActivityUnit;

public class ActivityUnitDAO extends Template {

	@Override
	void functions(){
		super.functions();
	}
	
	@Override
	public List<ActivityUnit> listAll() throws SQLException{
        // Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
        String query = "SELECT * FROM activityunit ORDER BY description";

		try (Connection conn = ConnectionDAO.getInstance().getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);) {
			List<ActivityUnit> list = new ArrayList<ActivityUnit>();

			while (rs.next()) {
				list.add(this.loadObject(rs));
			}

			return list;

		}
	}
	
	@Override
	public ActivityUnit findById(int id) throws SQLException{
        // Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		String query = "SELECT * FROM activityunit WHERE idActivityUnit=?";

		try (Connection conn = ConnectionDAO.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);) {
			stmt.setInt(1, id);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return this.loadObject(rs);
				} else {
					return null;
				}
			}
		}
	}
	
	@Override
	public int save(int idUser, ActivityUnit unit) throws SQLException {
		boolean insert = (unit.getIdActivityUnit() == 0);

	    // Colocando INSERT e UPDATE em funções separadas para melhor entendimento do código
		if (insert) {
			return insertActivityUnit(idUser, unit);
		} else {
			return updateActivityUnit(idUser, unit);
		}
	}

	// Criando método privado para INSERT
	private int insertActivityUnit(int idUser, ActivityUnit unit) throws SQLException {
		String sql = "INSERT INTO activityunit(description, fillAmount, amountDescription) VALUES(?, ?, ?)";

        // Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		try (Connection conn = ConnectionDAO.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, unit.getDescription());
			stmt.setInt(2, (unit.isFillAmount() ? 1 : 0));
			stmt.setString(3, unit.getAmountDescription());

            // Usanddo executeUpdate para garantir o INSERT
            // o método executeUpdate é destinado para INSERT, UPDATE e DELETE
            int rows = stmt.executeUpdate();
            if(rows == 0){
                throw new SQLException("Falha ao inserir no banco.");
            }

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					unit.setIdActivityUnit(rs.getInt(1));
				}

				new UpdateEvent(conn).registerInsert(idUser, unit);

				return unit.getIdActivityUnit();
			}
		}
	}

	// Criando método privado para UPDATE
	private int updateActivityUnit(int idUser, ActivityUnit unit) throws SQLException {
		String sql = "UPDATE activityunit SET description=?, fillAmount=?, amountDescription=? WHERE idActivityUnit=?";

        // Utilizando o método de tryResourceClose para minimizar o código e garantir o fechamento das conexões
        // Fonte: https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		try (Connection conn = ConnectionDAO.getInstance().getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);) {
			stmt.setString(1, unit.getDescription());
			stmt.setInt(2, (unit.isFillAmount() ? 1 : 0));
			stmt.setString(3, unit.getAmountDescription());
			stmt.setInt(4, unit.getIdActivityUnit());

            // Usando executeUpdate para garantir o UPDATE
            // o método executeUpdate é destinado para INSERT, UPDATE e DELETE
            int rows = stmt.executeUpdate();
            if(rows == 0){
                throw new SQLException("Falha ao fazer update no banco, id não encontrado.");
            }

			new UpdateEvent(conn).registerUpdate(idUser, unit);

			return unit.getIdActivityUnit();
		}
	}
	
	private ActivityUnit loadObject(ResultSet rs) throws SQLException{
		ActivityUnit unit = new ActivityUnit();
		
		unit.setIdActivityUnit(rs.getInt("idActivityUnit"));
		unit.setDescription(rs.getString("Description"));
		unit.setFillAmount(rs.getInt("fillAmount") == 1);
		unit.setAmountDescription(rs.getString("amountDescription"));
		
		return unit;
	}

}