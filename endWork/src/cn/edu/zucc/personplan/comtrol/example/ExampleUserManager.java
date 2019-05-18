package cn.edu.zucc.personplan.comtrol.example;

import cn.edu.zucc.personplan.util.BusinessException;

import java.sql.Connection;
import java.sql.SQLException;

import cn.edu.zucc.personplan.util.DBUtil;
import cn.edu.zucc.personplan.util.DbException;
import cn.edu.zucc.personplan.itf.IUserManager;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;

public class ExampleUserManager implements IUserManager { //user类 的具体实现

	@Override
	/**
	 * 注册：
	 * 要求用户名不能重复，不能为空
	 * 两次输入的密码必须一致，密码不能为空
	 * 如果注册失败，则抛出异常
	 * @param userid
	 * @param pwd  密码
	 * @param pwd2 重复输入的密码
	 * @return
	 * @throws BaseException
	 */
	public BeanUser reg(String userid, String pwd,String pwd2) throws BaseException {
		if(userid.matches("^.*[\\s]+.*$")) {
			throw new BusinessException("用户名称不能包含空格、制表符、换页符等空白字符");
		}
		if(!userid.matches("^.{5,}$")) {
			throw new BusinessException("用户名称必须大于5个字符");
		}
		if(!pwd.equals(pwd2)) {
			throw new BusinessException("确认密码错误");
		}
		else {
			if(pwd.length() < 12) {
				throw new BusinessException("密码长度小于12个字符");
			}
			else if( pwd.matches(".*[_+|<>,.?/:;'\\[\\]{}\"]+.*")) {
				throw new BusinessException("密码包含非法字符");
			}
			else if(!pwd.matches(".*\\d+.*") || !pwd.matches(".*[a-zA-Z]+.*")) {
				throw new BusinessException("密码需同时包含数字和字母");
			}
		}
				
		Connection conn = null;
		try {
			conn=DBUtil.getConnection();
			conn.setAutoCommit(false);
			String sql = "SELECT user_id from tbl_user WHERE user_id = ?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, userid);
			java.sql.ResultSet rs = pst.executeQuery();
			if(rs.next()) {
				throw new BusinessException("该用户名已经被注册");
			}
			rs.close();
			pst.close();
			
			sql = "INSERT INTO tbl_user(user_id, user_pwd, register_time) VALUE (?,?,?)";
			pst =conn.prepareStatement(sql);
			pst.setString(1, userid);
			pst.setString(2, pwd);
			pst.setDate(3, new java.sql.Date(System.currentTimeMillis()));
			pst.execute();
			pst.close();
			conn.commit();
		}
		catch (SQLException e) {
				e.printStackTrace();
				throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.rollback();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	/**
	 * 登陆
	 * 1、如果用户不存在或者密码错误，抛出一个异常
	 * 2、如果认证成功，则返回当前用户信息
	 * @param userid
	 * @param pwd
	 * @return
	 * @throws BaseException
	 */
	@Override
	public BeanUser login(String userid, String pwd) throws BaseException {
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			String sql ="SELECT user_id, user_pwd FROM tbl_user WHERE user_id = ?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, userid);
			java.sql.ResultSet rs = pst.executeQuery();
			if(!rs.next())
				throw new BusinessException("登陆账号不存在");
			else {
				if(!rs.getString(2).equals(pwd)) {
					throw new BusinessException("密码错误");
				}
			}
			BeanUser u = new BeanUser();
			u.setUser_id(userid);
			u.setUser_pwd(pwd);
			rs.close();
			pst.close();
			return u;
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * 修改密码
	 * 如果没有成功修改，则抛出异常
	 * @param user    当前用户
	 * @param oldPwd  原密码
	 * @param newPwd  新密码
	 * @param newPwd2 重复输入的新密码
	 */
	@Override
	public void changePwd(BeanUser user, String oldPwd, String newPwd,
			String newPwd2) throws BaseException {
		Connection conn = null;
		if(!user.getUser_id().equals(oldPwd)) {
			throw new BusinessException("原密码错误");
		}
		if(!newPwd.equals(newPwd2)) {
			throw new BusinessException("确认新密码错误，请重新确认");
		}
		if(newPwd.length() < 12) {
			throw new BusinessException("新密码长度小于12个字符");
		}
		else if( newPwd.matches(".*[_+|<>,.?/:;'\\[\\]{}\"]+.*")) {
			throw new BusinessException("新密码包含非法字符");
		}
		else if(!newPwd.matches(".*\\d+.*") || !newPwd.matches(".*[a-zA-Z]+.*")) {
			throw new BusinessException("新密码需同时包含数字和字母");
		}
		try {
			conn = DBUtil.getConnection();
			conn.setAutoCommit(false);
			String sql = "UPDATE tbl_user SET user_pwd = ? WHERE user_id = ?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, newPwd);
			pst.setString(2, user.getUser_id());
		    pst.executeUpdate(); 
		    pst.close();
		    conn.commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new DbException(e);
		}
		finally{
			if(conn!=null)
				try {
					conn.rollback();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}	
	}

}
