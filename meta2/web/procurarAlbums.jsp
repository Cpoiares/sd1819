<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Página de procura de albums</title>
</head>
<body>
<h1>procurarAlbums.jsp</h1>
<h1>Página de procura de albums</h1>

<s:form action="procurarAlbums" method="post">
    <s:textfield name="nome"/><br>
    <s:submit/>
</s:form>

<c:forEach items="${albums}" var="album">

    <br>

    <s:url var="test" action="detalhesAlbum">
        <s:param name="albumNome">${album.titulo}</s:param>
        <s:param name="artistaNome">${album.autor.nome}</s:param>
    </s:url>

    <a href="${test}">${album.autor.nome} - ${album.titulo}</a>
</c:forEach>

<br>
<br>

</body>
</html>
