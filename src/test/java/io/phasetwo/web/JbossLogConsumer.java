package io.phasetwo.web;

import org.jboss.logging.Logger;
import org.testcontainers.containers.output.OutputFrame;

import java.util.function.Consumer;

public class JbossLogConsumer implements Consumer<OutputFrame> {
    private final Logger log;

    public JbossLogConsumer(Logger log) {
        this.log = log;
    }

    @Override
    public void accept(OutputFrame outputFrame) {
        String utf8String = outputFrame.getUtf8String().trim();
        if (!utf8String.isEmpty()) {
            log.info(utf8String);
        }
    }
}
