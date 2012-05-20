<div class="grid_6" >
    <div class='errors' style='display: none;'></div>
    <g:form action="select.json" class="select_organizationInfo">
        <div class="box" >
            <div class="sixteen_column section" >
                <div class="sixteen column" >
                    <div class="column_content" >
                        <label for="id" ><g:message code="organizationInfo.name" default="Name" />
                            <g:select name="id" from="${orgInfoList}"
                                noSelection="${['null': 'Select One...']}"
                                optionKey="id" optionValue="name" ></g:select></label>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</div>