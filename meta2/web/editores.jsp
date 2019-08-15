<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Tornar editor</title>
</head>
<body>
<h1>Tornar editor</h1>

<s:form >
    <s:select list="%{userBean.nonEditors}" headerKey="-1"
              headerValue="User" name="username"
              value = "username"
    />

    <s:submit value="submit" name="submit" action="makeEditor"/>

</s:form>
</body>
</html>
