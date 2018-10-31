package org.starichkov.java.cache2k;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.integration.FunctionalCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Vadim Starichkov
 * @since 31.10.2018 17:08
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger("logger");

    public static void main(String[] args) throws InterruptedException {
        new Main().run(args);
    }

    private void run(String[] args) throws InterruptedException {
        boolean enableReload = false;
        if (args.length > 0) {
            enableReload = Boolean.valueOf(args[0]);
        }

        final List<String> keys = Arrays.asList("a", "b", "c");
        final List<String> values = Arrays.asList("d", "e", "f");

        Cache2kBuilder<String, String> cache2kBuilder = Cache2kBuilder.of(String.class, String.class)
                .name("cache2k_example")
                .expireAfterWrite(3, TimeUnit.SECONDS);

        if (enableReload) {
            cache2kBuilder
                    .loader(createCacheLoader(keys, values))
                    .refreshAhead(true);
        }

        Cache<String, String> cache = cache2kBuilder.build();

        if (!enableReload) {
            LOGGER.info("Reloading disabled, manually populating cache...");
            for (int i = 0; i < keys.size(); i++) {
                cache.put(keys.get(i), values.get(i));
            }
        }

        LOGGER.info("Cache initialized, let's check what's in cache!");
        printCacheValues(cache, keys);

        LOGGER.info("Sleep for 5 seconds giving our cache time to expire...");
        Thread.sleep(5000);

        LOGGER.info("Woke up, let's check what's in cache!");
        printCacheValues(cache, keys);
    }

    private void printCacheValues(Cache<String, String> cache, List<String> keys) {
        for (String key : keys) {
            LOGGER.info("Value for key '{}': '{}'", key, cache.get(key));
        }
    }

    private FunctionalCacheLoader<String, String> createCacheLoader(List<String> keys, List<String> values) {
        return key -> {
            LOGGER.info("Loading value for key '{}'...", key);
            if (keys.contains(key)) {
                return values.get(keys.indexOf(key));
            }
            return null;
        };
    }
}
