<%--
  Created by IntelliJ IDEA.
  User: churd
  Date: 5/6/12
  Time: 12:19 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Main</title>
    <meta name="layout" content="main" />
    <script type="text/javascript">
        $(function() {
            loadPage("#sfmMigrationDiv", "#mainBarContent", "<g:createLink controller="configuration" />", null);
        });
    </script>
</head>

<body>

    <div class="grid_2" id="leftBarContent" >&nbsp;
    </div>

    <div class="grid_12" id="mainBarContent" >
    </div>

    <div class="grid_2" id="rightBarContent" >&nbsp;
    </div>



</body>
</html>