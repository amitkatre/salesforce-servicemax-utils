<!DOCTYPE html>
<html>
<head>
    <title><g:layoutTitle default="Grails" /></title>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <title>Salesforce Utils</title>
    <meta name="description" content="">
    <meta name="author" content="">

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'style.css?v=2')}" />
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'text.css')}" media="screen" />
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'layout.css')}" media="screen" />
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'grid.css')}" media="screen" />
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'superfish.css')}" media="screen" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.tagsinput.css')}" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.treeview.css')}" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'ui.jqgrid.css')}" />
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.gritter.css')}" />
    <!-- fluid GS -->
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'fluid.gs.css')}" media="screen" />
    <!--[if lt IE 8 ]>
	        <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'fluid.gs.lt_ie8.css')}" media="screen" />
	    <![endif]-->
    <!-- //jqueryUI css -->
    <link type="text/css" href="${resource(dir: 'css/custom-theme', file: 'jquery-ui-1.8.13.custom.css')}" rel="stylesheet" />

    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script>!window.jQuery && document.write(unescape('%3Cscript src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"%3E%3C/script%3E'))</script>
    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery-fluid16.js')}"></script>
    <script src="${resource(dir: 'js', file: 'plugins.js')}"></script>
    <script src="${resource(dir: 'js', file: 'script.js')}"></script>

    <!-- //xoxco tags plugin https://github.com/xoxco/jQuery-Tags-Input -->
    <script src="${resource(dir: 'js', file: 'jquery.tagsinput.min.js')}"></script>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery.tagsinput.css')}">

    <!--[if lt IE 7 ]>
        <script src="${resource(dir: 'js/libs', file: 'dd_belatedpng.js')}"></script>
        <script> DD_belatedPNG.fix('images, .png_bg');</script>
        <![endif]-->
    <script src="${resource(dir: 'js/libs', file: 'modernizr-1.7.min.js')}"></script>
    <script src="${resource(dir: 'js', file: 'superfish.js')}"></script>
    <script src="${resource(dir: 'js', file: 'supersubs.js')}"></script>
    <script src="${resource(dir: 'js', file: 'hoverIntent.js')}"></script>
    <script src="${resource(dir: 'js', file: 'jquery.treeview.js')}"></script>
    <script src="${resource(dir: 'js', file: 'jquery.form.js')}"></script>
    <script src="${resource(dir: 'js', file: 'jquery.gritter.js')}"></script>
    <script src="${resource(dir: 'js/i18n', file: 'grid.locale-en.js')}"></script>
    <script src="${resource(dir: 'js', file: 'jquery.metadata.js')}"></script>
    <script src="${resource(dir: 'js', file: 'jquery.jqGrid.min.js')}"></script>
    <script src="${resource(dir: 'js', file: 'jquery.dialog.form.js')}"></script>

    <g:layoutHead />
    <g:javascript library="application" />

    <script type="text/javascript" >

        $(function() {
            $('#navigationTop').superfish();
        });


    </script>

    <r:layoutResources />
</head>
<body>
<div class="container_16">
    <header>
        <div class="grid_16">
            <h1 id="branding">
                <a href="#">SalesForce Utils</a>
            </h1>
        </div>
        <div class="clear"></div>
        <div class="grid_16" >
            <sec:ifNotLoggedIn >
                <div id="navigationTop"  style="height:30px" ></div>
            </sec:ifNotLoggedIn>
            <sec:ifLoggedIn >
                <ul class="sf-menu" id="navigationTop" >
                    <li class="selected" >
                        <g:link action="index" controller="main" >Main</g:link>
                        <ul>
                            <li><a href="#" onclick='loadPage("#regOrgsDiv", "#leftBarContent", "<g:createLink controller="organizationInfo" />", null);return false;' >Manage Salesforce Logins</a></li>
                        </ul>
                    </li>
                </ul>
            </sec:ifLoggedIn>
        </div>
        <div class="grid_16" >
            <h2 id="page-heading" style="display: none;">&nbsp;</h2>
        </div>
        <div class="clear"></div>
        <div class="grid_12">
            <h2 id="page-crumbs" style="display: none;" >&nbsp;</h2>
        </div>
    </header>


    <div id="main" role="main" >
        <div id="content" >
            <div class="grid_16" ><div style="height:20px" ></div></div>
            <g:layoutBody />
        </div>

    </div>


    <footer>
        <div class="grid_16" id="site_info">
            <div class="box">
                <p>html5Admin - <a href="http://www.html5admin.com">Your Free HTML5 ready backEnd</a></p>
            </div>
        </div>
        <div class="clear"></div>
    </footer>
    <div id="dialog-form" ></div>
</div>
<r:layoutResources />
</body>
</html>