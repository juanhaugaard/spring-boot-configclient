/*
 * Copyright (c) 2017. Dovel Technologies and Digital Infuzion.
 */

package gov.pmm.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * A class that creates a pipe by wrapping an InputStream connected to an OutputStream
 *
 * @author juan.haugaard on 1/18/2017.
 */
public class Pipe {
    private static final int DEFAULT_PIPE_SIZE = 1024;
    private PipedOutputStream outputStream;
    private PipedInputStream inputStream;

    /**
     * Pipe Default constructor, uses a default pipe size of 1k bytes
     */
    public Pipe() {
        this(DEFAULT_PIPE_SIZE);
    }

    /**
     * Pipe constructor
     *
     * @param pipeSize pipe size in bytes
     */
    public Pipe(int pipeSize) {
        try {
            inputStream = new PipedInputStream(pipeSize);
            outputStream = new PipedOutputStream(inputStream);
        } catch (IOException e) {
            // this only happens if output stream is already connected
            throw new IllegalStateException("Panic -- should not happen", e);
        }
    }

    /**
     * Access the input stream side of the pipe
     *
     * @return input stream side of the pipe
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Access the output stream side of the pipe
     *
     * @return output stream side of the pipe
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }
}
