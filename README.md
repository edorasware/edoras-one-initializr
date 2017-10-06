# edoras-one-initializr [![Build Status](https://travis-ci.org/edorasware/edoras-one-initializr.svg?branch=master)](https://travis-ci.org/edorasware/edoras-one-initializr)

See https://edoras-one-initializr.cfapps.io

Still in early alpha state. 

Using the generated code needs an edoras one license (see http://www.edorasware.com) and access to the appropriate repositories. Don't even try to get it running without.

Already working:
- Maven project and pom.xml creation based on edoras-one-starter 1.6 or 2.0.
- Gradle project and build.xml creation based on edoras-one-starter 1.6 or 2.0.
- Support for additional "Short Name" property (used for config files, tenant, etc.)
- Gradle .war and .executable .jar project generation (needs gradle 3.0+)
- Maven .war and .executable .jar project generation
- First very unstable draft of addon support.
- Support for different Java versions.

Todo:
- Improve addon dependency support
- Config generation for addons
- Metadata from Repository/Addon Marketplace
- Code cleanup
- Fixing a lot of bugs
- Support for Groovy / Kotlin
- Refactor the UI to use AngularJS/Webpack
- Externalize Templates and load them from GitHub

## Running form Command Line

Its also possible to run the initializr from a command line using the curl command e.g. like this:

```bash
curl http://edoras-one-initializr.cfapps.io/starter.tgz -d artifactId=my-app -d groupId=com.edorasware.app -d shortName=myApp | tar -zvx
```

## Change Log

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