<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<h1>albums.jsp</h1>
ola listar todos os albums
<br>

<c:forEach items="${albums}" var="album">

    <br>

    <s:url var="test" action="detalhesAlbum">
        <s:param name="albumNome">${album.titulo}</s:param>
        <s:param name="artistaNome">${album.autor.nome}</s:param>
    </s:url>

    <a href="${test}">${album.autor.nome} - ${album.titulo}</a>

</c:forEach>


</body>
</html>
