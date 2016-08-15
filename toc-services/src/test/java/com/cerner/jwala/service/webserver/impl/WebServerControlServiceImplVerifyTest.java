package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.request.state.SetStateRequest;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.*;
import com.cerner.jwala.service.state.StateNotificationService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.impl.WebServerControlServiceImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebServerControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private WebServerControlServiceImpl webServerControlService;

    @Mock
    private WebServerService webServerService;

    @Mock
    private RemoteCommandExecutor<WebServerControlOperation> commandExecutor;

    @Captor
    private ArgumentCaptor<SetStateRequest<WebServer, WebServerReachableState>> setStateCommandCaptor;

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private StateNotificationService stateNotificationService;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    RemoteCommandExecutorService remoteCommandExecutorService;

    @Mock
    private SshConfiguration mockSshConfig;

    private User user;

    @Before
    public void setup() {
        webServerControlService = new WebServerControlServiceImpl(webServerService, commandExecutor,
                mockHistoryService, mockMessagingService, remoteCommandExecutorService, mockSshConfig);

        user = new User("unused");
    }

    @Test
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        String wsName = "mockWebServerName";
        String wsHostName = "mockWebServerHost";
        final ControlWebServerRequest controlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer webServer = mock(WebServer.class);

        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(webServer.getName()).thenReturn(wsName);
        when(webServer.getHost()).thenReturn(wsHostName);
        when(webServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);

        final Identifier<WebServer> webServerId = mock(Identifier.class);
        final WebServerControlOperation controlOperation = WebServerControlOperation.START;
        final ClientHttpResponse mockClientHttpResponse = mock(ClientHttpResponse.class);

        when(controlWebServerRequest.getWebServerId()).thenReturn(webServerId);
        when(controlWebServerRequest.getControlOperation()).thenReturn(controlOperation);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Start succeeded", ""));

        webServerControlService.controlWebServer(controlWebServerRequest, user);

        verify(remoteCommandExecutorService, times(1)).executeCommand(any(RemoteExecCommand.class));
    }

    @Test
    public void testStart() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(webserver);
        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "SUCCEEDED", ""));
        ControlWebServerRequest controlWSRequest = new ControlWebServerRequest(webServerIdentifier, WebServerControlOperation.START);
        webServerControlService.controlWebServer(controlWSRequest, user);
        verify(mockMessagingService).send(any(CurrentState.class));

        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_PROCESS_KILLED, "", "PROCESS KILLED"));
        CommandOutput returnOutput = webServerControlService.controlWebServer(controlWSRequest, user);
        assertEquals("FORCED STOPPED", returnOutput.getStandardOutput());
        verify(webServerService).updateState(any(Identifier.class), eq(WebServerReachableState.FORCED_STOPPED), eq(""));
        verify(mockMessagingService, times(2)).send(any(CurrentState.class));
        reset(mockMessagingService);

        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS, "", "ABNORMAL SUCCESS"));
        webServerControlService.controlWebServer(controlWSRequest, user);
        verify(mockMessagingService).send(any(CurrentState.class));
        reset(mockMessagingService);

        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(1, "", "ABNORMAL SUCCESS"));
        webServerControlService.controlWebServer(controlWSRequest, user);
        verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), eq(EventType.APPLICATION_ERROR), anyString());
        verify(mockMessagingService, times(2)).send(any(CurrentState.class));
        reset(mockMessagingService);

        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Delete service succeeded", ""));
        webServerControlService.controlWebServer(new ControlWebServerRequest(webServerIdentifier, WebServerControlOperation.DELETE_SERVICE), user);
        verify(mockMessagingService).send(any(CurrentState.class));
    }

    @Test
    public void testSecureCopy() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        CommandOutput successReturnOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successReturnOutput);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist", ""));
        CommandOutput returnOutput = webServerControlService.secureCopyFile("testWebServer", "./source", "./dest", "user-id");
        assertEquals(new ExecReturnCode(0), returnOutput.getReturnCode());
    }

    @Test
    public void testSecureCopyPerformsBackup() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Secure copy succeeded", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File does exist", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.BACK_UP_CONFIG_FILE), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Back up succeeded", ""));
        webServerControlService.secureCopyFile("testWebServer", "./source", "./dest", "user-id");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.SECURE_COPY), any(WindowsWebServerPlatformCommandProvider.class), anyString(), anyString());
    }

    @Test
    public void testSecureCopyFailsBackup() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Secure copy succeeded", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File does exist", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.BACK_UP_CONFIG_FILE), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Back up failed"));
        webServerControlService.secureCopyFile("testWebServer", "./source", "./dest", "user-id");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.SECURE_COPY), any(WindowsWebServerPlatformCommandProvider.class), anyString(), anyString());
    }

    @Test
    public void testChangeFileMode() throws CommandFailureException {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("test-ws");
        when(mockWebServer.getHost()).thenReturn("test-host");
        webServerControlService.changeFileMode(mockWebServer, "777", "./target", "*");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHANGE_FILE_MODE), any(WindowsWebServerPlatformCommandProvider.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testCreateDirectory() throws CommandFailureException {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("test-ws");
        when(mockWebServer.getHost()).thenReturn("test-host");
        webServerControlService.createDirectory(mockWebServer, "./target");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CREATE_DIRECTORY), any(WindowsWebServerPlatformCommandProvider.class), anyString());
    }
}