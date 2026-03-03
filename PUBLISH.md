# Publishing
To publish the `cdd-java` binary/library:

1. Use Maven Central: `mvn deploy` (requires proper GPG and nexus credentials in `~/.m2/settings.xml`).
2. Documentation: `make build_docs` will build the JavaDoc in `docs/` folder. You can upload this to GitHub Pages or any static file host.
