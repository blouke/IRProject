<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
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
	<form method="post" action="${pageContext.request.contextPath}/search" onsubmit="return validateForm();">
	
	<input type="text" id="query" name="query">
	<button type="submit" id="submit" name="searchQuery">Search</button>
	<button type="submit" id="submit" name="refineQuery">Submit Feedback</button>
	
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
</body>
</html>