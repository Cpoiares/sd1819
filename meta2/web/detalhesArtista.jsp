<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${artista.nome}</title>
</head>
<body>

<p>
    Artista: ${artista.nome}
</p>


<h3>Albums</h3>
    <ul>
    <c:forEach items="${artista.albuns}" var="album">
        <s:url var="url" action="detalhesAlbum">
            <s:param name="albumNome">${album.titulo}</s:param>
            <s:param name="artistaNome">${artista.nome}</s:param>
        </s:url>
        <li>
            <a href="${url}">${album.titulo}</a>
        </li>
    </c:forEach>
    </ul>

    <br>

    <div id="editorOptions" style="visibility: ${session.editor ? 'visible' : 'hidden'}">
        <p>
            Adicionar album
        </p>
        <s:form action="adicionarAlbum" method="post">
            <s:textfield name="titulo" placeholder="titulo"/><br>
            <s:textfield name="genero" placeholder="genero musical"/><br>
            <input type="hidden" name="artista" value="${artista.nome}">
            <s:submit/>
        </s:form>

        <s:if test="%{artista.albuns.isEmpty()}">
            <p>
                Apagar artista ${artista.nome}
            </p>
            <s:form action="deleteArtist">
                <s:submit/>
            </s:form>
        </s:if>
    </div>

</body>
</html>
