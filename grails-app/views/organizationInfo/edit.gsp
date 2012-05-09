<div class="grid_6" >
    <div class='errors' style='display: none;'></div>
    <g:form action="save.json" class="create_organizationInfo">
        <g:hiddenField name="id" value="${orgInfo?.id}" />
        <div class="box" >
            <div class="sixteen_column section" >
                <div class="sixteen column" >
                    <div class="column_content" >
                        <label for="name" ><g:message code="organizationInfo.name" default="Name" />
                            <g:textField name="name" maxlength="255" value="${orgInfo?.name}" /></label>
                    </div>
                </div>
            </div>
            <div class="sixteen_column section" >
                <div class="sixteen column" >
                    <div class="column_content" >
                        <label for="username" ><g:message code="organizationInfo.username" default="Username" />
                        <g:textField name="username" maxlength="255" value="${orgInfo?.username}" /></label>
                    </div>
                </div>
            </div>
            <div class="sixteen_column section" >
                <div class="sixteen column" >
                    <div class="column_content" >
                        <label for="password" ><g:message code="organizationInfo.password" default="Password" />
                            <g:textField name="password" maxlength="255" value="${orgInfo?.password}" /></label>
                    </div>
                </div>
            </div>
            <div class="sixteen_column section" >
                <div class="sixteen column" >
                    <div class="column_content" >
                        <label for="securityToken" ><g:message code="organizationInfo.securityToken" default="Security Token" />
                            <g:textField name="securityToken" maxlength="255" value="${orgInfo?.securityToken}" /></label>
                    </div>
                </div>
            </div>
            <div class="sixteen_column section" >
                <div class="sixteen column" >
                    <div class="column_content" >
                        <label for="sandbox" ><g:message code="organizationInfo.sandbox" default="Is Sandbox?" />
                            <g:checkBox name="sandbox"  value="${orgInfo?.sandbox}" /></label>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</div>