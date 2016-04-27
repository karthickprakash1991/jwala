package com.siemens.cto.aem.template
import com.siemens.cto.aem.common.domain.model.app.Application
import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState
import com.siemens.cto.aem.common.domain.model.group.Group
import com.siemens.cto.aem.common.domain.model.group.GroupState
import com.siemens.cto.aem.common.domain.model.group.History
import com.siemens.cto.aem.common.domain.model.id.Identifier
import com.siemens.cto.aem.common.domain.model.jvm.Jvm
import com.siemens.cto.aem.common.domain.model.jvm.JvmState
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath
import com.siemens.cto.aem.common.domain.model.path.Path
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup
import com.siemens.cto.aem.common.domain.model.webserver.WebServer
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState
import org.joda.time.DateTime

class TestResourceFileGenerator extends GroovyTestCase{

    List<Jvm> jvms
    List<Application> apps
    List<WebServer> webServers
    WebServer webServer
    Jvm jvm
    Application app
    ResourceGroup resourceGroup;

    void setUp() {
        def groupHashSet = new HashSet<Group>();

        (jvms, webServers) = createTestJvmsAndWebServers(groupHashSet)


        def group = new Group(new Identifier<Group>(1111L), "groupName", new HashSet(jvms), new HashSet(webServers), new CurrentGroupState<>(new Identifier<Group>(1111L), GroupState.GRP_STOPPED, DateTime.now()), new HashSet<History>())
        groupHashSet.add(group);
        app = new Application(new Identifier<Application>(111L), "hello-world-1", "d:/stp/app/archive", "/hello-world-1", group, true, true, false, "testWar.war")

        apps = new ArrayList<>()
        apps.add(app)
        apps.add(new Application(new Identifier<Application>(222L), "hello-world-2", "d:/stp/app/archive", "/hello-world-2", group, true, true, false, "testWar.war"))
        apps.add(new Application(new Identifier<Application>(333L), "hello-world-3", "d:/stp/app/archive", "/hello-world-3", group, true, true, false, "testWar.war"))

        // do it again to associate the group with the jvms and web servers
        (jvms, webServers) = createTestJvmsAndWebServers(groupHashSet)
    }

    private List createTestJvmsAndWebServers(HashSet<Group> groupHashSet) {
        webServer = new WebServer(new Identifier<WebServer>(1L), groupHashSet, "Apache2.4", "localhost", 80, 443,
                new Path("/statusPath"), new FileSystemPath("D:/stp/app/data/httpd//httpd.conf"),
                new Path("./"), new Path("htdocs"), WebServerReachableState.WS_UNREACHABLE, "");
        jvm = new Jvm(new Identifier<Jvm>(11L), "tc1", "usmlvv1ctoGenerateMe", groupHashSet, 11010, 11011, 11012, -1, 11013,
                new Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, "")


        webServers = new ArrayList<>()
        webServers.add(webServer)

        jvms = new ArrayList<>()
        jvms.add(jvm)
        jvms.add(new Jvm(new Identifier<Jvm>(22L), "tc2", "usmlvv1ctoGenerateMe", groupHashSet, 11020, 11021, 11022, -1, 11023,
                new Path("/statusPath"), "EXAMPLE_OPTS=%someEvn%/someVal", JvmState.JVM_STOPPED, ""))
        [jvms, webServers]
    }

    void testGenerateHttpdConfConfigFile(){
        File httpdTemplate = new File("./src/test/resources/HttpdConfTemplate.tpl");
        resourceGroup = new ResourceGroup(webServers, jvms, apps);
        def generatedText = ResourceFileGenerator.generateResourceConfig(httpdTemplate.text, resourceGroup, webServer);
        def expectedText = new File("./src/test/resources/HttpdConfTemplate-EXPECTED.conf").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    void testGenerateInvokeBatConfigFile(){
        File httpdTemplate = new File("./src/test/resources/InvokeBatTemplate.tpl");
        resourceGroup = new ResourceGroup(webServers, jvms, apps);
        def generatedText = ResourceFileGenerator.generateResourceConfig(httpdTemplate.text, resourceGroup, jvm);
        def expectedText = new File("./src/test/resources/InvokeBatTemplate-EXPECTED.bat").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    void testGenerateInvokeWSBatConfigFile() {
        File httpdTemplate = new File("./src/test/resources/InvokeWSBatTemplate.tpl");
        resourceGroup = new ResourceGroup(webServers, jvms, apps);
        def generatedText = ResourceFileGenerator.generateResourceConfig(httpdTemplate.text, resourceGroup, webServer);
        def expectedText = new File("./src/test/resources/InvokeWSBatTemplate-EXPECTED.bat").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    void testGenerateServerXMLConfigFile() {
        File httpdTemplate = new File("./src/test/resources/ServerXMLTemplate.tpl");
        resourceGroup = new ResourceGroup(webServers, jvms, apps);
        def generatedText = ResourceFileGenerator.generateResourceConfig(httpdTemplate.text, resourceGroup, jvm);
        def expectedText = new File("./src/test/resources/ServerXMLTemplate-EXPECTED.xml").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    void testGenerateSetenvBatConfigFile() {
        File httpdTemplate = new File("./src/test/resources/SetenvBatTemplate.tpl");
        resourceGroup = new ResourceGroup(webServers, jvms, apps);
        def generatedText = ResourceFileGenerator.generateResourceConfig(httpdTemplate.text, resourceGroup, jvm);
        def expectedText = new File("./src/test/resources/SetenvBatTemplate-EXPECTED.bat").text
        assertEquals(removeCarriageReturnsAndNewLines(expectedText), removeCarriageReturnsAndNewLines(generatedText));
    }

    private static String removeCarriageReturnsAndNewLines(String s) {
        return s.replaceAll("\\r", "").replaceAll("\\n", "")
    }


}
