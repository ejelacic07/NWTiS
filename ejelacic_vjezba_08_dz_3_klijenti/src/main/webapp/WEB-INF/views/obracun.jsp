
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Obracun"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>REST MVC - Pregled obračuna</title>
<style type="text/css">
table, th, td {
	border: 1px solid;
}

th {
	text-align: center;
	font-weight: bold;
}

.desno {
	text-align: right;
}
</style>
</head>
<body>
	<h1>REST MVC - Pregled obračuna</h1>
	<ul>
		<li><a
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">
				Početna stranica</a></li>
	</ul>
	<br />

	<h1>Odaberite datum</h1>
	<form id="filterForm" method="get"
		action="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracun">
		<label for="od">Od (datum):</label> <input type="date" id="od"
			name="od" required /> <label for="do">Do (datum):</label> <input
			type="date" id="do" name="do" required /> <label for="tip">Tip
			obračuna:</label> <select id="tip" name="tip">
			<option value="">Jela i pića</option>
			<option value="jela">Jela</option>
			<option value="pica">Pića</option>
			<option value="idPartnera" >ID partnera</option>
		</select> <input type="submit" value="Prikaži obračune" />
	</form>

	<script>
		document
				.getElementById('filterForm')
				.addEventListener(
						'submit',
						function(e) {
							e.preventDefault();
							var od = document.getElementById('od').value;
							var do_ = document.getElementById('do').value;
							var tip = document.getElementById('tip').value;

							var idPartnera = '${idPartnera}';

							var odMs = od ? new Date(od).getTime() : '';
							var doMs = do_ ? new Date(do_).getTime() : '';

							var base = '${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracun';
							if (tip === 'jela') {
								base += '/jelo';
							} else if (tip === 'pica') {
								base += '/pice';
							} else if (tip === 'idPartnera') {
								base += '/' + idPartnera;
							}

							var url = base + '?od=' + odMs + '&do=' + doMs;

							window.location.href = url;
						});
	</script>
	<br>
	<br>

	<table>
		<tr>
			<th>R.br.</th>
			<th>Partner</th>
			<th>ID</th>
			<th>Jelo</th>
			<th>Količina</th>
			<th>Cijena</th>
			<th>Vrijeme</th>

		</tr>
		<%
		int i = 0;
		List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracun");
		for (Obracun o : obracuni) {
		  i++;
		%>
		<tr>
		
			<td><%=o.rb()%></td>
			<td><%=o.partner()%></td>
			<td><%=o.id()%></td>
			<td><%=o.jelo()%></td>
			<td><%=o.kolicina()%></td>
			<td><%=o.cijena()%></td>
			<td><%=o.vrijeme()%></td>

		</tr>
		<%
		}
		%>
	</table>
</body>
</html>