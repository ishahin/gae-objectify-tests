package com.mongermethod.gae_objectify;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

public abstract class TestBootstrap {
    private final LocalServiceTestHelper helper =  new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @BeforeTest
    public void setup() {
        helper.setUp();
    }

    @AfterTest
    public void teardown() {
        helper.tearDown();
    }
}
