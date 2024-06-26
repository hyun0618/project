package com.itwill.cafe.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.itwill.cafe.model.OrderHistory;

import oracle.jdbc.OracleDriver;

import static com.itwill.cafe.OracleJdbc.*;
import static com.itwill.cafe.model.OrderHistory.HistoryEntity.*;

public class OrderHistoryDao {
	
	private static OrderHistoryDao instance = null;
	
	private OrderHistoryDao() {
		try {
			DriverManager.registerDriver(new OracleDriver());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static OrderHistoryDao getInstance() {
		if (instance == null) {
			instance = new OrderHistoryDao();
		}
		
		return instance;
	}
	
	private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null) rs.close();
			if (stmt != null) stmt.close();
			if (conn != null) conn.close();
		} catch (SQLException e) {
			e.printStackTrace();		
		}
	}
	
	private void closeResources(Connection conn, Statement stmt) {
		closeResources(conn, stmt, null);
	}

	// ResultSet에서 각 컬럼의 값들을 읽어서 OrderHistory 타입의 객체를 생성하고 리턴. 
	private OrderHistory makeOrderHistoryResultSet(ResultSet rs) throws SQLException {
		int id = rs.getInt(COL_ID);
		String beverage = rs.getString(COL_BEVERAGE);
		String beverageOption = rs.getString(COL_BEVERAGE_OPTION);
		int beveragePrice = Integer.parseInt(rs.getString(COL_BEVERAGE_PRICE));
		
		OrderHistory hist = new OrderHistory(id, beverage, beverageOption, beveragePrice);
		return hist;
	}
	
// read() 메서드에서 사용할 SQL문장: select * from orders by orderTime
	private static final String SQL_HISTORY_ALL = String.format(
			"select * from %s", 
			TBL_ORDERS);
			
	
	/**
	 * 데이터베이스의 ORDERS 테이블에서 모든 행을 검색해서 
	 * orderTime의 오름차순으로 정렬된 리스트를 반환.
	 * 테이블에 행이 없는 경우에는 빈 리스트를 반환한다. 
	 * @return OrderHistory를 원소로 갖는 ArrayList.
	 */
	public List<OrderHistory> read() {
		List<OrderHistory> result = new ArrayList<>();
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		OrderHistory hist = null;
		
		try {
			conn = DriverManager.getConnection(URL, USER, PASSWORD); // 데이터베이스에 접속.
			stmt = conn.prepareStatement(SQL_HISTORY_ALL); // 실행할 SQL 문장을 갖고 있는 PreparedStatement 객체를 생성.
			rs = stmt.executeQuery(); // SQL 문장을 데이터베이스로 전송해서 실행.
			
			while (rs.next()) { // 리턴 결과를 처리.
				// OrderHistory hist = makeOrderHistoryResultSet(rs);
				hist = makeOrderHistoryResultSet(rs);
				result.add(hist);
			}	
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeResources(conn, stmt, rs);
		}
		
		return result;
	}
	
// save(OrderHistory hist) 메서드에서 사용할 SQL:
// insert into orders (beverage, beverageOption) values (?, ?)
	private static final String SQL_INSERT = String.format(
			"insert into %s (%s, %s, %s) values (?, ?, ?)", 
			TBL_ORDERS, COL_BEVERAGE, COL_BEVERAGE_OPTION, COL_BEVERAGE_PRICE);
	
	public int save(OrderHistory hist) {
		int result = 0;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			stmt = conn.prepareStatement(SQL_INSERT);
			stmt.setString(1, hist.getBeverage());
			stmt.setString(2, hist.getBeverageOption());
			stmt.setInt(3, hist.getBeveragePrice());
			result = stmt.executeUpdate();
		
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeResources(conn, stmt);
		}
		
		return result;
	}
	
// 삭제: deleteAll(), deleteChoice()
	// delete from orders where id = ?
	private static final String SQL_DELETE_CHOICE = String.format(
			"delete from %s where %s = ?", 
			TBL_ORDERS, COL_ID);
	
	public int deleteChoice(int id) {
		int result = 0;
		
		Connection conn = null;
		PreparedStatement stmt = null;	
		try {
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			stmt = conn.prepareStatement(SQL_DELETE_CHOICE);
			stmt.setInt(1, id); 
			result = stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeResources(conn, stmt);
		}
		
		return result;
	}
	
	private static final String SQL_DELETE_ALL = String.format(
			"delete from %s", 
			TBL_ORDERS);
	
	public void deleteAll() {
		int result = 0;
		
		Connection conn = null;
		PreparedStatement stmt = null;	
		try {
			conn = DriverManager.getConnection(URL, USER, PASSWORD);
			stmt = conn.prepareStatement(SQL_DELETE_ALL);
			result = stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			closeResources(conn, stmt);
		}
		
		return;
	}

}
