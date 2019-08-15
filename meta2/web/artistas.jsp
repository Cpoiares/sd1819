<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>

ola, pesquisar artista
<br>

<c:forEach items="${userBean.artistas}" var="artista">

    <br>

    <s:url var="test" action="detalhesArtista">
        <s:param name="nome">${artista.nome}</s:param>
    </s:url>

    <a href="${test}">${artista.nome}</a>

</c:forEach>


</body>
</html>
