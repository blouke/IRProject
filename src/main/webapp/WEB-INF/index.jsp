
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">

<head>
<meta charset="utf-8">
<title>IRProject</title>
</head>

<body>

	<h1>Search</h1>
	<form action="${pageContext.request.contextPath}/search" method="post" id="searchbox">
		
			<input type="text" id="query" name="query">
			<button type="submit" id="submit">Search</button>
	</form>

</body>
</html>
