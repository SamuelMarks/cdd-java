# Publishing cdd-java

## Publishing the Package
To publish this Java library to Maven Central, you need to use Sonatype Nexus and Maven (`mvn deploy`).

1. Sign up for Sonatype OSSRH.
2. Setup your `~/.m2/settings.xml` with your Sonatype credentials.
3. Use a `pom.xml` with the maven-javadoc-plugin, maven-source-plugin, and maven-gpg-plugin to sign your artifacts.
4. Run `mvn clean deploy -P release` to upload to the staging repository, then release it via the Sonatype GUI or Nexus Staging Maven Plugin.

## Publishing the Documentation
To build the docs to a local folder, use:
```sh
make build_docs docs/
```
You can serve the `/docs` directory using a simple HTTP server or push it to GitHub Pages.

To publish docs to a popular location (e.g. javadoc.io), simply publish your release to Maven Central, and javadoc.io will automatically pick it up and host your docs.
