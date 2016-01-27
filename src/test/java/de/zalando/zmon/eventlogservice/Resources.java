package de.zalando.zmon.eventlogservice;

import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

/**
 * 
 * @author jbellmann
 *
 */
public interface Resources {

    default Resource jsonResource(String filename) {
        return new ClassPathResource(filename + ".json", getClass());
    }

    default String resourceToString(Resource resource) throws IOException {
        return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
    }

}
