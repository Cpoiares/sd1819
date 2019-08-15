<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Procurar musicas</title>
</head>
<body>
<h1>Página de procura de musicas</h1>

<s:form action="procurarMusicas" method="post">
    <s:textfield name="nome"/><br>
    <s:submit/>
</s:form>

<ul>
<c:forEach items="${musicas}" var="musica">
    <li>
        ${musica.titulo}
    </li>
</c:forEach>
</ul>

<br>
<br>

</body>
</html>
