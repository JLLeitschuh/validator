<%--
  #%L
  BioPAX Validator Web Application
  %%
  Copyright (C) 2008 - 2013 University of Toronto (baderlab.org) and Memorial Sloan-Kettering Cancer Center (cbio.mskcc.org)
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as 
  published by the Free Software Foundation, either version 3 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.
  
  You should have received a copy of the GNU General Lesser Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-3.0.html>.
  #L%
  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>

<!DOCTYPE html>
<html>
<head>
   <meta charset="utf-8" />
	<meta name="author" content="BioPAX" />
	<meta name="description" content="BioPAX Validator" />
	<meta name="keywords" content="BioPAX, Validation, Validator, Rule, OWL, Exchange" />
	<link rel="stylesheet" type="text/css" href="styles/style.css" media="screen" />
	<link rel="shortcut icon" href="images/favicon.ico" />
	<script type="text/javascript" src="scripts/rel.js"></script>
	<script type="text/javascript"> <!-- this function is here (not in a separate .js file) for off-line use -->
		function switchit(list) {
			var listElementStyle = document.getElementById(list).style;
			if (listElementStyle.display == "none") {
				listElementStyle.display = "block";
			} else {
				listElementStyle.display = "none";
			}
		}
	</script>
	<title>BioPAX Validator: other</title>
</head>
<body>

<div id="wrap">
  <jsp:include page="/templates/header.jsp"/>
  <div id="content">
    <div id="left">

<h2>Additional Configuration</h2>
<h3>For XrefRule, XrefSynonymDbRule rules</h3>
<p>
Special groups of synonyms (or typos) for known databases 
can be configured via a Spring XML file, and later - auto-completed
by standard names from MIRIAM resource and PSI-MI ("database citation"). 
</p>
<p>
Click <a href="javascript:switchit('syngroups')">here to show/hide</a> 
the list:</p>

 <ol id="syngroups">
  <c:forEach var="g" items="${extraDbSynonyms}">
	<li>group:<ol>
	  <c:forEach var="db" items="${g}">
	    <li>${db}</li>
      </c:forEach>
    </ol></li>
    <br />
  </c:forEach>
 </ol>
<p/>
<hr />

  </div>
    <div id="right">
      <jsp:include page="/templates/menu.jsp"/>
      <jsp:include page="/templates/box.jsp"/>
    </div>
    <div id="clear"></div>
  </div>

  <jsp:include page="/templates/footer.jsp"/>
</div>

</body>
</html>