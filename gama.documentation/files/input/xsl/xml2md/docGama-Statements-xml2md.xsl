<?xml version="1.0" encoding="UTF-8"?><!---->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wiki="www.google.fr">
<xsl:import href="docGama-utils-xml2md.xsl"/>


<xsl:variable name="smallcase" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

<xsl:template match="/">
 	<xsl:text># Statements</xsl:text>
 	
<xsl:call-template name="msgIntro"/>

<xsl:text>## Table of Contents
</xsl:text> <xsl:call-template name="buildStatementsByName"/> <xsl:text>


## Statements by kinds
</xsl:text> <xsl:call-template name="buildStatementsByKind"/> <xsl:text>


## Statements by embedment
</xsl:text> <xsl:call-template name="buildStatementsByEmbeded"/> <xsl:text>


## General syntax

A statement represents either a declaration or an imperative command. It consists in a keyword, followed by specific facets, some of them mandatory (in bold), some of them optional. One of the facet names can be omitted (the one denoted as omissible). It has to be the first one.

```
statement_keyword expression1 facet2: expression2 ... ;
or
statement_keyword facet1: expression1 facet2: expression2 ...;
```

If the statement encloses other statements, it is called a **sequence statement**, and its sub-statements (either sequence statements or single statements) are declared between curly brackets, as in:

```
statement_keyword1 expression1 facet2: expression2... { // a sequence statement
     statement_keyword2 expression1 facet2: expression2...;  // a single statement
     statement_keyword3 expression1 facet2: expression2...;
}
```
	</xsl:text>
		<xsl:call-template name="buildStatements"/>

	<xsl:text>
	</xsl:text>

</xsl:template>

<xsl:template name="buildStatements">
	<xsl:for-each select="doc/statements/statement">
    	<xsl:sort select="@name" />

----
<!-- <xsl:call-template name="keyword">     -->
<!-- 	<xsl:with-param name="category" select="'statement'"/> -->
<!-- 	<xsl:with-param name="nameGAMLElement" select="@name"/> -->
<!-- </xsl:call-template> -->
### <xsl:value-of select="@name"/>
  	<xsl:if test="not(@alt_name_of)">

		<xsl:call-template name="buildFacets"/>
		<xsl:call-template name="buildDefinition"/>				
		<xsl:call-template name="buildEmbedments"/>
	</xsl:if>
		
<xsl:if test="@alt_name_of">
<xsl:text>
See: [</xsl:text><xsl:value-of select="@alt_name_of"/><xsl:text>](#</xsl:text><xsl:value-of select="translate(@alt_name_of, $uppercase, $smallcase)"/><xsl:text>)</xsl:text>

</xsl:if>
		
	</xsl:for-each>
</xsl:template>

<xsl:template name="buildEmbedments"> 
<xsl:variable name="nameStatGlobal" select="@name"/>

#### Embedments

* The `<xsl:value-of select="@name"/>` statement is of type: **<xsl:value-of select="@kind"/>**
* The `<xsl:value-of select="@name"/>` statement can be embedded into: <xsl:for-each select="inside/symbols/symbol"><xsl:value-of select="text()"/>, </xsl:for-each><xsl:for-each select="inside/kinds/kind"><xsl:value-of select="text()"/>, </xsl:for-each>
* The `<xsl:value-of select="@name"/>` statement embeds statements: <xsl:for-each select="/doc/statements/statement"> 
			<xsl:sort select="@name" />
				<xsl:variable name="nameStat" select="@name"/>			
			<xsl:for-each select="inside/symbols/symbol"><xsl:variable name="symbolItem" select="text()"/><xsl:if test="$symbolItem = $nameStatGlobal "><xsl:text>[</xsl:text> <xsl:value-of select="$nameStat"/> <xsl:text>](#</xsl:text> <xsl:value-of select="$nameStat"/> <xsl:text>),  </xsl:text></xsl:if></xsl:for-each>
		</xsl:for-each>
  
</xsl:template>

    
<xsl:template name="buildFacets"> 
#### Facets 
<xsl:for-each select="facets/facet">
	<xsl:sort select="@optional"/>			
	<xsl:sort select="@omissible" order="descending"/>	
	<xsl:sort select="@name" />  
	<xsl:choose>
  		<xsl:when test="@optional = 'false'">  
* **`<xsl:value-of select="@name"/>`** </xsl:when> <xsl:otherwise>
* `<xsl:value-of select="@name"/>` </xsl:otherwise></xsl:choose>(<xsl:value-of select="@type"/>)<xsl:if test="@omissible = 'true'">, (omissible) </xsl:if><xsl:if test="@values"><xsl:value-of select="@values"/></xsl:if>: <xsl:value-of select="documentation/result"/> 
</xsl:for-each>
</xsl:template>

 <xsl:template name="buildDefinition"> 
 	<xsl:if test="documentation[text()]"> 
 	
#### Definition

<xsl:value-of select="documentation/result"/>

#### Usages
<xsl:for-each select="documentation/usages/usage">
* <xsl:value-of select="@descUsageElt"/> 
<xsl:if test="examples[node()]">

```<xsl:call-template name="generateExamples"/>
```

</xsl:if>
</xsl:for-each>

<xsl:for-each select="documentation/usagesNoExample/usage">
* <xsl:value-of select="@descUsageElt"/> </xsl:for-each>

<xsl:if test="documentation/usagesExamples[node()]">
* Other examples of use: 
```<xsl:for-each select="documentation/usagesExamples/usage">
<xsl:call-template name="generateExamples"/> </xsl:for-each>
``` 
  </xsl:if>

  <xsl:if test="documentation/seeAlso[node()]">    
* See also: <xsl:for-each select="documentation/seeAlso/see"><xsl:text>[</xsl:text> <xsl:value-of select="@id"/><xsl:text>](#</xsl:text><xsl:value-of select="translate(@id, $uppercase, $smallcase)"/><xsl:text>)</xsl:text><xsl:text>, </xsl:text> </xsl:for-each>
  </xsl:if>
</xsl:if>
</xsl:template>

<xsl:template name="buildStatementsByKind">
	<xsl:for-each select="//doc/statementsKinds/kind[not(@symbol=preceding::kind/@symbol)]">
		<xsl:sort select="@symbol"/>
		<xsl:variable name="kindGlobal" select="@symbol"/> 			
		<xsl:text>
* **</xsl:text> <xsl:value-of select="$kindGlobal"/> <xsl:text>**
  * </xsl:text>
		<xsl:for-each select="/doc/statements/statement"> 
			<xsl:sort select="@name" />
				<xsl:if test="@kind = $kindGlobal "> 
					<xsl:text>[</xsl:text> <xsl:value-of select="@name"/> <xsl:text>](#</xsl:text> <xsl:value-of select="translate(@name, $uppercase, $smallcase)"/> <xsl:text>), </xsl:text> 
				
	<!--				<xsl:choose>	
								
					  <xsl:when test="@alt_name_of">
					      <xsl:text>[</xsl:text> <xsl:value-of select="@name"/> <xsl:text>](#</xsl:text> <xsl:value-of select="translate(@alt_name_of, $uppercase, $smallcase)"/> <xsl:text>), </xsl:text> 
					  </xsl:when>
					  <xsl:otherwise>
					      <xsl:text>[</xsl:text> <xsl:value-of select="@name"/> <xsl:text>](#</xsl:text> <xsl:value-of select="translate(@name, $uppercase, $smallcase)"/> <xsl:text>), </xsl:text> 
					  </xsl:otherwise>
					 </xsl:choose>		
	-->		
				</xsl:if>							
		</xsl:for-each>    	
	</xsl:for-each>
</xsl:template>

<xsl:template name="buildStatementsByName">
	<xsl:for-each select="/doc/statements/statement"> 
		<xsl:sort select="@name" />
<xsl:text>[</xsl:text> <xsl:value-of select="@name"/> <xsl:text>](#</xsl:text> <xsl:value-of select="translate(@name, $uppercase, $smallcase)"/> <xsl:text>), </xsl:text> 
		
<!-- 		<xsl:choose>
		  <xsl:when test="@alt_name_of">
		      <xsl:text>[</xsl:text> <xsl:value-of select="@name"/> <xsl:text>](#</xsl:text> <xsl:value-of select="translate(@alt_name_of, $uppercase, $smallcase)"/> <xsl:text>), </xsl:text> 
		  </xsl:when>
		  <xsl:otherwise>
		      <xsl:text>[</xsl:text> <xsl:value-of select="@name"/> <xsl:text>](#</xsl:text> <xsl:value-of select="translate(@name, $uppercase, $smallcase)"/> <xsl:text>), </xsl:text> 
		  </xsl:otherwise>
		</xsl:choose>
 -->		
	</xsl:for-each>  
</xsl:template>

<xsl:template name="buildStatementsByEmbeded">
	<xsl:for-each select="//doc/insideStatementKinds/insideStatementKind[not(@symbol = (preceding-sibling::*/@symbol))]">
		<xsl:sort select="@symbol"/>
		<xsl:variable name="kindGlobal" select="@symbol"/> 
		<xsl:text>
* **</xsl:text> <xsl:value-of select="$kindGlobal"/> <xsl:text>**
  * </xsl:text>
		<xsl:for-each select="/doc/statements/statement"> 
			<xsl:sort select="@name" />			
			<xsl:for-each select="inside/kinds/kind">
				<xsl:variable name="kindItem" select="text()"/>
				<xsl:if test="$kindItem = $kindGlobal "> 
					  <xsl:text>[</xsl:text> <xsl:value-of select="../../../@name"/> <xsl:text>](#</xsl:text> <xsl:value-of select="translate(../../../@name, $uppercase, $smallcase)"/> <xsl:text>), </xsl:text> 
				</xsl:if>			
			</xsl:for-each>
		</xsl:for-each>    	
	</xsl:for-each>
	<xsl:for-each select="//doc/insideStatementSymbols/insideStatementSymbol[not(@symbol = (preceding-sibling::*/@symbol))]">
		<xsl:sort select="@symbol"/>
		<xsl:variable name="symbolGlobal" select="@symbol"/> 
		<xsl:text>
* **</xsl:text> <xsl:value-of select="$symbolGlobal"/> <xsl:text>**
  * </xsl:text>
		<xsl:for-each select="/doc/statements/statement"> 
			<xsl:sort select="@name" />
				<xsl:variable name="nameStat" select="@name"/>
			
			<xsl:for-each select="inside/symbols/symbol">
				<xsl:variable name="symbolItem" select="text()"/>
				<xsl:if test="$symbolItem = $symbolGlobal "> 
					<xsl:text>[</xsl:text> <xsl:value-of select="$nameStat"/> <xsl:text>](#</xsl:text> <xsl:value-of select="translate($nameStat, $uppercase, $smallcase)"/> <xsl:text>), </xsl:text> 					
				</xsl:if>			
			</xsl:for-each>
		</xsl:for-each>    	
	</xsl:for-each>	
</xsl:template>

</xsl:stylesheet>
