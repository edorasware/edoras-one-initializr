package com.edorasware.one.initializr.util;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class VersionParserTest {

    private List<String> testVersionStrings = new ArrayList<>();

    @Before
    public void initTest() {
        testVersionStrings.add("2.0.0");
        testVersionStrings.add("2.0.0-1");
        testVersionStrings.add("2.0.0-SNAPSHOT");
        testVersionStrings.add("2.0.0-M6");
        testVersionStrings.add("2.0.0-M6-SNAPSHOT");
        testVersionStrings.add("2.0.0-M6-1");
        testVersionStrings.add("2.0.0-M6-1-SNAPSHOT");

        testVersionStrings.add("2.x.0");
        testVersionStrings.add("2.x.0-1");
        testVersionStrings.add("2.x.0-SNAPSHOT");
        testVersionStrings.add("2.x.0-M6");
        testVersionStrings.add("2.x.0-M6-SNAPSHOT");
        testVersionStrings.add("2.x.0-M6-1");
        testVersionStrings.add("2.x.0-M6-1-SNAPSHOT");

        testVersionStrings.add("2.0.x");
        testVersionStrings.add("2.0.x-1");
        testVersionStrings.add("2.0.x-SNAPSHOT");
        testVersionStrings.add("2.0.x-M6");
        testVersionStrings.add("2.0.x-M6-SNAPSHOT");
        testVersionStrings.add("2.0.x-M6-1");
        testVersionStrings.add("2.0.x-M6-1-SNAPSHOT");

        testVersionStrings.add("2.x.x");
        testVersionStrings.add("2.x.x-1");
        testVersionStrings.add("2.x.x-SNAPSHOT");
        testVersionStrings.add("2.x.x-M6");
        testVersionStrings.add("2.x.x-M6-SNAPSHOT");
        testVersionStrings.add("2.x.x-M6-1");
        testVersionStrings.add("2.x.x-M6-1-SNAPSHOT");
    }

    private String S100 = "1.0.0";
    private String S101 = "1.0.1";
    private String S110 = "1.1.0";
    private String S200 = "2.0.0";
    private String S100M1 = "1.0.0-M1";
    private String S100M2 = "1.0.0-M2";
    private String S100_1 = "1.0.0-1";
    private String S10x = "1.0.x";


    @Test
    public void parseVersions() throws Exception {
        VersionParser versionParser = VersionParser.DEFAULT;
        for (String testVersionString : testVersionStrings) {
            Version version = versionParser.parse(testVersionString);

            assertTrue(version.getMajor().equals(2));
            assertTrue(version.getMinor().equals(0) || version.getMinor().equals(999));
            assertTrue(version.getPatch().equals(0) || version.getPatch().equals(999));
            if (version.getQualifier() != null && !version.getQualifier().getQualifier().isEmpty()) {
                assertTrue(version.getQualifier().getQualifier().equals("M"));
                assertTrue(version.getQualifier().getVersion().equals(6) || version.getQualifier().getVersion().equals(999));
            }
        }
    }

    @Test
    public void comparePatch() throws Exception {
        Version lower = Version.parse(S100);
        Version higher = Version.parse(S101);
        assertTrue(higher.isHigherThan(lower));
        assertTrue(!lower.isHigherThan(higher));
    }

    @Test
    public void comparePatchUndefined() throws Exception {
        Version lower = Version.parse(S100);
        Version higher = Version.parse(S10x);
        assertTrue(higher.isHigherThan(lower));
        assertTrue(!lower.isHigherThan(higher));
    }

    @Test
    public void compareMinor() throws Exception {
        Version lower = Version.parse(S100);
        Version higher = Version.parse(S110);
        assertTrue(higher.isHigherThan(lower));
        assertTrue(!lower.isHigherThan(higher));
    }

    @Test
    public void compareMajor() throws Exception {
        Version lower = Version.parse(S100);
        Version higher = Version.parse(S200);
        assertTrue(higher.isHigherThan(lower));
        assertTrue(!lower.isHigherThan(higher));
    }

    @Test
    public void compareQualifier() throws Exception {
        Version lower = Version.parse(S100M1);
        Version higher = Version.parse(S100M2);
        assertTrue(higher.isHigherThan(lower));
        assertTrue(!lower.isHigherThan(higher));
    }

    @Test
    public void compareMajorQualifier() throws Exception {
        Version lower = Version.parse(S100M1);
        Version higher = Version.parse(S100);
        assertTrue(higher.isHigherThan(lower));
        assertTrue(!lower.isHigherThan(higher));
    }

//    @Test
//    public void compareMajorStarter() throws Exception {
//        Version lower = Version.parse(S100);
//        Version higher = Version.parse(S100_1);
//        assertTrue(higher.isHigherThan(lower));
//        assertTrue(!lower.isHigherThan(higher));
//    }
}