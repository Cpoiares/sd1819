<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Registo</title>
</head>
<body>
<h1>Página de Registo</h1>

<s:form action="register" method="post">
    <label for="username">Username: </label>
    <s:textfield name="username" id="username"/><br>
    <label for="password">Password: </label>
    <s:password name="password" id="password"/><br>
    <s:submit/>
</s:form>

<br>
<br>

<a href="https://www.dropbox.com/oauth2/authorize?response_type=code&client_id=juzih9ecafrb4ro&redirect_uri=http://localhost:8080/dropboxRegister">
    Registar com dropbox
</a>

<br>
<br>

<a href="<s:url action="login"/>">Login</a>

</body>
</html>
