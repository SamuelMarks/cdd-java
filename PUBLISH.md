# Publishing `cdd-java`

This guide explains how to publish the `cdd-java` tool itself, including its binary/library artifacts and its documentation.

## Publishing Artifacts

For Java, the most popular and standard location for publishing open-source libraries is **Maven Central**.

### Top 5 Build Systems for Publishing

1. **Maven:** The industry standard. Publishing to Maven Central involves configuring the `maven-release-plugin` and `nexus-staging-maven-plugin` in your `pom.xml`, and signing artifacts using GPG via `maven-gpg-plugin`.
2. **Gradle:** Highly popular for modern projects. Uses the `maven-publish` and `signing` plugins in `build.gradle` to generate POMs, sign artifacts, and upload to Sonatype OSSRH.
3. **Ant (with Ivy):** Legacy system. Can publish to Maven repositories using the Ivy `makepom` and `publish` tasks, though manual GPG signing is often required.
4. **SBT:** Popular in the Scala/Java ecosystem. Uses `sbt-pgp` and `sbt-sonatype` plugins to package, sign, and release to Maven Central.
5. **Bazel:** Used for large monorepos. Rules like `rules_jvm_external` or custom deployment scripts with `maven_publish` rules are used to push artifacts to remote repositories.

### Top 5 Locations/Ways for Publishing Java Packages

1. **Maven Central (Sonatype OSSRH):** The default public registry for Java. Requires namespace verification (e.g., a custom domain or GitHub coordinates), source/javadoc jars, and GPG signatures.
2. **GitHub Packages:** Excellent for organizations using GitHub. Integrates natively with GitHub Actions. Uses a `pom.xml` or `build.gradle` pointing to `https://maven.pkg.github.com/OWNER/REPOSITORY`. Requires a GitHub token (PAT) for authentication.
3. **GitLab Package Registry:** Similar to GitHub Packages but for GitLab CI/CD. Uses a repository URL like `https://gitlab.com/api/v4/projects/<project_id>/packages/maven`.
4. **AWS CodeArtifact:** Enterprise-grade private package management. Requires AWS CLI to fetch an authorization token, which is then injected into Maven's `settings.xml` or Gradle properties.
5. **JFrog Artifactory / Sonatype Nexus (Self-hosted/Cloud):** The standard for enterprise internal publishing. Artifacts are deployed via HTTP PUT to custom internal URLs, typically requiring username/password or token authentication in `settings.xml`.

---

## Publishing Documentation

Properly publishing Javadocs is crucial for library adoption.

### 1. Generating a Local Folder for Static Serving

To host docs on your own server, you first generate the standard HTML Javadoc output.

**Using Maven:**
```bash
mvn javadoc:javadoc
```
*Output is placed in `target/site/apidocs/`.*

**Using Gradle:**
```bash
./gradlew javadoc
```
*Output is placed in `build/docs/javadoc/`.*

You can then serve this local folder using any static web server:
```bash
# Example using Python's http.server
cd target/site/apidocs
python3 -m http.server 8080
```
Or you can upload this folder directly to AWS S3, Google Cloud Storage, or an Nginx web root.

### 2. Uploading Docs to the Most Popular Location (javadoc.io)

The most popular, zero-configuration location for hosting Java documentation is **javadoc.io**.

**How it works:**
javadoc.io automatically fetches and serves the Javadocs for any artifact published to Maven Central. You do **not** need to manually upload docs to javadoc.io.

**Steps to publish:**
1. Ensure your build system packages a `-javadoc.jar` (e.g., using `maven-javadoc-plugin`).
2. Publish your artifact (including the `-javadoc.jar`) to Maven Central.
3. Once synced to Maven Central, your documentation is immediately available at:
   `https://javadoc.io/doc/<groupId>/<artifactId>`
   *(e.g., `https://javadoc.io/doc/com.example/cdd-java`)*

Alternatively, for GitHub projects, you can use **GitHub Pages** by pushing the contents of `target/site/apidocs/` to a `gh-pages` branch.
