﻿1.安装MySQL数据库（5.7或以上版本），设置表名大小写不敏感
2.创建数据库bcia_szca_db，使用bcia_szca_db.sql脚本初始化此数据库
3.创建用户bcia，密码也是bcia，赋予此用户操作数据库bcia_szca_db的相关权限
3.后台管理系统需要使用HTTPS，tomcat的服务器证书bica_serv.jks，密码12345678，bcia_truststore.jks的密码是changeit
