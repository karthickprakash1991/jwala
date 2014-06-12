package com.siemens.cto.aem.service.app.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UploadWebArchiveCommand;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.WebArchiveManager;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {


    /*Mock by hand*/
    private ApplicationDao applicationDao;

    /*Mock by hand*/
    private ApplicationPersistenceService applicationPersistenceService;
    
    @Mock /*injected*/
    private WebArchiveManager webArchiveManager;

    @Mock /*injected*/
    private PrivateApplicationService privateApplicationService = new PrivateApplicationServiceImpl();

    @InjectMocks @Spy
    private ApplicationService applicationService = new ApplicationServiceImpl(
            applicationDao = Mockito.mock(ApplicationDao.class),
            applicationPersistenceService = Mockito.mock(ApplicationPersistenceService.class)            
            );
    
    @Mock
    private Application mockApplication; 
    @Mock
    private Application mockApplication2; 
        
    private Group group;
    private Group group2;
    private Identifier<Group> groupId;
    private Identifier<Group> groupId2;
    
    private ArrayList<Application> applications2 = new ArrayList<>(1);
    
    private User testUser = new User("testUser");

    // Managed by setup/teardown
    ByteArrayInputStream uploadedFile;
    Application app;
    
    @Before
    public void setUp() {
        
        groupId = new Identifier<Group>(1L);
        groupId2 = new Identifier<Group>(2L);
        group = new Group(groupId, "the-ws-group-name");
        group2 = new Group(groupId2, "the-ws-group-name-2");

        when(mockApplication.getId()).thenReturn(new Identifier<Application>(1L));
        when(mockApplication.getWarPath()).thenReturn("the-ws-group-name/toc-1.0.war");
        when(mockApplication.getName()).thenReturn("TOC 1.0");
        when(mockApplication.getGroup()).thenReturn(group);
        when(mockApplication.getWebAppContext()).thenReturn("/aem");
        

        when(mockApplication2.getId()).thenReturn(new Identifier<Application>(2L));
        when(mockApplication2.getWarPath()).thenReturn("the-ws-group-name-2/toc-1.1.war");
        when(mockApplication2.getName()).thenReturn("TOC 1.1");
        when(mockApplication2.getGroup()).thenReturn(group2);
        when(mockApplication2.getWebAppContext()).thenReturn("/aem");
        
        applications2.add(mockApplication);
        applications2.add(mockApplication2);
        
        ByteBuffer buf = java.nio.ByteBuffer.allocate(2); // 2 byte file
        buf.asShortBuffer().put((short)0xc0de);

        uploadedFile = new ByteArrayInputStream(buf.array());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSingleGet() {
        when(applicationDao.getApplication(any(Identifier.class))).thenReturn(mockApplication);
        final Application application = applicationService.getApplication(new Identifier<Application>(1L));
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("TOC 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/toc-1.0.war", application.getWarPath());
    }

    @Test
    public void testAllGet() {
        when(applicationDao.getApplications(any(PaginationParameter.class))).thenReturn(applications2);
        final List<Application> apps = applicationService.getApplications(PaginationParameter.all());
        assertEquals(applications2.size(), apps.size());
        
        Application application = apps.get(0);
        assertEquals(new Identifier<Application>(1L), application.getId());
        assertEquals(groupId, application.getGroup().getId());
        assertEquals("TOC 1.0", application.getName());
        assertEquals("the-ws-group-name", application.getGroup().getName());
        assertEquals("the-ws-group-name/toc-1.0.war", application.getWarPath());

        application = apps.get(1);
        assertEquals(new Identifier<Application>(2L), application.getId());
        assertEquals(groupId2, application.getGroup().getId());
        assertEquals("TOC 1.1", application.getName());
        assertEquals("the-ws-group-name-2", application.getGroup().getName());
        assertEquals("the-ws-group-name-2/toc-1.1.war", application.getWarPath());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFindByGroupId() {
        when(applicationDao.findApplicationsBelongingTo(any(Identifier.class), any(PaginationParameter.class))).thenReturn(applications2);
        final List<Application> apps = applicationService.findApplications(groupId, PaginationParameter.all());
        assertEquals(applications2.size(), apps.size());
        
        Application application = apps.get(1);

        assertEquals(new Identifier<Application>(2L), application.getId());
        assertEquals(groupId2, application.getGroup().getId());
        assertEquals("TOC 1.1", application.getName());
        assertEquals("the-ws-group-name-2", application.getGroup().getName());
        assertEquals("the-ws-group-name-2/toc-1.1.war", application.getWarPath());
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected=BadRequestException.class)
    public void testCreateBadRequest() {
        when(applicationPersistenceService.createApplication(any(Event.class))).thenReturn(mockApplication2);
        
        CreateApplicationCommand cac = new CreateApplicationCommand(Identifier.id(1L, Group.class), "", "");
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() {
        when(applicationPersistenceService.createApplication(any(Event.class))).thenReturn(mockApplication2);
        
        CreateApplicationCommand cac = new CreateApplicationCommand(Identifier.id(1L, Group.class), "wan", "/wan");
        Application created = applicationService.createApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }
    

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate() {
        when(applicationPersistenceService.updateApplication(any(Event.class))).thenReturn(mockApplication2);
        
        UpdateApplicationCommand cac = new UpdateApplicationCommand(mockApplication2.getId(), Identifier.id(1L, Group.class), "wan", "/wan");
        Application created = applicationService.updateApplication(cac, new User("user"));

        assertTrue(created == mockApplication2);
    }
    

    @SuppressWarnings("unchecked")
    @Test
    public void testRemove() {
        applicationService.removeApplication(mockApplication.getId(), testUser);

        Mockito.verify(applicationPersistenceService, Mockito.times(1)).removeApplication(Mockito.any(Identifier.class));        
    }
    
    private class IsValidUploadEvent extends ArgumentMatcher<Event<UploadWebArchiveCommand>> {

        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Object arg) {
            Event<UploadWebArchiveCommand> event = (Event<UploadWebArchiveCommand>)arg;
            UploadWebArchiveCommand uwac = event.getCommand();
            uwac.validateCommand();
            return true;
        } 
        
    }    
    
    @SuppressWarnings("unchecked")
    @Test
    public void testUploadWebArchive() throws IOException { 
        UploadWebArchiveCommand uwac = new UploadWebArchiveCommand(mockApplication, "fn.war", 2L, uploadedFile);

        when(webArchiveManager.store(any(Event.class))).thenReturn(RepositoryAction.stored(FileSystems.getDefault().getPath("D:\\fn.war"), 2L));
        
        applicationService.uploadWebArchive(uwac, testUser);

        Mockito.verify(privateApplicationService, Mockito.times(1)).uploadWebArchiveData(argThat(new IsValidUploadEvent()));        
        Mockito.verify(privateApplicationService, Mockito.times(1)).uploadWebArchiveUpdateDB(argThat(new IsValidUploadEvent()), any(RepositoryAction.class));        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteWebArchive() throws IOException { 
        when(webArchiveManager.remove(any(Event.class))).thenReturn(RepositoryAction.deleted(FileSystems.getDefault().getPath("D:\\fn.war")));
        
        applicationService.deleteWebArchive(mockApplication.getId(), testUser);

        Mockito.verify(webArchiveManager, Mockito.times(1)).remove(any(Event.class));        
        Mockito.verify(applicationPersistenceService, Mockito.times(1)).removeWARPath((any(Event.class)));        
    }
}
