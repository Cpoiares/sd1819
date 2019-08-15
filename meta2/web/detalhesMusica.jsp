<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <script type="text/javascript" src="https://www.dropbox.com/static/api/2/dropins.js" id="dropboxjs"
            data-app-key="juzih9ecafrb4ro"></script>
    <title>${titulo}</title>
</head>
<body>
<p>
    Artista: ${artistaNome} <br>
    Album: ${albumNome} <br>
    Musica: ${titulo} <br>
    Id: ${musica.filePath} <br>
    Link: ${tempLink}
    <span id="url"></span>
</p>

<c:if test="${not empty tempLink}">
    <audio controls>
        <source src="${tempLink}" type="audio/mp3">
    </audio>

    <c:if test="${not empty session.access_token}">
        <p>Partilhar musica com outros utilizadores</p>
        <s:form action="addFileMembers" method="post">
            <input type="hidden" name="titulo" value="${titulo}">
            <input type="hidden" name="artistaNome" value="${artistaNome}">
            <input type="hidden" name="albumNome" value="${albumNome}">

            <c:forEach items="${userBean.dropboxUsers}" var="user">
                <input type="radio" name="account_id" value="${user.account_id}">${user.username}<br>
            </c:forEach>

            <%-- <s:textfield name="account_id"/> --%>
            <s:submit value="Partilhar"/>
        </s:form>
    </c:if>
</c:if>

<c:if test="${not empty session.access_token}">
    <div id="dropbox">
        <form action="adicionarMusicaDropbox.action" id="musicaDropboxIdForm" method="post">
            <input id="musicaDropboxId" type="hidden" name="id">
            <input type="hidden" name="titulo" value="${titulo}">
            <input type="hidden" name="artistaNome" value="${artistaNome}">
            <input type="hidden" name="albumNome" value="${albumNome}">
            <s:submit id="musicaDropboxIdButton" disabled="true" value="Adicionar ficheiro dropbox"/>
        </form>
    </div>
</c:if>

<div id="editorOptions" style="visibility: ${session.editor ? 'visible' : 'hidden'}">
    <s:form action="deleteSong">
        <s:submit value="Apagar musica"/>
    </s:form>
</div>

<script type="text/javascript">
let websocket = null;
window.onload = function () {
    connect('ws://' + window.location.host + '/ws');

    var button = Dropbox.createChooseButton(options);
    button = document.getElementById("dropbox").appendChild(button);
    button.addEventListener('click', function () {
        console.log("botaozito carregadito");
    });
};

options = {
    // https://www.dropbox.com/developers/chooser
    // Required. Called when a user selects an item in the Chooser.
    success: function (files) {
        console.dir(files[0]);
        document.getElementById("musicaDropboxId").value = files[0].id;
        document.getElementById("musicaDropboxIdButton").disabled = false;
        document.getElementById("url").innerText = files[0].name;
        // document.getElementById("musicaDropboxIdForm").submit();
    },
    linkType: "direct", // or "direct"
    extensions: ['audio']
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
    console.log(message);
    let json = JSON.parse(message.data.toString());

    if (json['type'] === "MAKE_EDITOR") {
        alert("manito és editor agora");
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
