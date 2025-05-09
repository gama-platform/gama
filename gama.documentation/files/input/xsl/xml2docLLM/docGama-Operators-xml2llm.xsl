<?xml version="1.0" encoding="UTF-8"?><!---->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wiki="www.google.fr">
<xsl:import href="docGama-utils-xml2llm.xsl"/>

<xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

<xsl:variable name="fileAA" select="'OperatorsAA'"/>
<xsl:variable name="fileBC" select="'OperatorsBC'"/>
<xsl:variable name="fileDH" select="'OperatorsDH'"/>
<xsl:variable name="fileIM" select="'OperatorsIM'"/>
<xsl:variable name="fileNR" select="'OperatorsNR'"/>
<xsl:variable name="fileSZ" select="'OperatorsSZ'"/>
<xsl:variable name="alphabetID" select="'*'"/>
<xsl:variable name="fileName" select="'Operators'"/>
<xsl:variable name="aa" select="'aa'"/>
<xsl:variable name="bc" select="'bc'"/>
<xsl:variable name="dh" select="'dh'"/>
<xsl:variable name="im" select="'im'"/>
<xsl:variable name="nr" select="'nr'"/>
<xsl:variable name="sz" select="'sz'"/>

<xsl:template match="/">

	<xsl:call-template name="buildOperators"/>
	
	<xsl:text>
	</xsl:text>
</xsl:template>
    
 <xsl:template name="buildOperators"> 
    <xsl:for-each select="doc/operators/operator[@alphabetOrder = $alphabetID or $alphabetID = '*']">
    	<xsl:sort select="@name" />
    	<xsl:variable name="operatorName" select="@name"/>
    	
================================================================================
character : <xsl:call-template name="checkName"/> 
  	<xsl:if test="@alternativeNameOf">
  		<xsl:variable name="nameOpAlt" select="@alternativeNameOf"/>  	
<xsl:text>
   Same signification as </xsl:text><xsl:value-of select="@alternativeNameOf"/>
   </xsl:if>
   
   
  	
  	<xsl:if test="combinaisonIO[node()]">
		<xsl:call-template name="buildOperands">
		<xsl:with-param name="operatorName" select="$operatorName"/>
		</xsl:call-template>
	</xsl:if>
	
	<xsl:if test="documentation/result[text()]"> 

Result: <xsl:value-of select="documentation/result"/>
    </xsl:if>
    
  <xsl:if test="documentation/comment[text()]">  

Comment: <xsl:value-of select="documentation/comment"/> 
  </xsl:if>
  
  <xsl:if test="documentation/specialCases[node()] | documentation/usages[node()] | documentation/usagesNoExample[node()]">

Special cases: </xsl:if> 
  <xsl:if test="documentation/specialCases[node()]">
  <xsl:for-each select="documentation/specialCases/case">    
<xsl:value-of select="@item"/> </xsl:for-each> </xsl:if>     
  <xsl:if test="documentation/usages[node()] | documentation/usagesNoExample[node()]">
	<xsl:for-each select="documentation/usagesNoExample/usage">    
<xsl:value-of select="@descUsageElt"/> </xsl:for-each>
  <xsl:for-each select="documentation/usages/usage">    
<xsl:value-of select="@descUsageElt"/> 
  
<xsl:call-template name="generateExamples"/>
 

</xsl:for-each>
  </xsl:if>

  <xsl:if test="documentation/usagesExamples[node()]">

Examples: 
<xsl:for-each select="documentation/usagesExamples/usage"><xsl:call-template name="generateExamples"/> </xsl:for-each>

  </xsl:if>

  <xsl:if test="documentation/examples[node()]">
   <xsl:for-each select="documentation" > <xsl:call-template name="generateExamples"/> </xsl:for-each>
</xsl:if>
  	</xsl:for-each>
 </xsl:template>   
 
 <xsl:template name="buildOperands">
 	<xsl:param name="operatorName"/>

Possible uses: <xsl:for-each select="combinaisonIO/operands"> <xsl:sort select="count(operand)"/> <xsl:text>
</xsl:text>
<xsl:call-template name="buildOperand"><xsl:with-param name="operatorName" select="$operatorName"/></xsl:call-template> 
</xsl:for-each>
 </xsl:template> 
 
 <xsl:template name="buildOperand">
 	<xsl:param name="operatorName"/>
 	<xsl:variable name="nbOperands" select="count(operand)"/>
 	
	<xsl:choose>
	<xsl:when test="count(operand) = 1">
<xsl:text></xsl:text> <xsl:value-of select="$operatorName"/> <xsl:text> </xsl:text>(<xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="operand/@type"/></xsl:with-param></xsl:call-template>) --->  <xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="@returnType"/></xsl:with-param></xsl:call-template> 
	</xsl:when>
	<xsl:when test="count(operand) = 2">
<xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="operand[@position=0]/@type"/></xsl:with-param></xsl:call-template> <xsl:text> </xsl:text> <xsl:value-of select="$operatorName"/> <xsl:text> </xsl:text> <xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="operand[@position=1]/@type"/></xsl:with-param></xsl:call-template> --->  <xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="@returnType"/></xsl:with-param></xsl:call-template>
<xsl:text></xsl:text> <xsl:value-of select="$operatorName"/> <xsl:text> </xsl:text>(<xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="operand[@position=0]/@type"/></xsl:with-param></xsl:call-template> <xsl:text> , </xsl:text> <xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="operand[@position=1]/@type"/></xsl:with-param></xsl:call-template>) --->  <xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="@returnType"/></xsl:with-param></xsl:call-template>		
	</xsl:when>	
	<xsl:otherwise>
<xsl:text></xsl:text> <xsl:value-of select="$operatorName"/> <xsl:text> </xsl:text>(<xsl:for-each select="operand">
		<xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="@type"/></xsl:with-param></xsl:call-template>

		<xsl:choose>		
			<xsl:when test="@position = ($nbOperands - 1)">)</xsl:when>
			<xsl:otherwise>, </xsl:otherwise>
		</xsl:choose>		
	
	</xsl:for-each> --->  <xsl:call-template name="checkType"><xsl:with-param name="type"><xsl:value-of select="@returnType"/></xsl:with-param></xsl:call-template> 
	</xsl:otherwise>
	</xsl:choose>
 </xsl:template> 


 <xsl:template name="checkType">
  	<xsl:param name="type"/>
	<xsl:choose> 
 		<xsl:when test="$type = 'list&lt;list&gt;'"><xsl:text>list&lt;list&gt;</xsl:text></xsl:when>
  		<xsl:when test="$type = 'list&lt;list&lt;point&gt;&gt;'"><xsl:text>list&lt;list&lt;point&gt;&gt;</xsl:text></xsl:when>
    	<xsl:when test="$type = 'list&lt;list&lt;agent&gt;&gt;'"><xsl:text>list&lt;list&lt;agent&gt;&gt;</xsl:text></xsl:when>
 		<xsl:when test="$type = 'list&lt;agent&gt;'"><xsl:text>list&lt;agent&gt;</xsl:text></xsl:when>
 		<xsl:when test="$type = 'list&lt;geometry&gt;'"><xsl:text>list&lt;geometry&gt;</xsl:text></xsl:when>
 		<xsl:when test="$type = 'list&lt;point&gt;'"><xsl:text>list&lt;point&gt;</xsl:text></xsl:when>
 		<xsl:when test="$type = 'list&lt;path&gt;'"><xsl:text>list&lt;path&gt;</xsl:text></xsl:when>
 		<xsl:when test="$type = 'list&lt;float&gt;'"><xsl:text>list&lt;float&gt;</xsl:text></xsl:when>
 		<xsl:when test="$type = 'list&lt;rgb&gt;'"><xsl:text>list&lt;rgb&gt;</xsl:text></xsl:when>
  		<xsl:when test="$type = 'list&lt;KeyType&gt;'"><xsl:text>list&lt;KeyType&gt;</xsl:text></xsl:when>
 		
 		<xsl:when test="$type = 'container&lt;geometry&gt;'"><xsl:text>container&lt;geometry&gt;</xsl:text></xsl:when>
  		<xsl:when test="$type = 'container&lt;agent&gt;'"><xsl:text>container&lt;agent&gt;</xsl:text></xsl:when>
  		<xsl:when test="$type = 'container&lt;float&gt;'"><xsl:text>container&lt;float&gt;</xsl:text></xsl:when>  		
  		<xsl:when test="$type = 'container&lt;KeyType,ValueType&gt;'"><xsl:text>container&lt;KeyType,ValueType&gt;</xsl:text></xsl:when>
  		
  		<xsl:when test="$type = 'map&lt;string,unknown&gt;'"><xsl:text>map&lt;string,unknown&gt;</xsl:text></xsl:when>	
  		<xsl:when test="$type = 'map&lt;string,list&gt;'"><xsl:text>map&lt;string,list&gt;</xsl:text></xsl:when>	
   		<xsl:when test="$type = 'map&lt;point,float&gt;'"><xsl:text>map&lt;point,float&gt;</xsl:text></xsl:when>	
   		<xsl:when test="$type = 'map&lt;agent,float&gt;'"><xsl:text>map&lt;agent,float&gt;</xsl:text></xsl:when>	
  		
 		<xsl:when test="$type = 'matrix&lt;float&gt;'"><xsl:text>matrix&lt;float&gt;</xsl:text></xsl:when>
  		<xsl:when test="$type = 'matrix&lt;int&gt;'"><xsl:text>matrix&lt;int&gt;</xsl:text></xsl:when>
  		
  		
 		
 		<xsl:otherwise><xsl:value-of select="$type"/></xsl:otherwise>
 	</xsl:choose> 
 </xsl:template>
</xsl:stylesheet>
