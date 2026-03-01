# Publishing the Output

When you generate a client-library SDK using `cdd-java from_openapi to_sdk`, you can publish the output automatically using GitHub Actions.

## Sync Client SDK via Cron Job

Keep your client SDK up-to-date with your backend OpenAPI specification.

Create `.github/workflows/sdk_sync.yml` in your SDK repository:
```yml
name: Sync SDK
on:
  schedule:
    - cron: '0 0 * * *' # Every midnight
  workflow_dispatch:
jobs:
  sync:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Install Java
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      - name: Download Spec
        run: curl -sL https://api.yourservice.com/openapi.json -o spec.json
      - name: Generate SDK
        run: java -cp "lib/*:bin" cli.Main from_openapi to_sdk -i spec.json -o ./sdk/
      - name: Commit and Push
        run: |
          git config user.name "github-actions[bot]"
          git config user.email "github-actions[bot]@users.noreply.github.com"
          git add .
          git commit -m "chore: sync OpenAPI client SDK" || exit 0
          git push
```
