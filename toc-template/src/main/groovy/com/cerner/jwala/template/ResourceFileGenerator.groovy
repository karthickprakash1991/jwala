package com.cerner.jwala.template

import com.cerner.jwala.common.domain.model.app.Application
import com.cerner.jwala.common.domain.model.group.Group
import com.cerner.jwala.common.domain.model.jvm.Jvm
import com.cerner.jwala.common.domain.model.resource.ResourceGroup
import com.cerner.jwala.common.domain.model.webserver.WebServer
import com.cerner.jwala.common.properties.ApplicationProperties
import com.cerner.jwala.common.properties.ExternalProperties
import com.cerner.jwala.template.exception.ResourceFileGeneratorException
import groovy.text.StreamingTemplateEngine

class ResourceFileGenerator {
    static <T> String generateResourceConfig(String templateText, ResourceGroup resourceGroup, T selectedValue) {
        Group group = null
        WebServer webServer = null
        Jvm jvm = null
        Application webApp = null

        List<Group> groups = resourceGroup.getGroups();
        List<WebServer> webServers = null;
        List<Application> webApps = null;
        List<Jvm> jvms = null;

        if (selectedValue instanceof WebServer) {
            webServer = selectedValue as WebServer
            group = webServer.getParentGroup()
        } else if (selectedValue instanceof Jvm) {
            jvm = selectedValue as Jvm
            group = jvm.getParentGroup()
        } else if (selectedValue instanceof Application) {
            webApp = selectedValue as Application
            jvm = webApp.getParentJvm()
            group = webApp.getGroup()
        }
        groups.each {
            if (it.getWebServers() != null) {
                if (webServers == null) {
                    webServers = new ArrayList<>();
                }
                webServers.addAll(it.getWebServers());
            }

            if (it.getApplications() != null) {
                if (webApps == null) {
                    webApps = new ArrayList<>();
                }
                webApps.addAll(it.getApplications());
            }

            if (it.getJvms() != null) {
                if (jvms == null) {
                    jvms = new ArrayList<>();
                }
                jvms.addAll(it.getJvms());
            }
        }
        final map = new HashMap<String, String>(ApplicationProperties.properties);
        def binding = [webServers: webServers,
                       webServer : webServer,
                       jvms      : jvms,
                       jvm       : jvm,
                       webApps   : webApps,
                       webApp    : webApp,
                       groups    : groups,
                       group     : group,
                       vars      : map];

        def properties = ExternalProperties.properties
        if (properties.size() > 0) {
            final extMap = new HashMap<String, String>(properties);
            binding.ext = extMap;
        }

        final engine = new StreamingTemplateEngine();

        try {
            return engine.createTemplate(templateText).make(binding.withDefault { '' })
        } catch (final Exception e) {
            throw new ResourceFileGeneratorException("Failed to bind data and properties to the template. " +
                    "Cause(s) of the failure is/are: " + e.getMessage(), e)
        }

    }
}