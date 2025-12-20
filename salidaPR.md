Root cause
- The job failed when the dorny/test-reporter action tried to create a check run and got: "HttpError: Resource not accessible by integration". This happens when the reporter (Checks API) is invoked from workflows that do not have permission to create checks for the target repository — commonly when the workflow runs for a pull request coming from a fork or otherwise lacks Checks API write permissions.
- The failing lines are from the test-report step in .github/workflows/ci.yml (ref: 35c13b7cdd463634df0d177df965691e160166ec): https://github.com/niclesanti/ProyectoGastos/blob/35c13b7cdd463634df0d177df965691e160166ec/.github/workflows/ci.yml#L63-L71

Immediate, low-risk fix (recommended)
- Skip the test-reporter step for PRs originating from forks (or from external repositories). Add a conditional so the step only runs when the run has permissions to create check runs (i.e., when the PR head repo is the same as the base repo, or when the event is not a pull_request).

Replace the "Generate test report" step with this guarded version:

```yaml
      - name: Generate test report
        if: >
          always() &&
          (github.event_name != 'pull_request' ||
           github.event.pull_request.head.repo.full_name == github.repository)
        uses: dorny/test-reporter@v1
        with:
          name: Maven Tests
          path: 'backend/target/surefire-reports/TEST-*.xml'
          reporter: java-junit
          fail-on-error: false
```

What this does
- always() keeps the step from being skipped on failures so the report runs for pushes and same-repo PRs.
- The extra condition prevents attempts to create check runs for forked PRs, avoiding the "Resource not accessible by integration" error.

Optional/additional improvements
1) Add job-level permissions to allow the action to write checks when appropriate
- If your workflows run from branches within the same repo and you want explicit permission, add at the top of the job:

```yaml
  test:
    name: Run Tests
    permissions:
      contents: read
      checks: write
    runs-on: ubuntu-latest
```

Note: this will not fix forked-PR cases because those runs are still restricted by GitHub for security reasons. The conditional above is still required for forked PRs.

2) As an alternative to skipping the reporter for forks:
- Use artifact upload (actions/upload-artifact) to store the XML test files and run the reporting step in a separate workflow triggered in the target repo (requires a workflow that has write permissions), or run a different reporter that doesn't call the Checks API.

About the PostgreSQL "role 'root' does not exist" lines
- The logs show several "FATAL: role 'root' does not exist" messages from the postgres service. Those are likely benign attempts to connect as the system user 'root' (not the test DB user) — the service is still configured in the workflow with POSTGRES_USER/POSTGRES_PASSWORD and your tests use SPRING_DATASOURCE_* env vars (see the Run tests step). If you want to eliminate those lines:
  - Avoid mapping the service port to the host (remove ports: - 5432:5432) so there’s no accidental attempt to connect via host:5432 from other containers/host processes.
  - Or ensure any scripts/commands run in the job do not try to connect as root to the DB.

Example change to the postgres service (optional):

```yaml
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
        # remove host port mapping to avoid external attempts to connect as root
        # ports:
        #   - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
```

Summary of recommended patch
1. Modify the "Generate test report" step to include the conditional for forked PRs (snippet above).
2. Optionally add job-level permissions checks: checks: write for same-repo runs.
3. Optionally remove the host port mapping in the postgres service to reduce noisy "role 'root' does not exist" messages.

Files to edit
- .github/workflows/ci.yml at commit ref 35c13b7cdd463634df0d177df965691e160166ec:
  https://github.com/niclesanti/ProyectoGastos/blob/35c13b7cdd463634df0d177df965691e160166ec/.github/workflows/ci.yml

Apply those changes, push to the branch, and re-run the workflow — the "Resource not accessible by integration" error should be eliminated and the job should no longer fail for PRs from forks.