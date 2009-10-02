<?xml version="1.0" encoding="utf-8"?>
<!--

    Copyright (c) 2000-2009, Jasig, Inc.
    See license distributed with this file and available online at
    https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt

-->
<!-- ========================================================================= -->
<!-- ========== README ======================================================= -->
<!-- ========================================================================= -->
<!-- 
 | The theme is written in XSL. For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
 | Baseline XSL skill is strongly recommended before modifying this file.
 |
 | This file has two purposes:
 | 1. To instruct the portal how to compile and configure the theme for use in mobile devices.
 | 2. To provide theme configuration and customization.
 |
 | As such, this file has a mixture of code that should not be modified, and code that exists explicitly to be modified.
 | To help make clear what is what, a RED-YELLOW-GREEN legend has been added to all of the sections of the file.
 |
 | RED: Stop! Do not modify.
 | YELLOW: Warning, proceed with caution.  Modifications can be made, but should not generally be necessary and may require more advanced skill.
 | GREEN: Go! Modify as desired.
-->

<!-- ========================================================================= -->
<!-- ========== DOCUMENT DESCRIPTION ========================================= -->
<!-- ========================================================================= -->
<!-- 
 | Date: 08/14/2008
 | Author: Matt Polizzotti
 | Company: Unicon,Inc.
 | uPortal Version: uP3.0.0 and uP3.0.1
 | 
 | General Description: This file, muniversality.xsl, was developed with mcolumn.xsl in order 
 | to enable uPortal 3.0.1 to be viewable by mobile devices. Supported mobile devices 
 | consist of Internet-enabled mobile devices running Windows Mobile 5+ and the BlackBerry Browser 4+. 
 | 
 | This file transforms the xml content generated by the mcolumn.xsl file into html, which 
 | is then rendered in a mobile device. This file formats the html markup to render uPortal's tabs 
 | and their associated channels and portlets for mobile display.
-->


<!-- ========================================================================= -->
<!-- ========== STYLESHEET DELCARATION ======================================= -->
<!-- ========================================================================= -->
<!-- 
 | RED
 | This statement defines this document as XSL.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== IMPORTS ====================================================== -->
<!-- ========================================================================= -->
<!-- 
| RED
| Imports are the XSL files that build the theme.
| Import statments and the XSL files they refer to should not be modified.
-->
<xsl:import href="components.xsl" />
<xsl:import href="nonfocused.xsl" />
<xsl:import href="focused.xsl" />
<!-- ========================================================================= -->


<!-- ============================================== -->
<!-- ========== VARIABLES and PARAMETERS ========== -->
<!-- ============================================== -->
<!-- 
| YELLOW - GREEN
| These variables and parameters provide flexibility and customization of the user interface.
| Changing the values of the variables and parameters signals the theme to reconfigure use 
| and location of user interface components. Most text used within the theme is localized.  
-->


<!-- ****** SKIN SETTINGS ****** -->
<!-- 
| YELLOW
| Skin Settings can be used to change the location of skin files.
--> 
<xsl:param name="skin">uportal3</xsl:param>
<xsl:variable name="SKIN" select="$skin"/>
<xsl:variable name="MEDIA_PATH">media/skins/muniversality</xsl:variable>
<xsl:variable name="SKIN_PATH" select="concat($MEDIA_PATH,'/',$SKIN)"/>
<xsl:variable name="SCRIPT_PATH">media/skins/muniversality/common/javascript</xsl:variable>
<xsl:variable name="PORTAL_SHORTCUT_ICON">/favicon.ico</xsl:variable>
<!-- ======================================== -->


<!-- ****** LOCALIZATION SETTINGS ****** -->
<!-- 
| GREEN
| Locatlization Settings can be used to change the localization of the theme.
-->
<xsl:param name="MESSAGE_DOC_URL">messages.xml</xsl:param>
<xsl:param name="USER_LANG">en</xsl:param>
<!-- ======================================== -->


<!-- ****** PORTAL SETTINGS ****** -->
<!-- 
| YELLOW
| Portal Settings should generally not be (and not need to be) modified.
-->
<xsl:param name="AUTHENTICATED" select="'false'"/>
<xsl:param name="USE_SELECT_DROP_DOWN">true</xsl:param>
<xsl:param name="BASE_ACTION_URL">render.userLayoutRootNode.uP</xsl:param>
<xsl:variable name="TOKEN" select="document($MESSAGE_DOC_URL)/theme-messages/tokens[lang($USER_LANG)]/token"/>


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: PAGE CSS =========================================== -->
<!-- ========================================================================= -->
<!-- 
| GREEN
| This template renders the CSS links in the page <head>.
| Cascading Stylesheets (CSS) that provide the visual style and presentation of the portal.
| Refer to [http://www.w3.org/Style/CSS/] for CSS definition and syntax.
| CSS files are located in the uPortal mobile skins directory: /media/skins/muniversality/.
| Template contents can be any valid XSL or XHTML.
-->
<xsl:template name="page.css">
    <link rel="stylesheet" type="text/css" href="{$SKIN_PATH}/{$SKIN}.css" />
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: PAGE JAVASCRIPT ==================================== -->
<!-- ========================================================================= -->
<!-- 
| GREEN
| This template renders the Javascript links in the page <head>.
| Javascript files are located in the uPortal skins directory:
| /media/skins/muniversality/common/[theme_name]/javascript
| Support across mobile browsers for Javascript is limited and
| should be used with caution when developing solutions.
| Template contents can be any valid XSL or XHTML.
-->
<xsl:template name="page.js">
    <!-- NONE ADDED -->
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: PAGE TITLE ========================================= -->
<!-- ========================================================================= -->
<!-- 
| GREEN
| This template renders the page title in the <head>.
| Template contents can be any valid XSL or XHTML.
-->
<xsl:template name="page.title">
   <title><xsl:value-of select="$TOKEN[@name='PORTAL_PAGE_TITLE']" /></title>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: PAGE META ========================================== -->
<!-- ========================================================================= -->
<!-- 
| GREEN
| This template renders keywords and descriptions in the <head>.
| Template contents can be any valid XSL or XHTML.
-->
<xsl:template name="page.meta">
   <meta name="description" content="{$TOKEN[@name='PORTAL_META_DESCRIPTION']}" />
   <meta name="keywords" content="{$TOKEN[@name='PORTAL_META_KEYWORDS']}" />
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: FOOTER ============================================= -->
<!-- ========================================================================= -->
<!-- 
| GREEN
| The footer template currently contains the portal's copyright information. This area can be 
| customized to contain any number of links or institution identifiers. This template renders 
| in all areas of the portal (unauthenticated, focused and non-focused). 
| Template contents can be any valid XSL or XHTML.
-->
<xsl:template name="footer">
    <div class="mobile-footer">
    	<a href="{$TOKEN[@name='COPYRIGHT_URL']}"><xsl:value-of select="$TOKEN[@name='COPYRIGHT_TEXT']" /></a>
    </div>
</xsl:template>
<!-- ========================================================================= -->


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: ROOT =============================================== -->
<!-- ========================================================================= -->
<!-- 
| RED
| This is the root xsl template and it defines the overall structure of the html markup. 
| Focused and Non-focused content is controlled through an xsl:choose statement.
| Template contents can be any valid XSL or XHTML.
-->
<xsl:template match="/">
    <html>
        <head>
            <xsl:call-template name="page.title" />
            <xsl:call-template name="page.meta" />
            <xsl:call-template name="page.css" />
            <xsl:call-template name="page.js" />
        </head>
        <body>
            <xsl:choose>
                <xsl:when test="//focused">
                    <xsl:apply-templates mode="focused" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates />
                </xsl:otherwise>
            </xsl:choose>
        </body>
    </html>
</xsl:template>
<!-- ========================================================================= -->


</xsl:stylesheet>