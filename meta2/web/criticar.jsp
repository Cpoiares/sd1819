<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Criticar album</title>
</head>
<body>
<h1A>Criticar album ${session.album_atual.titulo}</h1A>
<s:form action="criticar">
    <s:textarea label="justificacao" name="justificacao" maxlength="300" cols="40" rows="10"/>
    <s:textfield type="number" name="pontuacao" min="0" max="100" step="1"/>
<s:submit/>
</s:form>

</script>
</body>
</html>
