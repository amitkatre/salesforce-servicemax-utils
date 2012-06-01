package com.chrishurd.salesforce

import com.sforce.soap.metadata.ListMetadataQuery
import com.sforce.soap.metadata.ManageableState
import com.sforce.soap.metadata.PackageTypeMembers
import com.sforce.soap.metadata.RetrieveRequest
import com.sforce.soap.metadata.Package
import com.sforce.soap.metadata.AsyncRequestState
import org.apache.commons.logging.LogFactory
import java.nio.channels.ReadableByteChannel
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import org.apache.commons.io.FileUtils

class MetadataService {

    private static final log = LogFactory.getLog(this)

    def connectionService
    def grailsApplication

    def static final ONE_SECOND = 1000
    def static final MAX_NUM_POLL_REQUESTS = 100


    def describeAllMetadata(orgInfo) {

        def connection = connectionService.getMetedataConnection(orgInfo)
        def metadataMap = this.describeMetadata(connection, MetadataEnum.values())

        return metadataMap
    }

    def describeMetadata(connection, objectTypes) {

        def queryList = []
        def metadataMap = [:]

        objectTypes.eachWithIndex { type, index ->
            def query = new ListMetadataQuery()
            query.setType(type.objectType)
            queryList.add(query)

            if (queryList.size() == 3 || index + 1 == objectTypes.size()) {
                def lmr =  connection.listMetadata(queryList as ListMetadataQuery[], Double.valueOf(grailsApplication.config.sfdc.api.version))
                if (lmr.length > 0) {
                    lmr.each { obj ->
                        if (! obj.manageableState.equals(ManageableState.installed) && ! obj.manageableState.equals(ManageableState.released) || obj.type.equals('CustomObject')) {

                            if (! metadataMap.containsKey(obj.type)) {
                                def mType = new MetadataType()
                                mType.setType(obj.type)
                                metadataMap.put(obj.type, mType)
                            }

                            def mObj = new MetadataObject()
                            mObj.createdBy = obj.createdByName
                            mObj.createdOn = obj.createdDate
                            mObj.id = obj.id
                            mObj.lastModifiedBy = obj.lastModifiedByName
                            mObj.lastModifiedOn = obj.lastModifiedDate
                            mObj.name = obj.fullName
                            mObj.filename = obj.fileName
                            mObj.namespace = obj.namespacePrefix
                            metadataMap.get(obj.type).objects.add(mObj)
                        }
                    }
                }

                queryList.clear()
            }
        }

        return metadataMap
    }


    def getMetadata(orgInfo, loadedObjects) {
        def dir = new File(getMetadataDir(orgInfo))
        if (! dir.exists()) {
            FileUtils.forceMkdir(dir)
        }
        FileUtils.cleanDirectory(new File(getMetadataDir(orgInfo)))
        def packageTypeMembersMap = getPackageTypeMembersMap(loadedObjects)

        if (packageTypeMembersMap.containsKey('CustomObject')) {
            def packageDef = []

            packageDef.add(this.getPackageTypeMembers('CustomObject', packageTypeMembersMap.get('CustomObject')))

            if (packageTypeMembersMap.containsKey('Profile')) {
                packageDef.add(this.getPackageTypeMembers('Profile', packageTypeMembersMap.get('Profile')))
            }

            if (packageTypeMembersMap.containsKey('Layout')) {
                packageDef.add(this.getPackageTypeMembers('Layout', packageTypeMembersMap.get('Layout')))
            }

            getMetadataFromPackageMembers(orgInfo, packageDef)
        }

        def count = 0
        def packageDefList = []
        packageTypeMembersMap.keySet().eachWithIndex { key, index ->
            if (! key.equals('CustomObject')
                    && (! packageTypeMembersMap.containsKey('CustomObject') || (! key.equals('Profile') && ! key.equals('Layout')))) {
                def pkg = this.getPackageTypeMembers(key, packageTypeMembersMap.get(key))
                count += pkg.getMembers().length
                packageDefList.add(pkg)
            }

            if (count > 20 || index == packageTypeMembersMap.size() - 1) {
                getMetadataFromPackageMembers(orgInfo, packageDefList)
                packageDefList.clear()
                count = 0
            }
        }

    }

    def getMetadataFromPackageMembers(orgInfo, packageDef) {
        def connection = connectionService.getMetedataConnection(orgInfo)
        def request = new RetrieveRequest()
        request.setApiVersion(Double.valueOf(grailsApplication.config.sfdc.api.version))
        def pkg = new Package()
        pkg.setTypes(packageDef as PackageTypeMembers[])
        request.setUnpackaged(pkg)

        def asyncRequest = connection.retrieve(request)

        def waitTimeMilliSecs = ONE_SECOND
        def poll = 0

        while (! asyncRequest.isDone()) {
            Thread.sleep(waitTimeMilliSecs)
            waitTimeMilliSecs *= 2

            if (poll++ > MAX_NUM_POLL_REQUESTS) {
                throw new Exception("Request timed out.  If this is a large set of metadata component, check that the time allowed is sufficient")
            }

            asyncRequest = connection.checkStatus([asyncRequest.getId()] as String[])[0]
            log.debug("Status is: " + asyncRequest.getState())
        }

        if (asyncRequest.getState() != AsyncRequestState.Completed) {
            throw new Exception(asyncRequest.getStatusCode() + " msg: " + asyncRequest.getMessage())
        }

        def result = connection.checkRetrieveStatus(asyncRequest.getId())

        if (result.getMessages()) {
            result.getMessages().each { msg ->
                log.warn(msg.getFileName() + " - " + msg.getProblem())
            }
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(result.getZipFile())
        File resultsFile = new File(this.getMetadataDir(orgInfo) + "retrieveResults.zip")
        FileOutputStream os = new FileOutputStream(resultsFile)

        try {
            ReadableByteChannel src = Channels.newChannel(bais)
            FileChannel dest = os.getChannel()
            ByteBuffer buffer = ByteBuffer.allocate(8092)
            while (src.read(buffer) != -1) {
                buffer.flip()
                while (buffer.hasRemaining()) {
                    dest.write(buffer)
                }
                buffer.clear()
            }
        }
        finally {
            os.close()
        }

        new AntBuilder().unzip(src: resultsFile.getAbsolutePath(), dest: this.getMetadataDir(orgInfo), overwrite: "false")
    }

    def getPackageTypeMembers(type, objects) {
        def packageItem = new PackageTypeMembers();
        packageItem.setName(type)
        packageItem.setMembers(objects as String[])

        return packageItem
    }


    def getPackageTypeMembersMap(loadedObjects) {
        def packageTypeMembersMap = [:]

        loadedObjects.each { obj ->
            def type = obj.substring(0, obj.indexOf('/'))
            def name = obj.substring(obj.indexOf('/') + 1)

            if (! packageTypeMembersMap.containsKey(type)) {
                packageTypeMembersMap.put(type, [])
            }

            packageTypeMembersMap.get(type).add(name)

        }

        return packageTypeMembersMap
    }

    def getMetadataDir(orgInfo) {
        def path = "${grailsApplication.config.sfdc_svmx.metadata.dir}${File.separator}${orgInfo.user.id}${File.separator}$orgInfo.id${File.separator}"

        return path
    }
}
