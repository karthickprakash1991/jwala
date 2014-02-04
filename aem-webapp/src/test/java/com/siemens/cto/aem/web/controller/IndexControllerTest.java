package com.siemens.cto.aem.web.controller;

import junit.framework.TestCase;

public class IndexControllerTest extends TestCase {
    final IndexController ic = new IndexController();

    public void testIndex() {
        assertEquals("aem/index", ic.index());
    }

    public void testAbout() {
        assertEquals("aem/about", ic.about());
    }

    public void testReactIndex() {
        assertEquals("aem/index-react", ic.reactIndex());
    }

    public void testAngularIndex() {
        assertEquals("aem/index-angular", ic.angularIndex());
    }
}
