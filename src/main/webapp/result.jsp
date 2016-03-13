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
	function validateFeedback() {
		var totalDoc = document.getElementsByName("docId").length;
		if (totalDoc == 0){
			document.getElementById("heading").innerHTML = "No documents to provide feedback on.";
			return false;
		}
		for (var i=0; i<totalDoc; i++){
			if (document.getElementsByName("relevance")[i].checked) {
				document.getElementsByName("relevanceHidden")[i].value = "1";
			} 
		}
		document.getElementByName("relevance").disabled = true;
	}
	
	function validateQuery(){
		var queryLength = document.getElementById("search").value.length;
		if (queryLength == 0 ){
			document.getElementById("resultlist").innerHTML = "";
			document.getElementById("heading").innerHTML = "Search query can't be empty.";
			return false;
		}
	}
</script>

</head>
<body>
	<div class="container-2">
		<form method="post" action="${pageContext.request.contextPath}/search">
			
			<span class="icon"><i class="fa fa-search"></i></span> 
			<input type="search" id="search" name="query" placeholder="Search..."> 
			<div><input type="submit" id="searchsubmit" value="Search" name="searchQuery" onclick="return validateQuery();"></div>
			<input type="submit" id="feedbacksubmit" name="refineQuery" value="Submit Feedback" onclick="return validateFeedback();">
			
			<div class="resultlist">
				<h3 class="heading" id="heading"><c:out value="${fn:length(results)}"/> documents found.</h3>
				
				<div id="resultlist" class="resultlist">
					<table class="tablestyle">
						<c:choose>
							<c:when test="${fn:length(results)>0}">
								<c:forEach var="document" items="${results}">
									<tr><td><a href="<c:url value="http://${document.url}" />">${document.url}</a></td></tr>
									<tr><td id="description"><c:out value="${document.snippet}" /></td></tr>
									<tr><td><input type="checkbox" id="relevance" name="relevance" value="1"/>Relevant
									<span id="score"><strong>Score :<fmt:formatNumber value="${document.score}" type="number" maxFractionDigits="3"/></strong></span></td></tr>
									<input type="hidden" id="relevanceHidden" name="relevanceHidden" value="0"/>
									<input type="hidden" name="docId" value="${document.docId}"/>
									<tr><td  class="tablepadding"></td></tr>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<span>No relevant documents found.</span>
							</c:otherwise>
						</c:choose>
					</table>
				</div>
			</div>
		</form>
	</div>
	<div style="clear:both; position:absolute; bottom:-20px;"></div>
</body>
</html>