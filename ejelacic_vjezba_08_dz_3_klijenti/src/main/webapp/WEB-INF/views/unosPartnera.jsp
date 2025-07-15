<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Unos novog partnera</title>
<style type="text/css">
.poruka {
	color: red;
}
</style>
</head>
<body>
	<h1>Dodavanje partnera</h1>
	<ul>
		<li><a
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica</a></li>
		<%
		if (request.getAttribute("poruka") != null) {
		  String poruka = (String) request.getAttribute("poruka");
		  Object oPogreska = request.getAttribute("pogreska");
		  boolean pogreska = false;
		  System.out.println(oPogreska);
		  if (oPogreska != null) {
		    pogreska = (Boolean) oPogreska;
		  }
		  if (poruka.length() > 0) {
		    String klasa = "";
		    if (pogreska) {
		  klasa = "poruka";
		    }
		%>
		<li>
			<p class="<%=klasa%>">${poruka}</p>
		</li>
		<%
		}
		}
		%>
		<li><p>Podaci o partneru:</p>
			<form method="post"
				action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/partner">
				<table>
					<tr>
						<td>ID:</td>
						<td><input name="id" type="number" required></td>
					</tr>
					<tr>
						<td>Naziv:</td>
						<td><input name="naziv" required></td>
					</tr>
					<tr>
						<td>Vrsta kuhinje:</td>
						<td><input name="vrstaKuhinje" required></td>
					</tr>
					<tr>
						<td>Adresa:</td>
						<td><input name="adresa" required></td>
					</tr>
					<tr>
						<td>Mrežna vrata:</td>
						<td><input name="mreznaVrata" type="number" required></td>
					</tr>
					<tr>
						<td>Mrežna vrata kraj:</td>
						<td><input name="mreznaVrataKraj" type="number" required></td>
					</tr>
					<tr>
						<td>GPS širina:</td>
						<td><input name="gpsSirina" type="number" step="any" required></td>
					</tr>
					<tr>
						<td>GPS dužina:</td>
						<td><input name="gpsDuzina" type="number" step="any" required></td>
					</tr>
					<tr>
						<td>Sigurnosni kod:</td>
						<td><input name="sigurnosniKod" required></td>
					</tr>
					<tr>
						<td>Admin kod:</td>
						<td><input name="adminKod" required></td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td><input type="submit" value="Dodaj partnera"></td>
					</tr>
				</table>
			</form></li>
	</ul>


</body>
</html>
