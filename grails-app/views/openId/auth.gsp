<head>
    <title>Login</title>
    <meta name="layout" content="main" />
</head>

<body>
<div class="grid_6" >
    <div class="block" ></div>
</div>
<div class="grid_4" >
    <div class="box" >
        <h2>Please login..</h2>
        <div class="block" id="login-form" >
            <form action='${openIdPostUrl}' method='POST' autocomplete='off' name='openIdLoginForm'>
                <fieldset class="login" >
                    <legend>Login</legend>

                    <g:if test='${flash.message}'>
                        <div class="sixteen_column section" >
                            <div class="sixteen column" >
                                <div class="column_content" >
                                    <div class='login_message'>${flash.message}</div>
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <div class="sixteen_column section" >
                        <div class="sixteen column" >
                            <div class="column_content" >
                                <label for="openIdIdentifier" >OpenID:</label>
                                <select name="${openidIdentifier}" id="openIdIdentifier" >
                                    <option value="https://www.google.com/accounts/o8/id" >Google</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <g:if test='${persistentRememberMe}'>
                        <div class="sixteen_column section" >
                            <div class="sixteen column" >
                                <div class="column_content" >
                                    <label for="remember_me" >Remember me</label>
                                    <g:checkBox name="${rememberMeParameter}" id="remember_me" />
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <div class="sixteen_column section" >
                        <div class="twelve column" >
                        </div>
                        <div class="four column" >
                            <div class="column_content" >
                                <g:submitButton name="Submit" />
                            </div>
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

</body>
