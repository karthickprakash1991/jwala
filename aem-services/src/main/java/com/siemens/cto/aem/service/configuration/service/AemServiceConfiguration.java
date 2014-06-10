package com.siemens.cto.aem.service.configuration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siemens.cto.aem.control.configuration.AemCommandExecutorConfig;
import com.siemens.cto.aem.persistence.configuration.AemDaoConfiguration;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.app.impl.ApplicationServiceImpl;
import com.siemens.cto.aem.service.app.impl.PrivateApplicationServiceImpl;
import com.siemens.cto.aem.service.configuration.jms.AemJmsConfig;
import com.siemens.cto.aem.service.dispatch.CommandDispatchGateway;
import com.siemens.cto.aem.service.group.GroupControlService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.group.impl.GroupControlServiceImpl;
import com.siemens.cto.aem.service.group.impl.GroupServiceImpl;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.JvmStateService;
import com.siemens.cto.aem.service.jvm.impl.JvmControlServiceImpl;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.jvm.state.impl.JvmStateServiceImpl;
import com.siemens.cto.aem.service.jvm.state.jms.JmsJvmStateNotificationServiceImpl;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.impl.WebServerControlServiceImpl;
import com.siemens.cto.aem.service.webserver.impl.WebServerServiceImpl;

@Configuration
public class AemServiceConfiguration {

    @Autowired
    private AemPersistenceServiceConfiguration persistenceServiceConfiguration;

    @Autowired
    private AemDaoConfiguration aemDaoConfiguration;

    @Autowired
    private AemCommandExecutorConfig aemCommandExecutorConfig;

    @Autowired
    private AemJmsConfig aemJmsConfig;

    @Bean
    public GroupService getGroupService() {
        return new GroupServiceImpl(persistenceServiceConfiguration.getGroupPersistenceService());
    }

    @Bean
    public JvmService getJvmService() {
        return new JvmServiceImpl(persistenceServiceConfiguration.getJvmPersistenceService(),
                                  getGroupService());
    }

    @Bean
    public WebServerService getWebServerService() {
        return new WebServerServiceImpl(aemDaoConfiguration.getWebServerDao());
    }

    @Bean
    public ApplicationService getApplicationService() {
        return new ApplicationServiceImpl(aemDaoConfiguration.getApplicationDao(), persistenceServiceConfiguration.getApplicationPersistenceService());
    }

    @Bean
    public PrivateApplicationService getPrivateApplicationService() {
        return new PrivateApplicationServiceImpl(/** Relying on autowire */);
    }

    @Bean(name="jvmControlService")
    public JvmControlService getJvmControlService() {
        return new JvmControlServiceImpl(persistenceServiceConfiguration.getJvmControlPersistenceService(),
                                         getJvmService(),
                                         aemCommandExecutorConfig.getJvmCommandExecutor());
    }

    @Autowired
    CommandDispatchGateway commandDispatchGateway;
    
    @Bean(name="groupControlService")
    public GroupControlService getGroupControlService() {
        return new GroupControlServiceImpl(persistenceServiceConfiguration.getGroupControlPersistenceService(),
                                         getGroupService(),
                                         commandDispatchGateway);
    }

    @Bean
    public WebServerControlService getWebServerControlService() {
        return new WebServerControlServiceImpl(persistenceServiceConfiguration.getWebServerControlPersistenceService(),
                                               getWebServerService(),
                                               aemCommandExecutorConfig.getWebServerCommandExecutor());
    }

    @Bean
    public JvmStateService getJvmStateService() {
        return new JvmStateServiceImpl(persistenceServiceConfiguration.getJvmStatePersistenceService(),
                                       getJvmStateNotificationService());
    }

    @Bean
    public JvmStateNotificationService getJvmStateNotificationService() {
        return new JmsJvmStateNotificationServiceImpl(aemJmsConfig.getJmsPackageBuilder(),
                                                      aemJmsConfig.getJmsTemplate(),
                                                      aemJmsConfig.getJvmStateNotificationDestination());
    }
}
