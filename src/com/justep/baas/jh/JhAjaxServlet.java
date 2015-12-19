package com.justep.baas.jh;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSONObject;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;
import com.justep.baas.data.Util;

public class JhAjaxServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String DATASOURCE_JH = "jdbc/jh";

	// Servlet入口，通过判断action参数，进入各自对应的实现方法
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		try {
			switch (action) {
			case "sendCode":
				sendCode(request, response);
				break;
			case "saveUser":
				saveUser(request, response);
				break;
			case "queryUser":
				queryUser(request, response);
				break;
			case "saveCheck":
				saveCheck(request, response);
				break;
			case "queryCheck":
				queryCheck(request, response);
				break;
			case "queryShimianRate":
				queryShimianRate(request, response);
				break;
			case "queryJianfeiRate":
				queryJianfeiRate(request, response);
				break;
			case "queryQuestion":
				queryQuestion(request, response);
				break;
			}
		} catch (ParseException | SQLException | NamingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

	// 保存用户信息
	private static void saveUser(ServletRequest request, ServletResponse response) throws ParseException, SQLException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));
		// 获取参数
		JSONObject userData = params.getJSONObject("userData");
		Connection conn = Util.getConnection(DATASOURCE_JH);
		try {
			conn.setAutoCommit(false);
			try {
				Table userTable = Transform.jsonToTable(userData);
				Util.saveData(conn, userTable, "users");
				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} finally {
			conn.close();
		}
	}

	// 查询用户信息
	private static void queryUser(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

		List<Object> sqlParams = new ArrayList<Object>();
		List<String> filters = new ArrayList<String>();

		String ID = params.getString("id");
		if (!Util.isEmptyString(ID)) {
			filters.add("fUserId = ?");
			sqlParams.add(ID);
		}

		Table table = null;
		Connection conn = Util.getConnection(DATASOURCE_JH);
		try {
			table = Util.queryData(conn, "users", null, filters, null, sqlParams, null, null);
		} finally {
			conn.close();
		}

		// 输出返回结果
		Util.writeTableToResponse(response, table);
	}

	// 保存检测信息
	private static void saveCheck(ServletRequest request, ServletResponse response) throws ParseException, SQLException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));
		// 获取参数
		JSONObject userData = params.getJSONObject("checkData");
		Connection conn = Util.getConnection(DATASOURCE_JH);
		try {
			conn.setAutoCommit(false);
			try {
				Table userTable = Transform.jsonToTable(userData);
				Util.saveData(conn, userTable, "checks");
				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} finally {
			conn.close();
		}
	}

	// 查询检测信息
	private static void queryCheck(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

		List<Object> sqlParams = new ArrayList<Object>();
		List<String> filters = new ArrayList<String>();

		String ID = params.getString("id");
		if (!Util.isEmptyString(ID)) {
			filters.add("fID = ?");
			sqlParams.add(ID);
		}

		String belongUserId = params.getString("belongUserId");
		if (!Util.isEmptyString(belongUserId)) {
			filters.add("fBelongUserId = ?");
			sqlParams.add(belongUserId);
		}

		Table table = null;
		Connection conn = Util.getConnection(DATASOURCE_JH);
		try {
			table = Util.queryData(conn, "checks", null, filters, null, sqlParams, null, null);
		} finally {
			conn.close();
		}

		// 输出返回结果
		Util.writeTableToResponse(response, table);
	}

	// 查询失眠检测比例信息
	private static void queryShimianRate(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException {

		Connection conn = Util.getConnection(DATASOURCE_JH);

		JSONObject jsonObj = new JSONObject();
		try {
			Object sum = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='06'", null);
			Object type1 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='06' and fFeature='1'", null);
			Object type2 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='06' and fFeature='2'", null);
			Object type3 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='06' and fFeature='3'", null);
			Object type4 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='06' and fFeature='4'", null);
			Object type5 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='06' and fFeature='5'", null);
			jsonObj.put("type1", (((long) type1 * 100) / (long) sum));
			jsonObj.put("type2", (((long) type2 * 100) / (long) sum));
			jsonObj.put("type3", (((long) type3 * 100) / (long) sum));
			jsonObj.put("type4", (((long) type4 * 100) / (long) sum));
			jsonObj.put("type5", (((long) type5 * 100) / (long) sum));
		} finally {
			conn.close();
		}

		// 输出返回结果
		Util.writeJsonToResponse(response, jsonObj);
	}

	// 查询减肥检测比例信息
	private static void queryJianfeiRate(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException {

		Connection conn = Util.getConnection(DATASOURCE_JH);

		JSONObject jsonObj = new JSONObject();
		try {
			Object sum = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='01'", null);
			Object type1 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='01' and fFeature='1'", null);
			Object type2 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='01' and fFeature='2'", null);
			Object type3 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='01' and fFeature='3'", null);
			Object type4 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='01' and fFeature='4'", null);
			Object type5 = Util.getValueBySQL(conn, "select count(*) from checks where fBelongDemand='01' and fFeature='5'", null);
			jsonObj.put("type1", (((long) type1 * 100) / (long) sum));
			jsonObj.put("type2", (((long) type2 * 100) / (long) sum));
			jsonObj.put("type3", (((long) type3 * 100) / (long) sum));
			jsonObj.put("type4", (((long) type4 * 100) / (long) sum));
			jsonObj.put("type5", (((long) type5 * 100) / (long) sum));
		} finally {
			conn.close();
		}
		System.out.println(jsonObj.toJSONString());
		// 输出返回结果
		Util.writeJsonToResponse(response, jsonObj);
	}

	// 查询问题信息
	private static void queryQuestion(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

		List<Object> sqlParams = new ArrayList<Object>();
		List<String> filters = new ArrayList<String>();

		String ID = params.getString("id");
		if (!Util.isEmptyString(ID)) {
			filters.add("fUserId = ?");
			sqlParams.add(ID);
		}
		
		String belongDemandId = params.getString("belongDemandId");
		if (!Util.isEmptyString(belongDemandId)) {
			filters.add("fBelongDemand = ?");
			sqlParams.add(belongDemandId);
		}

		Table table = null;
		Connection conn = Util.getConnection(DATASOURCE_JH);
		try {
			table = Util.queryData(conn, "questions", null, filters, null, sqlParams, null, null);
		} finally {
			conn.close();
		}

		// 输出返回结果
		Util.writeTableToResponse(response, table);
	}

	// 发送手机验证码
	@SuppressWarnings("deprecation")
	private static void sendCode(ServletRequest req, ServletResponse resp) throws IOException {
		JSONObject params = (JSONObject) JSONObject.parse(req.getParameter("params"));
		String phone = params.getString("phone");
		String code = params.getString("code");

		code = URLEncoder.encode("手机验证码是：" + code + "，60秒内有效，请妥善保管【见好】");
		String url = "http://service.winic.org/sys_port/gateway/?id=liuyang2014&pwd=1q2w3e&to=" + phone + "&content=" + code + "&time=";
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = httpclient.execute(httpGet);
		String resultContent = new BasicResponseHandler().handleResponse(response);
		System.out.println(resultContent);
		resp.getWriter().write(resultContent);

	}

}
