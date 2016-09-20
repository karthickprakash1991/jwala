package com.siemens.cto.aem.service.fault;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import org.springframework.integration.MessageHandlingException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * Used to map exceptions to AemFaultTypes in 
 * certain situations.
 *
 */
public class AemExceptionMapping {

    private AemExceptionMapping() {

    }

    /**
     * Map exceptions to AemFaultTypes.
     * 
     * @param penultimateRootCause source exception
     * @return null if not known, otherwise an AemFaultType
     */
    public static AemFaultType mapGenericFaultTypesForRemoteConnections(Throwable penultimateRootCause) {
        if(penultimateRootCause instanceof JSchException) {
            JSchException x = (JSchException)penultimateRootCause;
            if(x.getCause() instanceof ConnectException) {
                ConnectException cnx = (ConnectException)x.getCause();
                if("Connection timed out: connect".equals(cnx.getMessage())) {
                    return AemFaultType.CANNOT_CONNECT;
                 } else if("Connection refused: connect".equals(cnx.getMessage())) {
                     return AemFaultType.CANNOT_CONNECT;
                 }
            }
        } else if(penultimateRootCause instanceof ResourceAccessException) {
            ResourceAccessException x = (ResourceAccessException) penultimateRootCause;
            if(x.getCause() instanceof ConnectException) {
                ConnectException cnx = (ConnectException)x.getCause();
                if("Connection timed out: connect".equals(cnx.getMessage())) {
                    return AemFaultType.CANNOT_CONNECT;
                 } else if("Connection refused: connect".equals(cnx.getMessage())) {
                     return AemFaultType.CANNOT_CONNECT;
                 }
            } else if(x.getCause() instanceof SocketTimeoutException) {
                SocketTimeoutException stx = (SocketTimeoutException)x.getCause();
                if("connect timed out".equals(stx.getMessage())) {
                    return AemFaultType.CANNOT_CONNECT;                    
                }
            }
        } else if(penultimateRootCause instanceof MessageHandlingException) {
            MessageHandlingException x = (MessageHandlingException) penultimateRootCause;
            if(x.getCause() instanceof HttpClientErrorException) {
                // Could test for specific errors: if("404 Not Found".equals(httpx.getMessage())) {
               return AemFaultType.INVALID_STATUS_PATH;
                // Could get the requested URI from the headers: httpx.getResponseHeaders().getLocation();
            }
        }  
        return null;
    }
}