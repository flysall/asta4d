package com.astamuse.asta4d.web.test.unit;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.astamuse.asta4d.web.util.timeout.DefaultSessionAwareTimeoutDataManager;
import com.astamuse.asta4d.web.util.timeout.TooManyDataException;

@Test
public class DefaultSessionAwaretimeoutDataManagerTest {

    private static class MockedSessionIdManager extends DefaultSessionAwareTimeoutDataManager {

        String mockedSessionId = null;

        @Override
        protected String retrieveSessionId(boolean create) {
            return mockedSessionId;
        }

    }

    public void testNormalPutAndGet() {
        MockedSessionIdManager manager = new MockedSessionIdManager();
        manager.start();

        manager.mockedSessionId = "s1";
        Object data = new Object();
        manager.put("d1", data, 1000 * 1000);// would never timeout in this test

        Assert.assertSame(manager.get("d1"), data);
        // could not retrieve it any more
        Assert.assertNull(manager.get("d1"));

        manager.stop();

    }

    public void testSessionCheck() {
        MockedSessionIdManager manager = new MockedSessionIdManager();
        manager.start();
        Object data = new Object();

        manager.mockedSessionId = "s1";
        manager.put("d1", data, 1000 * 1000);// would never timeout in this test

        manager.mockedSessionId = "s2";
        // could not retrieve it
        Assert.assertNull(manager.get("d1"));

        manager.mockedSessionId = "s1";
        // since the data has been retrieved once by wrong session id, the data would not exist anymore
        Assert.assertNull(manager.get("d1"));

        manager.stop();

    }

    public void testTimeoutCheck() throws Exception {
        MockedSessionIdManager manager = new MockedSessionIdManager();
        manager.mockedSessionId = "s1";
        manager.setExpirationCheckPeriodInMilliseconds(50);
        manager.start();

        Object data = new Object();

        manager.put("d1", data, 100);// would be timeout in 100 ms

        // could not retrieve it any more after 200 ms sleep
        Thread.sleep(200);
        Assert.assertNull(manager.get("d1"));

        manager.stop();

    }

    @Test(expectedExceptions = TooManyDataException.class)
    public void testMaxDataSizeCheck() {
        MockedSessionIdManager manager = new MockedSessionIdManager();
        manager.mockedSessionId = "s1";
        manager.setMaxDataSize(1);
        manager.setSpinTimeInMilliseconds(50);
        manager.setMaxSpinTimeInMilliseconds(50);
        manager.start();

        try {
            Object data = new Object();
            manager.put("d1", data, 1000 * 1000);// would never be timeout in this test
            manager.put("d2", data, 1000 * 1000);// would never be timeout in this test
        } finally {
            manager.stop();
        }
    }

    public void testMaxDataSizeCheck2() {
        MockedSessionIdManager manager = new MockedSessionIdManager();
        manager.mockedSessionId = "s1";
        manager.setMaxDataSize(1);
        manager.setSpinTimeInMilliseconds(50);
        manager.setMaxSpinTimeInMilliseconds(200);
        manager.setExpirationCheckPeriodInMilliseconds(50);
        manager.start();

        try {
            Object data = new Object();
            Object data2 = new Object();
            manager.put("d1", data, 100);// would be timeout
            manager.put("d2", data2, 1000 * 1000);// would never be timeout in this test

            Assert.assertSame(manager.get("d2"), data2);
        } finally {
            manager.stop();
        }
    }
}
