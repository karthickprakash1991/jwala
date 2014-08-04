package com.siemens.cto.aem.service.jvm.impl;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;

public class JvmControlServiceImpl implements JvmControlService {

    private final JvmService jvmService;
    private final JvmCommandExecutor jvmCommandExecutor;
    private final JvmControlServiceLifecycle jvmControlServiceLifecycle;

    public JvmControlServiceImpl(final JvmService theJvmService,
                                 final JvmCommandExecutor theExecutor,
                                 final JvmControlServiceLifecycle theLifecycle) {
        jvmService = theJvmService;
        jvmCommandExecutor = theExecutor;
        jvmControlServiceLifecycle = theLifecycle;
    }

    @Override
    public JvmControlHistory controlJvm(final ControlJvmCommand aCommand,
                                        final User aUser) {

        try {
            aCommand.validateCommand();
            
            final JvmControlHistory incompleteHistory = jvmControlServiceLifecycle.startHistory(aCommand, aUser);
            
            final Jvm jvm = jvmService.getJvm(aCommand.getJvmId());

            jvmControlServiceLifecycle.startState(aCommand, aUser);

            final ExecData execData = jvmCommandExecutor.controlJvm(aCommand,
                                                                    jvm);

            final JvmControlHistory completeHistory = jvmControlServiceLifecycle.completeHistory(incompleteHistory, aCommand, execData, aUser);

            return completeHistory;
        } catch (final CommandFailureException cfe) {
            throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                             "CommandFailureException when attempting to control a JVM: " + aCommand,
                                             cfe);
        }
    }

    
    public static class LifecycleImpl implements JvmControlService.JvmControlServiceLifecycle {

        private final JvmControlPersistenceService persistenceService;
        private final StateService<Jvm, JvmState> jvmStateService;

        public LifecycleImpl(JvmControlPersistenceService thePersistenceService,
                StateService<Jvm, JvmState> theJvmStateService) {
            jvmStateService = theJvmStateService;
            persistenceService = thePersistenceService;
        }

        @Override
        @Transactional
        public JvmControlHistory startHistory(final ControlJvmCommand aCommand, final User aUser) {
            return persistenceService.addIncompleteControlHistoryEvent(new Event<>(aCommand,
                                                                           AuditEvent.now(aUser)));
        }

        @Transactional
        @Override
        public void startState(final ControlJvmCommand aCommand, final User aUser) {
            jvmStateService.setCurrentState(createNewSetJvmStateCommand(aCommand),
                    aUser);  
        }

        @Transactional
        @Override
        public JvmControlHistory completeHistory(JvmControlHistory incompleteHistory, ControlJvmCommand aCommand, ExecData execData, final User aUser) {
            return persistenceService.completeControlHistoryEvent(new Event<>(new CompleteControlJvmCommand(incompleteHistory.getId(),
                    execData), AuditEvent.now(aUser)));
        }
        

        protected JvmSetStateCommand createNewSetJvmStateCommand(final ControlJvmCommand aControlCommand) {
            return new JvmSetStateCommandBuilder().setControlCommand(aControlCommand)
                                                  .build();
        }
        
    }
}
