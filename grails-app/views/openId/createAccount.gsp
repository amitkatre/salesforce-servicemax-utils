<head>
    <meta name='layout' content='main'/>
    <title>Create Account</title>
</head>

<body>

<div class="grid_5" >
    <div class="block" ></div>
</div>
<div class="grid_6" >
    <div class="box" >
        <h2>Please register..</h2>
        <p>No user was found with that OpenID but you can register now and associate your OpenID with that account.</p>
        <g:if test='${openId}'>
            Or if you're already a user you can <g:link action='linkAccount'>link this OpenID</g:link> to your account<br/>
            <br/>
        </g:if>
        <div class="block" id="login-form" >
            <fieldset class="login" >
                <legend>Login</legend>

                <g:hasErrors bean="${command}">
                    <div class="sixteen_column section error" >
                        <div class="sixteen column" >
                            <div class="column_content" >
                                <g:renderErrors bean="${command}" as="list"/>
                            </div>
                        </div>
                    </div>
                </g:hasErrors>

                <g:if test='${flash.error}'>
                    <div class="sixteen_column section error" >
                        <div class="sixteen column" >
                            <div class="column_content" >
                                <div class="errors">${flash.error}</div>
                            </div>
                        </div>
                    </div>
                </g:if>
                <g:form action='createAccount'>
                    <div class="sixteen_column section" >
                        <div class="sixteen column" >
                            <div class="column_content" >
                                <label for="openIdIdentifier" >Open ID:</label>
                                <span id='openIdIdentifier'>${openId}</span>
                            </div>
                        </div>
                    </div>
                    <div class="sixteen_column section" >
                        <div class="sixteen column" >
                            <div class="column_content" >
                                <label for="username" >Email:</label>
                                <g:textField name='username' value='${command.username}'/>
                            </div>
                        </div>
                    </div>
                    <div class="sixteen_column section" >
                        <div class="twelve column" >
                        </div>
                        <div class="four column" >
                            <div class="column_content" >
                                <g:submitButton name="Register" />
                            </div>
                        </div>
                    </div>
                </g:form>
            </fieldset>
        </div>
    </div>
</div>

</body>
