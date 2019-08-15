<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Login</title>
</head>
<body style="color: #f442aa">
<h1>Página de login</h1>

<s:form action="login" method="post">
    <label for="username">Username: </label>
    <s:textfield name="username" id="username"/><br>
    <label for="password">Password: </label>
    <s:password name="password" id="password"/><br>
    <s:submit/>
</s:form>

<br>

<a href="https://www.dropbox.com/oauth2/authorize?response_type=code&client_id=juzih9ecafrb4ro&redirect_uri=http://localhost:8080/dropboxLogin">
    Login com dropbox
</a>

<br>
<br>

<a href="<s:url action="register"  />">Registo</a>

</body>
</html>
