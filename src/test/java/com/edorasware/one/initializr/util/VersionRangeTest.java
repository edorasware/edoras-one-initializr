package com.edorasware.one.initializr.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class VersionRangeTest {

    private String S100 = "1.0.0";
    private String S101 = "1.0.1";
    private String S110 = "1.1.0";
    private String S200 = "2.0.0";
    private String S100M1 = "1.0.0-M1";
    private String S100M2 = "1.0.0-M2";
    private String S100_1 = "1.0.0-1";
    private String S10x = "1.0.x";
    private String S1xx = "1.x.x";

    private String LOW_INC = "[";
    private String LOW_EXC = "(";
    private String HIGH_INC = "]";
    private String HIGH_EXC = ")";

    private String SEPARATOR = ",";

    @Test
    public void matchPatchHighExc() throws Exception {
        VersionRange versionRange = VersionParser.DEFAULT.parseRange(LOW_INC+S100+SEPARATOR+S101+HIGH_EXC);
        assertTrue(versionRange.match(Version.parse(S100)));
        assertTrue(!versionRange.match(Version.parse(S101)));
    }

    @Test
    public void matchPatchHighInc() throws Exception {
        VersionRange versionRange = VersionParser.DEFAULT.parseRange(LOW_INC+S100+SEPARATOR+S101+HIGH_INC);
        assertTrue(versionRange.match(Version.parse(S100)));
        assertTrue(versionRange.match(Version.parse(S101)));
    }

    @Test
    public void matchMinorPatchUndefined() throws Exception {
        VersionRange versionRange = VersionParser.DEFAULT.parseRange(LOW_INC+S100+SEPARATOR+S1xx+HIGH_INC);
        assertTrue(versionRange.match(Version.parse(S100)));
        assertTrue(versionRange.match(Version.parse(S101)));
        assertTrue(versionRange.match(Version.parse(S110)));
        assertTrue(versionRange.match(Version.parse(S100_1)));
    }

    @Test
    public void matchSepc() throws Exception {
        VersionRange versionRange = VersionParser.DEFAULT.parseRange(LOW_INC+"1.6.0"+SEPARATOR+"1.6.x"+HIGH_INC);
        assertTrue(versionRange.match(Version.parse("1.6.9")));
        assertTrue(versionRange.match(Version.parse("1.6.9-1")));
    }


}