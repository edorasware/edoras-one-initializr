# edoras-one-initializr [![Build Status](https://travis-ci.org/edorasware/edoras-one-initializr.svg?branch=master)](https://travis-ci.org/edorasware/edoras-one-initializr)

See https://edoras-one-initializr.cfapps.io

Using the generated code needs an edoras one license (see http://www.edorasware.com) and access to the appropriate repositories. Don't even try to get it running without.

Already working:
- Maven project and pom.xml creation based on edoras-one-starter 1.6 or 2.0.
- Gradle project and build.xml creation based on edoras-one-starter 1.6 or 2.0.
- Support for additional "Short Name" property (used for config files, tenant, etc.)
- Gradle .war and .executable .jar project generation (needs gradle 3.0+)
- Maven .war and .executable .jar project generation
- Create sample code for addons and project if requested
- Support for creating addons.
- Support for different Java versions.

Todo:
- Metadata from Repository/Addon Marketplace
- Code cleanup
- Fixing a lot of bugs
- Support for Groovy / Kotlin
- Externalize Templates and load them from GitHub

A test server with the latest head version can be found on https://edoras-one-initializr-test.cfapps.io (potentially unstable)

## Running form Command Line

Its also possible to run the initializr from a command line using the curl command e.g. like this:

```bash
curl http://edoras-one-initializr.cfapps.io/starter.tgz -d artifactId=my-app -d groupId=com.edorasware.app -d shortName=myApp -d dependencies=edoras-addon-rest-doc,edoras-addon-data-esi | tar -zvx
```

## Change Log

### 06-06-2018
- Updated version to starter 2.0.4-2
- Updated a lot of addons to their latest version
- Added a lot of new addons (maily widgets, grid, grid lite, etc.)
- Fixed bug where addons with no transients could not be added (e.g. REST API Doc)
- Added separate ExpressionService class to sample code
- Fixed issue in sample code when searching for work object by id.
- Updated analytics addon version and corrected typo in artifact name
- Minor documentation updates

### 02-23-2018
- Update version to starter 2.0.4 (also most addons)
- Added possibility to create addon projects for edoras one 2.0
- Added sample code for projects (see https://github.com/edorasware/edoras-one-initializr/issues/2)
- Added sample code for addon projects
- Moved sample code generation checkbox to light view, unchecked by default now
- Updated travis build to support stages and deploy only on tags
- Setup test server with latest head version on https://edoras-one-initializr-test.cfapps.io
- Added new addon dependencies (ECM, Batch, ESLocal)
- Removed google analytics code
- Added dependency management bom to project pom
- Minor Bugfixes

### 02-12-2018
- Update version to starter 2.0.3-1
- Updated all addons to newest version (2.0.3)
- Limited Gear Search Addon to 1.6.x versions
- Added Index Core Addon (edoras-one-index)
- Added Gear Search (1.6) or Index Core (2.0) transients to Index Addon
- Added Bootapp Addon

### 01-19-2018
- Update version to starter 1.6.13
- Updated all addons to newest version (2.0.2)

### 01-18-2018
- Update versions to starter 2.0.2
- Updated most addons to newest version (2.0.1 or 2.0.2)

### 01-08-2018
- Update versions to starter 2.0.1
- Corrected live preview property
- Added Logback Operator Dashboard addon

### 11-30-2017
- Update versions to starter 2.0.0-2
- Added REST Documentation addon
- Added edoras one repository configuration to build.gradle
- Added Operator Dashboard 2.0 and Logback Plugin 2.0 as addon

### 11-16-2017
- Update versions to starter 2.0.0
- Removed versions 1.6.8 and 1.6.9 (deprecated)
- Added edoras one repository configuration to pom.xml
- Bugfix: Generated test class missed WorkObjectType import

### 11-01-2017
- Removed 1.6.11 release (quality reasons)

### 10-31-2017
- Update versions to starter 1.6.11 and 2.0.0-RC2

### 10-17-2017
- First tests to switch to flowable Pivotal account
- Finally switched to flowable Pivotal account.
- Travis Build with manifest instead of maven plugin for Cloud Foundry deployment

### 10-13-2017
- Update versions to starter 1.9.10

### 10-08-2017
- Updated versions to starter 2.0.0-RC1-2

### 10-07-2017
- Updated versions to starter 2.0.0-RC1-1

### 10-06-2017
- Removed shortName-web-context.xml generation and documentation (not supported by edoras one since 2.0 anymore)
- Updated SampleComponentTest for 2.0.0-RC1
- Updated version to starter 2.0.0-RC1
- Fixed VersionParser to allow RC1 qualifier

### 09-26-2017
- Finalized code and added functionality for transient dependencies (addons that need other addons)

### 09-12-2017
- Corrected version range parser
- Added code for transient Plugin dependencies
- Moved sample code checkbox to enhanced section

### 08-21-2017
- Updated versions to starter 1.6.9-1 and 2.0.0-M6-2
- Changed logback.xml to application-logback.xml so so projects can extend the default logback config (instead of providing their own logback.xml)
- Updated Version Regex to match new 1.6.9-1 pattern.
- Added checkbox to choose to create sample code (enabled by default)

### 08-11-2017
- Added versions 1.6.9 and 2.0.0-M6-1
- Made version 1.6.9 the default version
- Corrected a bug that added root project files twice to download archive

### 08-07-2017
- Forked project to edorasware account
- Updated VersionParser to allow Starter-Patch versions (Versioning scheme: ProductMajor.ProductMinor.ProductPatch-ProductQualifier-StarterPatch-StarterQualifier) 
- Corrected README.md template (minor formatting and spelling)
- Made the basic artifact metadata fields (artifactId, groupId and shortname) required.
- Added field description as help-block to html (e.g. for explanation of shortName)
- Added field validation to shortName field.
- Added change log to README.md

### 08-06-2017
- Added versions 1.6.8 and 2.0.0-M6