<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<h1>Editar album ${session.album_atual.titulo}</h1>
<p>Descrição atual: ${session.album_atual.albumDesc}</p>
<body>

<s:form action="editAlbum">
    <s:textarea label="descricao" name="descricao" cols="40" rows="10"/>
    <s:submit/>
</s:form>
</body>
</html>
