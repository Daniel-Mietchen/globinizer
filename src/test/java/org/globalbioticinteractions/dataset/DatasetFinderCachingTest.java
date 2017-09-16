package org.globalbioticinteractions.dataset;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetImpl;
import org.globalbioticinteractions.cache.Cache;
import org.globalbioticinteractions.cache.CacheLog;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.cache.CachedURI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DatasetFinderCachingTest {

    private static final String cachePath = "target/cache-test";

    @Before
    @After
    public void mkdirs() {
        FileUtils.deleteQuietly(new File(cachePath));
    }

    @Test
    public void cacheDatasetLocal() throws DatasetFinderException, IOException, URISyntaxException {
        Dataset datasetCached = datasetCached();

        assertNotNull(datasetCached.getArchiveURI());
        URI uri = datasetCached.getResourceURI("globi.json");
        assertThat(uri.isAbsolute(), is(true));
        assertThat(uri.toString(), startsWith("jar:file:"));

        InputStream is = datasetCached.getResource("globi.json");
        assertNotNull(is);
        JsonNode jsonNode = new ObjectMapper().readTree(is);
        assertThat(jsonNode.has("citation"), is(true));


        String[] list = new File(cachePath + "/some/namespace").list();
        assertThat(list.length, is(3));
        assertThat(Arrays.asList(list), hasItem("1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80"));
        assertThat(Arrays.asList(list), hasItem("c9ecb3b0100c890bd00a5c201d06f0a78d92488591f726fbf4de5c88bda39147"));
        assertThat(Arrays.asList(list), hasItem("access.tsv"));
    }


    private Dataset datasetCached() throws IOException, URISyntaxException {
        Dataset dataset = new DatasetImpl("some/namespace", getClass().getResource("archive.zip").toURI());
        Cache cache = CacheUtil.cacheFor("some/namespace", cachePath);
        return new DatasetWithCache(dataset, cache);
    }
}
