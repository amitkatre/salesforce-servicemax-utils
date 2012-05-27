<%@ page import="com.chrishurd.salesforce.MetadataObject" %>

<div class="eight column" >
    <div class="column_content" >
        <div id="metadata-navigator" >
            <ul id="metaNavColumn1" class="treeview-famfamfam" >
                <g:each in="${metadataMap.keySet().sort()}" var="type" status="i" >
                    <li class="closed" >${type}
                        <ul>
                            <g:if test="${metadataMap.get(type).managedObjects}" >
                                <li class="closed" >Managed
                                    <ul>
                                        <g:each in="${metadataMap.get(type).managedObjects.sort(MetadataObject.nameComparator)}" var="obj" >
                                            <li>${obj.name}</li>
                                        </g:each>
                                    </ul>
                                </li>
                            </g:if>
                            <g:if test="${metadataMap.get(type).unmanagedObjects}" >
                                <li class="closed" >Un-managed
                                    <ul>
                                        <g:each in="${metadataMap.get(type).unmanagedObjects.sort(MetadataObject.nameComparator)}" var="obj" >
                                            <li>${obj.name}</li>
                                        </g:each>
                                    </ul>
                                </li>
                            </g:if>
                        </ul>
                    </li>

                    <g:if test="${ i + 1 == metadataMap.size() / 2}" >
            </ul>
        </div>
    </div>
</div>
<div class="eight column" >
    <div class="column_content" >
        <div id="metadata-navigator2" >
            <ul id="metaNavColumn2" class="treeview-famfamfam" >
                    </g:if>
                </g:each>
            </ul>
        </div>
    </div>
</div>

