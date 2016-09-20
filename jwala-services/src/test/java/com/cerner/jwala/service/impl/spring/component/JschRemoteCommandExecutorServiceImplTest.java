package com.cerner.jwala.service.impl.spring.component;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.impl.spring.component.JschRemoteCommandExecutorServiceImpl;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * Tests for {@link JschRemoteCommandExecutorServiceImpl}.
 *
 * Created by JC043760 on 4/18/2016.
 */
public class JschRemoteCommandExecutorServiceImplTest {

    private static final String SOME_OUTPUT = "some output";
    private static final String SOME_ERR_OUTPUT = "some err output";

    private JschRemoteCommandExecutorServiceImpl jschRemoteCommandExecutorService;

    @Mock
    private JSch mockJSch;

    @Mock
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> mockChannelPool;

    @Mock
    private RemoteExecCommand mockRemoteExecCommand;

    @Mock
    private Session mockSession;

    @Mock
    private RemoteSystemConnection mockRemoteSystemConnection;

    @Mock
    private ChannelExec mockChannelExec;

    @Mock
    private ExecCommand mockExecCommand;

    final RemoteSystemConnection remoteSystemConnection = new RemoteSystemConnection("theUser", "thePassword", "theHost", 999);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.jschRemoteCommandExecutorService = new JschRemoteCommandExecutorServiceImpl(mockJSch, mockChannelPool);
    }

    @Test
    public void testExecuteShellCommand() throws Exception {
        final ChannelShell mockChannelShell = mock(ChannelShell.class);
        final byte [] bytes = "EXIT_CODE=0*** ".getBytes();
        bytes[14] = (byte) 0xff;
        when(mockChannelShell.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        when(mockChannelShell.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockChannelShell.isConnected()).thenReturn(true);
        when(mockChannelPool.borrowObject(any(ChannelSessionKey.class))).thenReturn(mockChannelShell);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(remoteSystemConnection);
        final ExecCommand mockExecCommand = mock(ExecCommand.class);
        when(mockExecCommand.toCommandString()).thenReturn("some-command.sh");
        when(mockExecCommand.getRunInShell()).thenReturn(true);
        when(mockRemoteExecCommand.getCommand()).thenReturn(mockExecCommand);
        final RemoteCommandReturnInfo remoteCommandReturnInfo = jschRemoteCommandExecutorService.executeCommand(mockRemoteExecCommand);
        assertEquals(0, remoteCommandReturnInfo.retCode);
    }

    @Test
    public void testExecuteExecCommand() throws Exception {
        when(mockJSch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(mockRemoteSystemConnection);
        when(mockSession.openChannel(eq(ChannelType.EXEC.getChannelType()))).thenReturn(mockChannelExec);

        final byte [] output = SOME_OUTPUT.getBytes();
        final InputStream remoteOutput = new ByteArrayInputStream(output);

        final byte [] err = SOME_ERR_OUTPUT.getBytes();
        final InputStream remoteError = new ByteArrayInputStream(err);

        when(mockChannelExec.getInputStream()).thenReturn(remoteOutput);
        when(mockChannelExec.getErrStream()).thenReturn(remoteError);
        when(mockChannelExec.getExitStatus()).thenReturn(1);
        when(mockRemoteExecCommand.getCommand()).thenReturn(mockExecCommand);
        when(mockExecCommand.toCommandString()).thenReturn("sc query something");
        final RemoteCommandReturnInfo returnInfo = this.jschRemoteCommandExecutorService.executeCommand(mockRemoteExecCommand);

        assertEquals("some output", returnInfo.standardOuput);
        assertEquals("some err output", returnInfo.errorOupout);
    }
}