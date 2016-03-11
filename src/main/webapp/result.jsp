<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="style.css" rel="stylesheet">
<title>Insert title here</title>
<script type="text/javascript">
	function validateForm() {
		var totalDoc = document.getElementsByName("docId").length;
		for (var i=0; i<totalDoc; i++){
			if (document.getElementsByName("relevance")[i].checked) {
				document.getElementsByName("relevanceHidden")[i].value = "1";
			} 
		}
		document.getElementByName("relevance").disabled = true;
	}
</script>

</head>
<body>
	<div class="container-2">
		<form method="post" action="${pageContext.request.contextPath}/search" onsubmit="return validateForm();">
			
			<span class="icon"><i class="fa fa-search"></i></span> 
			<input type="search" id="search" name="query" placeholder="Search..."> 
			<div><input type="submit" id="submit" value="Search" name="searchQuery"></div>
			<input type="submit" id="submit" name="refineQuery" value="Submit Feedback">
			
			<h3 class="heading"><c:out value="${fn:length(results)}"/> documents found.</h3>
			
			<c:choose>
				<c:when test="${fn:length(results)>0}">
					<c:forEach var="document" items="${results}">
					<table>
						<tr><td><h4><a href="<c:url value="http://${document.url}" />">${document.url}</a></h4></td></tr>
						<tr><td><c:out value="${document.snippet}" /></td></tr>
						<tr><td><input type="checkbox" id="relevance" name="relevance" value="1"/>Relevant</td></tr>
						<tr><td><strong>Score :<fmt:formatNumber value="${document.score}" type="number" maxFractionDigits="3"/></strong></td></tr>
						
						<input type="hidden" id="relevanceHidden" name="relevanceHidden" value="0"/>
						<input type="hidden" name="docId" value="${document.docId}"/>
						</table>
					</c:forEach>
				</c:when>
				<c:otherwise>
					<p>No relevant documents found.</p>
				</c:otherwise>
			</c:choose>
		</form>
	</div>
</body>
</html>