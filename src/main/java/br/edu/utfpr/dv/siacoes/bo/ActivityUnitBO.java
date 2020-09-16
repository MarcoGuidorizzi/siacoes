package br.edu.utfpr.dv.siacoes.bo;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.edu.utfpr.dv.siacoes.dao.ActivityUnitDAO;
import br.edu.utfpr.dv.siacoes.model.ActivityUnit;

public abstract class ActivityUnitBO {
	public abstract List<ActivityUnit> listAllDAO() throws SQLException;
	public abstract ActivityUnit findByIdDAO(int id) throws SQLException;
	public abstract int saveDAO(int idUser, ActivityUnit unit) throws SQLException;
	
	public List<ActivityUnit> listAll() throws Exception{
		try{
			return listAllDAO();
		}catch(SQLException e){
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			throw new Exception(e);
		}
	}
	
	public ActivityUnit findById(int id) throws Exception{
		try{
			return findByIdDAO(id);
		}catch(SQLException e){
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			throw new Exception(e);
		}
	}
	
	public int save(int idUser, ActivityUnit unit) throws Exception{
		if(unit.getDescription().isEmpty()){
			throw new Exception("Informe a descrição da unidade.");
		}
		if(unit.isFillAmount() && unit.getAmountDescription().isEmpty()) {
			throw new Exception("Informe a descrição da quantidade a ser informada.");
		}
		if(!unit.isFillAmount()) {
			unit.setAmountDescription("");
		}
		
		try{
			return saveDAO(idUser, unit);
		}catch(SQLException e){
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			
			throw new Exception(e);
		}
	}

}