
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">

<head>
<meta charset="utf-8">
<link href="//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">
<link href="style.css" rel="stylesheet">
<title>IRProject</title>
</head>

<body class="homepage">
	
		<div class="container-1">
			<h3 class="heading">DePaul CSC575 Project</h3>
			<form action="${pageContext.request.contextPath}/search" method="post" id="searchbox">
				
				<span class="icon"><i class="fa fa-search"></i></span> 
				<input type="search" id="search" name="query" placeholder="Search..." required> 
				<input type="submit" id="submit" value="Search">
				
			</form>
			
		</div>
		<div class="footer">Index created on ${indexCreationTime} with a total size of ${indexSize}KB</div>
	
</body>
</html>
