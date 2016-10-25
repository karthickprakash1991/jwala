package com.cerner.jwala.files.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cerner.jwala.files.FilesConfiguration;
import com.cerner.jwala.files.RepositoryFileInformation;
import com.cerner.jwala.files.RepositoryService;
import com.cerner.jwala.files.JwalaPath;
import com.cerner.jwala.files.RepositoryFileInformation.Type;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

public class LocalFileSystemRepositoryServiceImpl implements RepositoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileSystemRepositoryServiceImpl.class);

    @Autowired
    FilesConfiguration filesConfiguration;
    
    @Override
    public RepositoryFileInformation writeStream(JwalaPath refPlace, Path partialPath, InputStream transientData, RepositoryFileInformation... relatedHistory) throws IOException {
        Path place = filesConfiguration.getConfiguredPath(refPlace);
        Path resolvedPath = place.resolve(partialPath).toAbsolutePath().normalize();
        long copied = 0;
        try(
                FileChannel out = FileChannel.open(resolvedPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
                ReadableByteChannel in = Channels.newChannel(transientData);   
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
              buffer.flip();
              int segment = out.write(buffer);
              if(segment != len) {
                  throw new IOException("Write failed");
              }
              copied += len;
              buffer.clear();
            }
        }        
        return RepositoryFileInformation.stored(resolvedPath, (Long) copied, relatedHistory);
        
    }

    @Override
    public RepositoryFileInformation deleteIfExisting(JwalaPath refPlace, Path partialPath, RepositoryFileInformation... relatedHistory) throws IOException {
        RepositoryFileInformation res1 = find(refPlace, partialPath, relatedHistory);
        if(res1.getType() == Type.FOUND) {
            Files.delete(res1.getPath());
            return RepositoryFileInformation.deleted(res1.getPath(), res1);
        } else {
            return res1;
        }
    }

    @Override
    public RepositoryFileInformation find(JwalaPath refPlace, Path partialPath, RepositoryFileInformation... relatedHistory) throws IOException {
        Path place = filesConfiguration.getConfiguredPath(refPlace);
        Path resolvedPath = place.resolve(partialPath);
        if(Files.exists(resolvedPath)) {
            return RepositoryFileInformation.found(resolvedPath, relatedHistory);
        }
        return RepositoryFileInformation.none(relatedHistory);
    }

    @Override
    public RepositoryFileInformation findAll(JwalaPath refPlace, String pattern, RepositoryFileInformation... relatedHistory) throws IOException {
        Path place = filesConfiguration.getConfiguredPath(refPlace);
        List<Path> results = new LinkedList<Path>();

        try(DirectoryStream<Path> directory = Files.newDirectoryStream(place, pattern)) {
            
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Inspecting path '"+place.toAbsolutePath().toString()+"' for children matching '"+ pattern +"'");
            }
            
            for(Path p : directory) { 

                LOGGER.debug("Found partialPath: '"+p.toAbsolutePath().toString()+"'");
                results.add(p); 
            }
                
            if(!results.isEmpty()) {
                return RepositoryFileInformation.found(results, relatedHistory);
            }
        }
        return RepositoryFileInformation.none(relatedHistory);
    }
}
