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

<%--
  Created by IntelliJ IDEA.
  User: Jacky Luo
  Date: 2018/5/16
  Time: 10:47
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.Timestamp" %>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>BCIA Javachain - OCSP服务器</title>
</head>
<body>
<h1>欢迎使用BCIA Javachain OCSP服务器</h1>
<h5>BCIA Javachain  OCSP服务器,由<a href="http://www.szca.com"><b>深圳市电子商务安全证书管理有限公司</b>(SZCA)</a>开发。</h5>
<hr>
<p>
    Copyright © 2018  深圳市电子商务安全证书管理有限公司(SZCA,深圳CA) 版权所有

<hr>
<%=new Timestamp(System.currentTimeMillis()).toString()%>
</body>
</html>