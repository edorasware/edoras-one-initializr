/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edorasware.one.initializr.generator;

import com.edorasware.one.initializr.InitializrException;
import com.edorasware.one.initializr.metadata.*;
import com.edorasware.one.initializr.metadata.InitializrConfiguration.Env.Maven.ParentPom;
import com.edorasware.one.initializr.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generate a project based on the configured metadata.
 *
 * @author Dave Syer
 * @author Stephane Nicoll
 * @author Sebastien Deleuze
 * @author Andy Wilkinson
 * @author Roger Villars
 */
public class AddonGenerator {

	private static final Logger log = LoggerFactory.getLogger(AddonGenerator.class);

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private InitializrMetadataProvider metadataProvider;

	@Autowired
	private ProjectRequestResolver requestResolver;

	@Autowired
	private ProjectGenerator projectGenerator;

	private TemplateRenderer addonTemplateRenderer = new TemplateRenderer(TemplateRenderer.mustacheCompiler("classpath:/templates/addon/"));

	private ProjectResourceLocator projectResourceLocator = new ProjectResourceLocator();

	@Value("${TMPDIR:.}/initializr")
	private String tmpdir;

	private File temporaryDirectory;
	private transient Map<String, List<File>> temporaryFiles = new LinkedHashMap<>();

	public InitializrMetadataProvider getMetadataProvider() {
		return metadataProvider;
	}

	public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void setMetadataProvider(InitializrMetadataProvider metadataProvider) {
		this.metadataProvider = metadataProvider;
	}

	public void setRequestResolver(ProjectRequestResolver requestResolver) {
		this.requestResolver = requestResolver;
	}

	public void setAddonTemplateRenderer(TemplateRenderer addonTemplateRenderer) {
		this.addonTemplateRenderer = addonTemplateRenderer;
	}

	public void setProjectResourceLocator(ProjectResourceLocator projectResourceLocator) {
		this.projectResourceLocator = projectResourceLocator;
	}

	public void setTmpdir(String tmpdir) {
		this.tmpdir = tmpdir;
	}

	public void setTemporaryDirectory(File temporaryDirectory) {
		this.temporaryDirectory = temporaryDirectory;
	}

	public void setTemporaryFiles(Map<String, List<File>> temporaryFiles) {
		this.temporaryFiles = temporaryFiles;
	}

	/**
	 * Generate a Maven pom for the specified {@link ProjectRequest}.
	 */
	public byte[] generateMavenPom(ProjectRequest request) {
		try {
			Map<String, Object> model = resolveModel(request);
			if (!isMavenBuild(request)) {
				throw new InvalidProjectRequestException("Could not generate Maven pom, "
						+ "invalid project type " + request.getType());
			}
			byte[] content = doGenerateAddonParentPom(model);
			publishAddonGeneratedEvent(request);
			return content;
		}
		catch (InitializrException ex) {
			publishAddonFailedEvent(request, ex);
			throw ex;
		}
	}

	/**
	 * Generate a project structure for the specified {@link ProjectRequest}. Returns a
	 * directory containing the project.
	 */
	public File generateAddonStructure(ProjectRequest request) {
		try {
			return doGenerateAddonStructure(request);
		}
		catch (InitializrException ex) {
			publishAddonFailedEvent(request, ex);
			throw ex;
		}
	}

	protected File doGenerateAddonStructure(ProjectRequest request) {
		// manually set some properties, seems not to be stable in javascript
//		request.setBaseDir(request.getArtifactId());
		request.setPackageName(request.getGroupId().concat(".").concat(request.getShortName()));

		Map<String, Object> model = resolveModel(request);

		// parent dir
		File parentDir;
		try {
			parentDir = File.createTempFile("tmp", "", getTemporaryDirectory());
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot create temp dir", e);
		}
		addTempFile(parentDir.getName(), parentDir);
		parentDir.delete();
		parentDir.mkdirs();

		// parent pom.xml
		String pom = new String(doGenerateAddonParentPom(model));
		writeText(new File(parentDir, "pom.xml"), pom);

		// .gitignore
		generateGitIgnore(parentDir, request);

		// README.md
		write(new File(parentDir, "README.md"), "addon-README.tmpl", model);

		String language = request.getLanguage();

		// ==== Addon Module ====

		String capitalShortName = StringUtils.capitalize(request.getShortName());

		File addonDir = new File(parentDir, request.getArtifactId());
		addonDir.mkdirs();

		String addonPom = new String(doGenerateAddonModulePom(model));
		writeText(new File(addonDir, "pom.xml"), addonPom);

		// src/main/java
		File javaSrc = new File(addonDir, "src/main/" + language);
		File addonSrc = new File(javaSrc, request.getPackageName().replace(".", "/"));
		addonSrc.mkdirs();

		if (request.isCreateSampleCode()) {

			// Application Context Configuration
			File configSrc = new File(javaSrc, "com/edorasware/addons/config");
			configSrc.mkdirs();
			write(new File(configSrc, capitalShortName+"AddonConfiguration."+language), "addon-configuration." + language + ".tmpl", model);

			// Rest Context Configuration
			File restConfigSrc = new File(javaSrc, "com/edorasware/addons/rest");
			restConfigSrc.mkdirs();
			write(new File(restConfigSrc, capitalShortName+"AddonRestConfiguration."+language), "addon-rest-configuration." + language + ".tmpl", model);

			// Controller
			File addonDemoController = new File(addonSrc, "controller");
			addonDemoController.mkdirs();
			write(new File(addonDemoController, capitalShortName+"AddonController." + language), "addon-controller." + language + ".tmpl", model);

			// Service Interface
			File addonDemoService = new File(addonSrc, "service");
			addonDemoService.mkdirs();
			write(new File(addonDemoService, capitalShortName+"AddonService." + language), "addon-service-interface." + language + ".tmpl", model);

			// Service Implementation
			File addonDemoServiceImpl = new File(addonDemoService, "impl");
			addonDemoServiceImpl.mkdirs();
			write(new File(addonDemoServiceImpl, "Default"+capitalShortName+"AddonService." + language), "addon-service-implementation." + language + ".tmpl", model);
		}

		// src/test/java
		File addonTest = new File(new File(addonDir, "src/test/" + language),
				request.getPackageName().replace(".", "/"));
		addonTest.mkdirs();

		// src/main/resources
		File addonResources = new File(addonDir, "src/main/resources");
		addonResources.mkdirs();

		if (request.isCreateSampleCode()) {

			// Widget CSS/JS
			File widgetResouces = new File(addonResources, "com/edorasware/addons/widget");
			widgetResouces.mkdirs();
			write(new File(widgetResouces, "edoras-addon-"+request.getShortName()+".css"), "addon-widget.css", model);
			write(new File(widgetResouces, "edoras-addon-"+request.getShortName()+".js"), "addon-widget.js", model);

			// Palette
			File paletteResouces = new File(addonResources, "com/edorasware/addons/palette");
			paletteResouces.mkdirs();
			write(new File(paletteResouces, "edoras-addon-"+request.getShortName()+".process.palette.xml"), "addon.process.palette.xml", model);
		}

		// src/test/resources
		File addonTestResources = new File(addonDir, "src/test/resources");
		addonTestResources.mkdirs();


		// ==== Addon Frontend Module ====

		File addonFrontendDir = new File(parentDir, request.getArtifactId()+"-frontend");
		addonFrontendDir.mkdirs();

		String addonFrontendPom = new String(doGenerateAddonFrontendModulePom(model));
		writeText(new File(addonFrontendDir, "pom.xml"), addonFrontendPom);

		// package.json
		write(new File(addonFrontendDir, "package.json"), "addon-frontend-package.json", model);

		// README.md
		write(new File(addonFrontendDir, "README.md"), "addon-frontend-README.tmpl", model);

		// ==== Addon Demo Module ====

		File addonDemoDir = new File(parentDir, request.getArtifactId()+"-demo");
		addonDemoDir.mkdirs();
		request.setPackageName("com.example."+request.getShortName());
		projectGenerator.doGenerateProjectStructure(request, addonDemoDir);

		String addonTestPom = new String(doGenerateAddonStarterModulePom(model));
		writeText(new File(addonDemoDir, "pom.xml"), addonTestPom);

		publishAddonGeneratedEvent(request);
		return parentDir;

	}

	/**
	 * Create a distribution file for the specified project structure directory and
	 * extension
	 */
	public File createDistributionFile(File dir, String extension) {
		File download = new File(getTemporaryDirectory(), dir.getName() + extension);
		addTempFile(dir.getName(), download);
		return download;
	}

	private File getTemporaryDirectory() {
		if (temporaryDirectory == null) {
			temporaryDirectory = new File(tmpdir, "initializr");
			temporaryDirectory.mkdirs();
		}
		return temporaryDirectory;
	}

	/**
	 * Clean all the temporary files that are related to this root directory.
	 * @see #createDistributionFile
	 */
	public void cleanTempFiles(File dir) {
		List<File> tempFiles = temporaryFiles.remove(dir.getName());
		if (!tempFiles.isEmpty()) {
			tempFiles.forEach((File file) -> {
				if (file.isDirectory()) {
					FileSystemUtils.deleteRecursively(file);
				}
				else if (file.exists()) {
					file.delete();
				}
			});
		}
	}

	private void publishAddonGeneratedEvent(ProjectRequest request) {
		ProjectGeneratedEvent event = new ProjectGeneratedEvent(request);
		eventPublisher.publishEvent(event);
	}

	private void publishAddonFailedEvent(ProjectRequest request, Exception cause) {
		ProjectFailedEvent event = new ProjectFailedEvent(request, cause);
		eventPublisher.publishEvent(event);
	}

	/**
	 * Generate a {@code .gitignore} file for the specified {@link ProjectRequest}
	 * @param dir the root directory of the project
	 * @param request the request to handle
	 */
	protected void generateGitIgnore(File dir, ProjectRequest request) {
		Map<String, Object> model = new LinkedHashMap<>();
		if (isMavenBuild(request)) {
			model.put("build", "maven");
			model.put("mavenBuild", true);
		}
		else {
			model.put("build", "gradle");
		}
		write(new File(dir, ".gitignore"), "addon-gitignore.tmpl", model);
	}

	/**
	 * Resolve the specified {@link ProjectRequest} and return the model to use to
	 * generate the project
	 * @param originalRequest the request to handle
	 * @return a model for that request
	 */
	protected Map<String, Object> resolveModel(ProjectRequest originalRequest) {
		Assert.notNull(originalRequest.getEdorasoneVersion(), "edorasone version must not be null");
		Map<String, Object> model = new LinkedHashMap<>();
		InitializrMetadata metadata = metadataProvider.get();

		ProjectRequest request = requestResolver.resolve(originalRequest, metadata);

		// request resolved so we can log what has been requested
		List<Dependency> dependencies = request.getResolvedDependencies();
		List<String> dependencyIds = dependencies.stream().map(Dependency::getId)
				.collect(Collectors.toList());
		log.info("Processing request{type=" + request.getType() + ", dependencies="
				+ dependencyIds);

		if (isWar(request)) {
			model.put("war", true);
		}

		if (isMavenBuild(request)) {
			model.put("mavenBuild", true);

			ParentPom parentPom = null;
			if (!isWar(request)) {
				parentPom = new ParentPom();
				parentPom.setGroupId("com.edorasware.one");
				parentPom.setArtifactId("edoras-one-starter-parent");
				parentPom.setVersion(request.getEdorasoneVersion());
			}
			if (parentPom != null) {
				model.put("mavenParentDefined", true);
				model.put("mavenParentGroupId", parentPom.getGroupId());
				model.put("mavenParentArtifactId", parentPom.getArtifactId());
				model.put("mavenParentVersion", parentPom.getVersion());
			} else {
				model.put("mavenParentDefined", false);
			}
		}

		model.put("repositoryValues", request.getRepositories().entrySet());
		if (!request.getRepositories().isEmpty()) {
			model.put("hasRepositories", true);
		}

		List<Map<String,String>> resolvedBoms = buildResolvedBoms(request);
		model.put("resolvedBoms", resolvedBoms);
		ArrayList<Map<String,String>> reversedBoms = new ArrayList<>(resolvedBoms);
		Collections.reverse(reversedBoms);
		model.put("reversedBoms", reversedBoms);

		model.put("compileDependencies",
				filterDependencies(dependencies, Dependency.SCOPE_COMPILE));
		model.put("runtimeDependencies",
				filterDependencies(dependencies, Dependency.SCOPE_RUNTIME));
		model.put("compileOnlyDependencies",
				filterDependencies(dependencies, Dependency.SCOPE_COMPILE_ONLY));
		model.put("providedDependencies",
				filterDependencies(dependencies, Dependency.SCOPE_PROVIDED));
		model.put("testDependencies",
				filterDependencies(dependencies, Dependency.SCOPE_TEST));

		request.getBoms().forEach((k, v) -> {
			if (v.getVersionProperty() != null) {
				request.getBuildProperties().getVersions().computeIfAbsent(
						v.getVersionProperty(), key -> v::getVersion);
			}
		});

		Map<String, String> versions = new LinkedHashMap<>();
		model.put("buildPropertiesVersions", versions.entrySet());
		request.getBuildProperties().getVersions().forEach((k, v) ->
				versions.put(computeVersionProperty(request,k), v.get()));
		Map<String, String> gradle = new LinkedHashMap<>();
		model.put("buildPropertiesGradle", gradle.entrySet());
		request.getBuildProperties().getGradle().forEach((k, v) ->
				gradle.put(k, v.get()));
		Map<String, String> maven = new LinkedHashMap<>();
		model.put("buildPropertiesMaven", maven.entrySet());
		request.getBuildProperties().getMaven().forEach((k, v) -> maven.put(k, v.get()));

		// edoras one version
		model.put("edorasOneVersionWithoutStarter", request.getEdorasoneVersion().split("-")[0]);

		// Add various versions
		model.put("dependencyManagementPluginVersion", metadata.getConfiguration()
				.getEnv().getGradle().getDependencyManagementPluginVersion());
		model.put("kotlinVersion",
				metadata.getConfiguration().getEnv().getKotlin().getVersion());
		if ("kotlin".equals(request.getLanguage())) {
			model.put("kotlin", true);
		}
		if ("groovy".equals(request.getLanguage())) {
			model.put("groovy", true);
		}

		// Java versions
		model.put("isJava6", isJavaVersion(request, "1.6"));
		model.put("isJava7", isJavaVersion(request, "1.7"));
		model.put("isJava8", isJavaVersion(request, "1.8"));

		// edoras one versions
		model.put("isEdorasOne10", isEdorasoneVersion10(request));
		model.put("isEdorasOne20", isEdorasoneVersion20(request));

		// Append the project request to the model
		BeanWrapperImpl bean = new BeanWrapperImpl(request);
		for (PropertyDescriptor descriptor : bean.getPropertyDescriptors()) {
			if (bean.isReadableProperty(descriptor.getName())) {
				model.put(descriptor.getName(),
						bean.getPropertyValue(descriptor.getName()));
			}
		}
		if (!request.getBoms().isEmpty()) {
			model.put("hasBoms", true);
		}

		model.put("capitalShortName", StringUtils.capitalize(request.getShortName()));
		model.put("packagePath", request.getPackageName().replace(".", "/"));

		return model;
	}

	private List<Map<String,String>> buildResolvedBoms(ProjectRequest request) {
		return request.getBoms().values().stream()
				.sorted(Comparator.comparing(BillOfMaterials::getOrder))
				.map(bom -> toBomModel(request, bom))
				.collect(Collectors.toList());
	}

	private Map<String,String> toBomModel(ProjectRequest request, BillOfMaterials bom) {
		Map<String, String> model = new HashMap<>();
		model.put("groupId", bom.getGroupId());
		model.put("artifactId", bom.getArtifactId());
		model.put("versionToken", (bom.getVersionProperty() != null
				? "${" + computeVersionProperty(request, bom.getVersionProperty()) + "}"
				: bom.getVersion()));
		return model;
	}

	private String computeVersionProperty(ProjectRequest request,
			VersionProperty property) {
		if (isGradleBuild(request)) {
			return property.toCamelCaseFormat();
		}
		return property.toStandardFormat();
	}

//	protected void setupTestModel(ProjectRequest request, Map<String, Object> model) {
//		String imports = "";
//		String testAnnotations = "";
//		boolean newTestInfrastructure = isNewTestInfrastructureAvailable(request);
//		if (newTestInfrastructure) {
//			imports += String.format(
//					generateImport("org.springframework.boot.test.context.SpringBootTest",
//							request.getLanguage()) + "%n");
//			imports += String.format(
//					generateImport("org.springframework.test.context.junit4.SpringRunner",
//							request.getLanguage()) + "%n");
//		}
//		else {
//			imports += String.format(generateImport(
//					"org.springframework.boot.test.SpringApplicationConfiguration",
//					request.getLanguage()) + "%n");
//			imports += String.format(generateImport(
//					"org.springframework.test.context.junit4.SpringJUnit4ClassRunner",
//					request.getLanguage()) + "%n");
//		}
//		if (request.hasWebFacet() && !newTestInfrastructure) {
//			imports += String.format(generateImport(
//					"org.springframework.test.context.web.WebAppConfiguration",
//					request.getLanguage()) + "%n");
//			testAnnotations = String.format("@WebAppConfiguration%n");
//		}
//		model.put("testImports", imports);
//		model.put("testAnnotations", testAnnotations);
//	}

	protected String generateImport(String type, String language) {
		String end = ("groovy".equals(language) || "kotlin".equals(language)) ? "" : ";";
		return "import " + type + end;
	}

	private static boolean isGradleBuild(ProjectRequest request) {
		return "gradle".equals(request.getBuild());
	}

	private static boolean isMavenBuild(ProjectRequest request) {
		return "maven".equals(request.getBuild());
	}

	private static boolean isWar(ProjectRequest request) {
		return "war".equals(request.getPackaging());
	}

	private static boolean isJavaVersion(ProjectRequest request, String version) {
		return request.getJavaVersion().equals(version);
	}

	private static boolean isEdorasoneVersion20(ProjectRequest request) {
		VersionRange edorasone20 = VersionParser.DEFAULT.parseRange("[2.0.0-M0,3.0.0-M0)");
		return edorasone20.match(Version.parse(request.getEdorasoneVersion()));
	}

	private static boolean isEdorasoneVersion10(ProjectRequest request) {
		VersionRange edorasone10 = VersionParser.DEFAULT.parseRange("[1.0.0-M0,2.0.0-M0)");
		return edorasone10.match(Version.parse(request.getEdorasoneVersion()));
	}

	private byte[] doGenerateAddonParentPom(Map<String, Object> model) {
		return addonTemplateRenderer.process("addon-parent-pom.xml", model).getBytes();
	}

	private byte[] doGenerateAddonModulePom(Map<String, Object> model) {
		return addonTemplateRenderer.process("addon-module-pom.xml", model).getBytes();
	}

	private byte[] doGenerateAddonFrontendModulePom(Map<String, Object> model) {
		return addonTemplateRenderer.process("addon-frontend-module-pom.xml", model).getBytes();
	}

	private byte[] doGenerateAddonStarterModulePom(Map<String, Object> model) {
		return addonTemplateRenderer.process("addon-starter-module-pom.xml", model).getBytes();
	}

	private byte[] doGenerateGradleBuild(Map<String, Object> model) {
		return addonTemplateRenderer.process("starter-build.gradle", model).getBytes();
	}

	private File writeBinaryResource(File dir, String name, String location) {
		return doWriteProjectResource(dir, name, location, true);
	}

	private File writeTextResource(File dir, String name, String location) {
		return doWriteProjectResource(dir, name, location, false);
	}

	private File doWriteProjectResource(File dir, String name, String location,
			boolean binary) {
		File target = new File(dir, name);
		if (binary) {
			writeBinary(target, projectResourceLocator
					.getBinaryResource("classpath:project/" + location));
		}
		else {
			writeText(target, projectResourceLocator
					.getTextResource("classpath:project/" + location));
		}
		return target;
	}

	private File initializerAddonDir(File rootDir, ProjectRequest request) {
		if (request.getBaseDir() != null) {
			File dir = new File(rootDir, request.getBaseDir());
			dir.mkdirs();
			return dir;
		}
		else {
			return rootDir;
		}
	}

	public void write(File target, String templateName, Map<String, Object> model) {
		String body = addonTemplateRenderer.process(templateName, model);
		writeText(target, body);
	}

	private void writeText(File target, String body) {
		try (OutputStream stream = new FileOutputStream(target)) {
			StreamUtils.copy(body, Charset.forName("UTF-8"), stream);
		}
		catch (Exception e) {
			throw new IllegalStateException("Cannot write file " + target, e);
		}
	}

	private void writeBinary(File target, byte[] body) {
		try (OutputStream stream = new FileOutputStream(target)) {
			StreamUtils.copy(body, stream);
		}
		catch (Exception e) {
			throw new IllegalStateException("Cannot write file " + target, e);
		}
	}

	private void addTempFile(String group, File file) {
		temporaryFiles.computeIfAbsent(group, key -> new ArrayList<>()).add(file);
	}

	private static List<Dependency> filterDependencies(List<Dependency> dependencies,
			String scope) {
		return dependencies.stream().filter(dep -> scope.equals(dep.getScope()))
				.sorted(Comparator.comparing(MetadataElement::getId))
				.collect(Collectors.toList());
	}

}
