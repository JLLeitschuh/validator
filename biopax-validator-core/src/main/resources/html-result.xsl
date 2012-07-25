<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemalocation="http://www.w3.org/1999/XSL/Transform 
    http://www.w3.org/2005/02/schema-for-xslt20.xsd"
  version="2.0">
 
	<xsl:output method="html" omit-xml-declaration="yes" indent="no"/>
	    
	<xsl:template match="/">
        <html>
			<head>
				<meta name="author" content="biopax.org" />
				<meta name="description" content="BioPAX Validator Response as HTML" />
				<meta name="keywords" content="BioPAX, Validation, Validator, Results" />
				<script type="text/javascript">
		function switchit(lid) {
			var listElementStyle = document.getElementById(lid).style;
			if (listElementStyle.display == "none") {
				listElementStyle.display = "block";
			} else {
				listElementStyle.display = "none";
			}
		}</script>
				<title>Validation Results</title>
			</head>
			
            <body>
            	<ul>
            		<xsl:apply-templates/>
				</ul>
            </body>
            
        </html>
               
    </xsl:template>
    
    <xsl:template match="validationResult">
    	<li style="text-decoration: underline" title="Click to see more detail">
			<a href="javascript:switchit('{generate-id()}')">Resource: <xsl:value-of select="@description"/>; <xsl:value-of select="@summary"/></a>
		</li>		
		<ul>
		  	<li><xsl:for-each select="comment" >
					<xsl:value-of select="."/>&#xa0; <!-- same as &nbsp; -->
  				</xsl:for-each></li>
			<li>auto-fix: <xsl:value-of select="@fix"/>;&#xa0;normalize: <xsl:value-of select="@normalize"/></li>
			<li>errors/warnings: <xsl:value-of select="@totalProblemsFound"/>&#xa0;- not fixed: <xsl:value-of select="@notFixedProblems"/>;&#xa0; 
	  			<xsl:choose>
	  				<xsl:when test="@maxErrors>0">errors limit: <xsl:value-of select="@maxErrors"/> (not fixed)</xsl:when>
	  				<xsl:otherwise>errors not fixed: <xsl:value-of select="@notFixedErrors"/></xsl:otherwise>
	  			</xsl:choose>
			</li>
	
			<xsl:if test="(@fix='true') or (@normalize='true')">
	  			<li><a href="javascript:switchit('{generate-id()}owl')">Modified BioPAX</a>&#xa0;
	  			("escaped" RDF in HTML; choose BioPAX or XML as return if you plan to process it)</li>
				<ul id="{generate-id()}owl" style="display: none"><li><div><xsl:value-of select="@modelSerializedHtmlEscaped"/></div></li></ul>
			</xsl:if>
		</ul>
		
    	<ul id="{generate-id()}" style="display: none; list-style: decimal;">
    		<xsl:apply-templates select="error"/>
    	</ul>
    
    </xsl:template>
    
    <xsl:template match="error">
		<li title="Click to see the error cases">
			<a href="javascript:switchit('{generate-id()}')">
			<xsl:value-of select="@type"/>: <em><xsl:value-of select="@code"/></em>,&#xa0;category: <em><xsl:value-of select="@category"/></em>,
			&#xa0;cases: <em><xsl:value-of select="@totalCases"/></em>,&#xa0;
			<xsl:choose>
				<xsl:when test="@notFixedCases>0">
				not fixed: <em><xsl:value-of select="@notFixedCases"/></em>
				</xsl:when>
				<xsl:otherwise>
				all fixed!
				</xsl:otherwise>
	  		</xsl:choose>
			</a>
			<br/><xsl:value-of select="@message"/>
		</li>
		
		<ul id="{generate-id()}" style="display: none">
			<xsl:apply-templates/>
		</ul>
		<br/>
    </xsl:template>
    
    <xsl:template match="errorCase">
		<li>
			<xsl:if test="@fixed='true'"><b>[FIXED!]</b>&#xa0;</xsl:if>
			object:<b>&#xa0;<xsl:value-of select="@object"/></b>
			<div><xsl:value-of select="message"/></div>(found by: <em><xsl:value-of select="@reportedBy"/></em>)
		</li>
    </xsl:template>
    
</xsl:stylesheet>