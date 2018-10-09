<%@page import="java.sql.Timestamp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%--
  ~
  ~ Copyright © 2018  深圳市电子商务安全证书管理有限公司(SZCA,深圳CA) 版权所有
  ~ Copyright © 2018  SZCA. All Rights Reserved.
  ~ <p>
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~ <p>
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~ <p>
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Hello,SpringMVC & Hibernate JPA</title>
</head>
<body>
	<h1>
		<span style="color: red">SpringMVC & Hibernate JPA</span> -
		<%=new Timestamp(System.currentTimeMillis()).toString()%></h1>
	<hr>
	<ul>
		<c:forEach items="${depts}" var="tabObj">
			<li>${tabObj}</li>
		</c:forEach>
	</ul>
<p></p>
新增部门：
<hr>
<form action="deptSave.html" method="post">
<!-- 
user id:<input type="text" name="id"><br> -->
dept name:<input type="text" name="deptname"><br>
dept desc:<input type="text" name="deptdesc"><br>
dept admin:<input type="text" name="admin"><br>
<input type="submit" >
</form>
</body>
</html>