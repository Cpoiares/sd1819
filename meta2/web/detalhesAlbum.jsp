<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${album.titulo}</title>
</head>
<p>
    Artista: ${album.autor.nome}
</p>
<p>
    Album: ${album.titulo}
</p>

<c:choose>
    <c:when test="${session.album_atual.albumDesc} == null">
        <p>Descrição do album: Album não tem descrição.</p>
    </c:when>
    <c:otherwise>
        <p id="album_desc">Descrição do album: ${session.album_atual.albumDesc}</p>
    </c:otherwise>
</c:choose>

<p>
    Genero musical: ${album.generoMusical}
</p>

<h3>Músicas</h3>

<ul>
    <c:forEach items="${album.musicas}" var="musica">
        <s:url var="test" action="detalhesMusica">
            <s:param name="albumNome">${album.titulo}</s:param>
            <s:param name="artistaNome">${album.autor.nome}</s:param>
            <s:param name="titulo">${musica.titulo}</s:param>
        </s:url>

        <li>
            <a href="${test}">${musica.titulo}</a>
        </li>
    </c:forEach>
</ul>

<p id="average">
    Pontuação média: ${media}
</p>

<h3>Criticas</h3>
<c:forEach items="${session.album_atual.criticas}" var="critica">
    <p>Justificação: ${critica.justificacao}</p>
    <p>Pontuação: ${critica.pontuacao}</p>
</c:forEach>


<h1>-----------------------------------------------</h1>

<a href="criticar.action">Adicionar Critica</a>

<div id="editorOptions" style="visibility: ${session.editor ? 'visible' : 'hidden'}">
    <h1>yaboi és editor toma lá mais cenas</h1>

    <a href="editAlbum.action">Editar Descrição textual</a>

    <br>
    adicionar musica
    <s:form action="adicionarMusica" method="post">
        <s:textfield name="titulo" placeholder="titulo"/><br>
        <input type="hidden" name="albumNome" value="${album.titulo}">
        <s:submit/>
    </s:form>

    <s:if test="%{album.musicas.isEmpty()}">
        <p>
            Apagar album ${album.titulo}
        </p>
        <s:form action="deleteAlbum">
            <s:submit/>
        </s:form>
    </s:if>

</div>

<script type="text/javascript">
var websocket = null;
window.onload = function () {
    connect('ws://' + window.location.host + '/ws');
};

function connect(host) {
    if ('WebSocket' in window) {
        websocket = new WebSocket(host);
        console.log("Album details websocket");
    }
    else if ('MozWebSocket' in window)
        websocket = new MozWebSocket(host);
    else {
        console.log("Get a real browser my bro");
        return;
    }

    websocket.onopen = onOpen;
    websocket.onclose = onClose;
    websocket.onmessage = onMessage;
    websocket.onerror = onError;
}

function onOpen(event) {
    websocket.send("${session.username}");
}

function onClose(event) {
    console.log("bye - WebSocket close");
}

function onMessage(message) {
    let artista = '${album.autor.nome}';
    let album = '${album.titulo}';
    console.log(message);
    let json = JSON.parse(message.data.toString());
    if(json['type'] === 'average') {
        if (json['artista'] === artista && json['album'] === album) {
            alert("Nova Pontuacao média para o album.");
            document.getElementById("average").innerHTML = "Pontuação média: " + json['pontuacao'];
        }
    }
    if (json['type'] === "MAKE_EDITOR") {
        alert("manito és editor agora");
        document.getElementById("editorOptions").style.visibility = 'visible';
    }

    if (json['type'] === "album_desc") {
        alert("olha que mudaram-te o album " + json['titulo']);
    }

}

function onError(event) {
    console.log("WebSocket error");
}
</script>
</body>
</html>
