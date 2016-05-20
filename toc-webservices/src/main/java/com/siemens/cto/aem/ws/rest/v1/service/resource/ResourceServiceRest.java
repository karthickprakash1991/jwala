package com.siemens.cto.aem.ws.rest.v1.service.resource;

import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.service.resource.impl.JsonResourceInstance;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public interface ResourceServiceRest {

    /**
     * /aem/v1.0/resources/types
     *
     * @return a list of resourceTypes from the file system
     */
    @GET
    @Path("/types")
    Response getTypes();

    /**
     * /aem/v1.0/resources;groupName=[your group name]
     *
     * @param groupName the name of the previously created group
     * @return a list of ResourceInstance objects associated with a group
     */
    @GET
    Response findResourceInstanceByGroup(@MatrixParam("groupName") final String groupName);

    /**
     * /aem/v1.0/resources/[your resource instance name];groupName=[your group name]
     *
     * @param name the name of an existing resource instance
     * @param groupName the name of an existing group
     * @return a specific resourceInstance object if present
     */
    @GET
    @Path("/{name}")
    Response findResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName);

    @GET
    @Path("/{name}/preview")
    Response generateResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName);

    /**
     * /aem/v1.0/resources <br/>
     * JSON POST data of JsonResourceInstance
     * @param aResourceInstanceToCreate {@link JsonResourceInstance}
     * @param aUser the authenticated user who is creating the ResourceInstance
     * @return the newly created ResourceInstance object
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createResourceInstance(final JsonResourceInstance aResourceInstanceToCreate, @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name] <br/>
     * JSON PUT conttaining the same object as create, but empty attributes will remain the same and it will detect changes in the name within the JsonResourceInstance object
     * @param name the name of an existing resource instance for updating
     * @param groupName the name of an existing group which is associcated with the resource instance to be updated.
     * @param aUser the authenticated user who is updating the resource instance
     * @return the updated ResourceInstance object
     */
    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateResourceInstanceAttributes(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, final JsonResourceInstance aResourceInstanceToUpdate, @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name]
     * @param name the name of a the existing ResourceInstance to be deleted
     * @param groupName the group name of the resource instance to be deleted
     * @return  If successful nothing.
     */
    @DELETE
    @Path("/{name}")
    Response removeResourceInstance(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName);

    /**
     * Removes a list of resources.
     *
     * usage: /aem/v1.0/resources;groupName=[group name];resourceName=[resourceName1];resourceName=[resourceName2]
     *
     * @param groupName the group where the resources to be removed belong to.
     * @param resourceNames the names of the resources to remove.
     *
     * @return {@link Response}
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    Response removeResources(@MatrixParam("groupName") final String groupName,
                             @MatrixParam("resourceName") final List<String> resourceNames);

    @GET
    @Path("/types/{resourceTypeName}/template")
    Response getTemplate(@PathParam("resourceTypeName") final String resourceTypeName);

    /**
     * Creates a template file and it's corresponding JSON meta data file.
     * A template file is used when generating the actual resource file what will be deployed to a JVM or web server.
     * @param attachments contains the template's meta data and main content
     * @param user a logged in user who's calling this service
     * @return {@link Response}
     */
    @POST
    @Path("/template/{targetName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response createTemplate(List<Attachment> attachments, @PathParam("targetName") final String targetName, @BeanParam AuthenticatedUser user);

    /**
     * Deletes a resource template.
     * @param name the template name (the actual name of the resource file when deployed e.g. context.xml)
     * @return {@link Response} which contains the number of records deleted.
     */
    @DELETE
    @Path("/template/{name}")
    Response removeTemplate(@PathParam("name") String name);

    @GET
    @Path("/data")
    Response getResourceAttrData();

    /**
     * Gets the resource data topology.
     * @return resource JSON data topology wrapped by {@link Response}.
     */
    @GET
    @Path("/topology")
    Response getResourceTopology();
}
