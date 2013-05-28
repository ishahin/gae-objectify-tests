package com.mongermethod.gae_objectify;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.memcache.Stats;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.cache.CachingDatastoreServiceFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.testng.Assert.*;

public class ObjectifyTest extends TestBootstrap {

    @BeforeTest
    public void registerEntities() {
        ObjectifyService.register(CacheableEntity.class);
        ObjectifyService.register(NonCacheableEntity.class);
    }

    @BeforeMethod
    public void clearTheCache() {
        final MemcacheService memcacheService = getCache();
        memcacheService.clearAll();
    }


    @Test(description = "ensure that an entity annotated with @Cache gets stored in the cache")
    public void cachedKeyTest() {
        final int hits = 1;
        final int misses = 0;
        final String id = "user123";
        final String name = "John Doe";
        final int age = 23;
        final CacheableEntity cacheableEntity = new CacheableEntity(id, name, age);

        // get cache service
        final MemcacheService memcacheService = getCache();

        // persist our entity
        final Key<CacheableEntity> cacheableKey = ofy().save().entity(cacheableEntity).now();

        // the key to the cacheable entity is in the cache
        assertTrue(memcacheService.contains(cacheableKey.getString()));

        // the memcache stats will show one hit and zero misses
        final Stats stats = memcacheService.getStatistics();
        assertEquals(stats.getHitCount(), hits);
        assertEquals(stats.getMissCount(), misses);
    }

    @Test(description = "ensure that an entity without @Cache will not get stored in the cache")
    public void nonCachedKeyTest() {
        final int hits = 0;
        final int misses = 1;
        final String id = "user321";
        final String name = "Jane Doe";
        final int age = 24;
        final NonCacheableEntity nonCacheableEntity = new NonCacheableEntity(id, name, age);

        // get cache service
        final MemcacheService memcacheService = getCache();

        // persist our entity
        final Key<NonCacheableEntity> nonCacheableKey = ofy().save().entity(nonCacheableEntity).now();

        // the key to the non-cacheable entity is not in the cache
        assertFalse(memcacheService.contains(nonCacheableKey.getString()));

        // the memcache stats will show zero hits and one miss
        final Stats stats = memcacheService.getStatistics();
        assertEquals(stats.getHitCount(), hits);
        assertEquals(stats.getMissCount(), misses);
    }

    @Test(description = "ensure that objectify pulls from the cache and doesn't hit the datastore")
    public void getFromCacheTest() {
        Stats stats;
        final int initialItemTotal = 0;
        final int afterSaveItemTotal = 1;
        final int hits = 1;
        final String id = "user890";
        final String name = "James Doe";
        final int age = 35;
        final CacheableEntity cacheableEntity = new CacheableEntity(id, name, age);

        // get cache service
        final MemcacheService memcacheService = getCache();
        stats = memcacheService.getStatistics();
        assertEquals(stats.getItemCount(), initialItemTotal);

        // persist our entity
        final Key<CacheableEntity> cacheableKey = ofy().save().entity(cacheableEntity).now();
        stats = memcacheService.getStatistics();
        assertEquals(stats.getItemCount(), afterSaveItemTotal);

        // load the entity via Objectify
        final CacheableEntity loadedEntity = ofy().load().key(cacheableKey).now();
        stats = memcacheService.getStatistics();

        // expect the cache hit count to be 1 (because loadedEntity was loaded from the cache) -- FAILS
        assertEquals(stats.getHitCount(), hits);

        // the entity key is in the cache
        assertTrue(memcacheService.contains(cacheableKey.getString()));

        // expect the cached entity to not be null -- FAILS
        final Object result = memcacheService.get(cacheableKey.getString());
        assertNotNull(result);
    }

    private MemcacheService getCache() {
        return MemcacheServiceFactory.getMemcacheService(CachingDatastoreServiceFactory.getDefaultMemcacheNamespace());
    }

    @Cache
    @Entity
    private static class CacheableEntity {
        @Id
        private String id;
        private String name;
        private int age;

        public CacheableEntity() {}
        public CacheableEntity(String id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    @Entity
    private static class NonCacheableEntity {
        @Id
        private String id;
        private String name;
        private int age;

        public NonCacheableEntity() {}
        public NonCacheableEntity(String id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
