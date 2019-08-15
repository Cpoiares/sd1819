<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Página de adicionar artista</title>
</head>
<body>
<h1>adicionarArtista.jsp</h1>
<h1>Página de adicionar um artista novo</h1>

<s:form action="adicionarArtista" method="post">
    <s:textfield name="nome"/><br>
    <s:submit/>
</s:form>

<br>
<br>

</body>
</html>
