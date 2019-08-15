<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Página de procura de artistas</title>
</head>
<body>
<h1>procurarArtistas.jsp</h1>
<h1>Página de procura de artistas</h1>

<s:form action="procurarArtistas" method="post">
    <s:textfield name="nome"/><br>
    <s:submit/>
</s:form>

<c:forEach items="${artistas}" var="artista">

    <br>

    <s:url var="test" action="detalhesArtista">
        <s:param name="nome">${artista.nome}</s:param>
    </s:url>

    <a href="${test}">${artista.nome}</a>

</c:forEach>

<br>
<br>

</body>
</html>
