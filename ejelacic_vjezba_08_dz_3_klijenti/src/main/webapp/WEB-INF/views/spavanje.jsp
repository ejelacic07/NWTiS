<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Spavanje – Nadzorna konzola Tvrtka</title>
</head>
<body>
    <h1>Unos vremena za spavanje</h1>
    <br />

    <div>
        <form method="get" action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spavanje">
            <label for="vrijeme">Unesi broj sekundi za spavanje:</label>
            <input type="number" name="vrijeme" id="vrijeme" min="1" step="1" required>
            <button type="submit">Pošalji zahtjev za spavanje</button>
        </form>
    </div>

    <div>
     
</body>
</html>