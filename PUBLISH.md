# Publishing

To publish the `cdd-java` binary, library, and WebAssembly packages:

1. **Maven Central**:
   ```sh
   mvn clean deploy
   ```
   Requires GPG signing and Sonatype Nexus credentials configured in `~/.m2/settings.xml`.

2. **NPM WebAssembly Package (`@offscale/cdd-java`)**:
   ```sh
   make build_wasm
   cd package
   npm publish --access public
   ```

3. **Javadoc Documentation**:
   ```sh
   make build_docs
   ```
   Builds the HTML Javadoc in the `docs/` folder for deployment to GitHub Pages or any static file host.
