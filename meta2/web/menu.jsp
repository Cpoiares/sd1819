<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Dropmusic!</title>
</head>
<body>
<h1>Menu Principal</h1>

<h2>Olá ${username}</h2>

<a href="artistas">Listar todos os artistas</a>
<br>
<a href="albums">Listar todos os albums</a>
<br>
<a href="#">Listar todos as musicas</a>

<br>
<br>

<a href="procurarArtistas">Procurar artistas</a>
<br>
<a href="procurarAlbums">Procurar albums</a>
<br>
<a href="procurarMusicas">Procurar músicas</a>

<br>
<br>

<a href="https://www.dropbox.com/oauth2/authorize?response_type=code&client_id=juzih9ecafrb4ro&redirect_uri=http://localhost:8080/dropboxAdd">
    Adicionar conta dropbox
</a>


<div id="editorOptions" style="visibility: ${session.editor ? 'visible' : 'hidden'}">
    <h1>yaboi és editor toma lá mais cenas</h1>
    <a href="makeEditor.action">Tornar Editor</a>
    <a href="adicionarArtista">Adicionar artista</a>
</div>

<br>

<script type="text/javascript">
    var websocket = null;
    window.onload = function () {
        connect('ws://' + window.location.host + '/ws');
    };

    function connect(host) {
        if ('WebSocket' in window) {
            websocket = new WebSocket(host);
            console.log("Hey new ws here");
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
        let json = JSON.parse(message.data.toString());
        if (json['type'] === "MAKE_EDITOR") {
            alert("manito és editor agora");
            document.getElementById("editorOptions").style.visibility = 'visible';
        }
        if (json['type'] === "album_desc"){
            alert("Olha que alteraram o album" + json['titulo']);
        }
    }

    function onError(event) {
        console.log("WebSocket error");
    }
</script>

</body>
</html>