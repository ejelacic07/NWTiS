<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Nadzorna konzola Tvrtka</title>
</head>
<body>
	<h1>Nadzorna konzola Tvrtka</h1>


	<form onsubmit="return statusDio(event);">
		<label>Status poslužitelja:</label> <select id="idStatus">
			<option value="1">Registracija</option>
			<option value="2">Rad s partnerima</option>
		</select>
		<button type="submit" =>Provjeri status</button>
	</form>
	<p id="statusPoruka"></p>


	<form onsubmit="return pauzirajDio(event);">
		<label>Pauziraj poslužitelj:</label> <select id="pauzaId">
			<option value="1">Registracija</option>
			<option value="2">Rad s partnerima</option>
		</select>
		<button type="submit">Pauziraj</button>
	</form>
	<p id="pauzaPoruka"></p>


	<form onsubmit="return startDio(event);">
		<label>Pokreni poslužitelj:</label> <select id="startId">
			<option value="1">Registracija</option>
			<option value="2">Rad s partnerima</option>
		</select>
		<button type="submit">Pokreni poslužitelj</button>
	</form>
	<p id="startPoruka"></p>


	<form onsubmit="return krajDio(event);">
		<button type="submit">Završi rad poslužitelja</button>
	</form>
	<p id="krajPoruka"></p>


	
	<form onsubmit="return posaljiPoruku(event);">
		<p>Broj obračuna: <span id="brojObracuna"></span></p>
		<p>Status: <span id="status"></span></p>
	
	
    <label>Poruka:</label>
    <input type="text" id="internaInput" maxlength="100" />
    <button type="submit">Pošalji poruku</button>
    
    <p>Interna poruka: <span id="internaPoruka">—</span></p>
<p>Interna poruka: <span id="poslanaPoruka"></span></p>
	
</form>


	<script>
var wsocket;
function connect() {
    var adresa = window.location.pathname;
    var dijelovi = adresa.split("/");
    adresa = "ws://" + window.location.hostname + ":" + window.location.port + "/" + dijelovi[1] + "/ws/tvrtka";
    if ('WebSocket' in window) {
        wsocket = new WebSocket(adresa);
    } else if ('MozWebSocket' in window) {
        wsocket = new MozWebSocket(adresa);
    } else {
        alert('WebSocket nije podržan od web preglednika.');
        return;
    }
    wsocket.onmessage = onMessage;
}


window.addEventListener("load", function() {
    connect();

    document.getElementById("internaInput").disabled = true;
    document.querySelector("form[onsubmit='return posaljiPoruku(event);'] button").disabled = true;
}, false);

function onMessage(evt) {
    var poruka = evt.data;
    var [status, brojObracuna, internaPoruka] = poruka.split(";");

    document.getElementById("status").innerText = status;
    document.getElementById("brojObracuna").innerText = brojObracuna;
    document.getElementById("internaPoruka").innerText = internaPoruka && internaPoruka.trim() !== "" ? internaPoruka : "—";

    document.getElementById("internaInput").disabled = false;
    document.querySelector("form[onsubmit='return posaljiPoruku(event);'] button").disabled = false;
}

window.addEventListener("load", connect, false);

function statusDio(event) {
    event.preventDefault();
    let id = document.getElementById("idStatus").value;
    let contextPath = '<%=request.getContextPath()%>';
   let url = contextPath + "/mvc/tvrtka/admin/status/" + id;
    fetch(url, { method: 'GET' })
        .then(resp => {
            document.getElementById("statusPoruka").innerText = resp.ok ? "Status upitan (status 200)" : "Greška: " + resp.status;
        })
        .catch(() => {
            document.getElementById("statusPoruka").innerText = "Neuspješan zahtjev.";
        });
    return false;
}


function pauzirajDio(event) {
    event.preventDefault();
    let id = document.getElementById("pauzaId").value;
    let contextPath = '<%=request.getContextPath()%>';
    let url = contextPath + "/mvc/tvrtka/admin/pauza/" + id;
    fetch(url, { method: 'GET' })
        .then(resp => {
            document.getElementById("pauzaPoruka").innerText = resp.ok ? "Pauza postavljena (status 200)" : "Greška: " + resp.status;
        })
        .catch(() => {
            document.getElementById("pauzaPoruka").innerText = "Neuspješan zahtjev.";
        });
    return false;
}


function startDio(event) {
    event.preventDefault();
    let id = document.getElementById("startId").value;
    let contextPath = '<%=request.getContextPath()%>';
    let url = contextPath + "/mvc/tvrtka/admin/start/" + id;
    fetch(url, { method: 'GET' })
        .then(resp => {
            document.getElementById("startPoruka").innerText = resp.ok ? "Pokretanje uspješno (status 200)" : "Greška: " + resp.status;
        })
        .catch(() => {
            document.getElementById("startPoruka").innerText = "Neuspješan zahtjev.";
        });
    return false;
}


function krajDio(event) {
    event.preventDefault();
    let contextPath = '<%=request.getContextPath()%>';
    let url = contextPath + "/mvc/tvrtka/admin/kraj/"
    fetch(url, { method: 'GET' })
        .then(resp => {
            document.getElementById("krajPoruka").innerText = resp.ok ? "Kraj poslužitelja postavljen (status 200)" : "Greška: " + resp.status;
        })
        .catch(() => {
            document.getElementById("krajPoruka").innerText = "Neuspješan zahtjev.";
        });
    return false;
}

function posaljiPoruku(event) {
    event.preventDefault();
    var input = document.getElementById("internaInput").value; 
    var status = document.getElementById("status").innerText; 
    var brojObracuna = document.getElementById("brojObracuna").innerText;

    var poruka = status + ";" + brojObracuna + ";" + input;

    if (wsocket && wsocket.readyState === WebSocket.OPEN) {
        wsocket.send(poruka);
        document.getElementById("internaInput").value = "";
   
        document.getElementById("poslanaPoruka").innerText = poruka;
    }

    return false;
}
</script>
</body>
</html>