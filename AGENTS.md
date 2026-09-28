<!-- ai-context:start -->
# AI Context Kit

This repository uses `.ai-context/` as its persistent AI context layer.

Before meaningful work:

1. Read `.ai-context/README.md`.
2. If `MAP.md`, `CONVENTIONS.md`, `STATE.md`, or `DECISIONS.md` are missing or clearly not yet bootstrapped, bootstrap the semantic context automatically before implementing the user's requested work. Do not ask the user to provide a separate analysis/bootstrap prompt.
3. For bootstrap, read `.ai-context/evidence.json`, then inspect only the source/configuration needed to understand the current project. Create or update `MAP.md`, `CONVENTIONS.md`, `STATE.md`, and `DECISIONS.md` from confirmed facts. Do not invent project history or decisions.
4. If semantic context already exists, read `MAP.md` and other `.ai-context/*.md` files when relevant.
5. Use the context to locate relevant source code instead of scanning the whole repository.
6. Source code is always the final source of truth.

Secret protection:

- Never copy, expose, summarize, or persist secret values.
- Never place credentials, tokens, private keys, passwords, API keys, signing secrets, or environment secret values into `.ai-context/`, `evidence.json`, logs, documentation, or commits.
- Do not read protected secret-bearing files unless the task explicitly requires their structure and there is no safer alternative.
- Prefer `.env.example`, configuration schemas, variable names, or redacted values.
- Treat these defaults as protected, including `.env*`, private keys/certificates, Android signing/config files such as `local.properties`, `gradle.properties`, `keystore.properties`, `google-services.json`, and common cloud/CI credential files such as `firebase*.json`, `service-account*.json`, Terraform state/variables, Kubernetes credentials, SSH/GPG material, `docker/config.json`, and `.docker/config.json`.
- AI Context Kit's deterministic inspector has built-in secret protection in addition to `.ai-contextignore`; `.ai-contextignore` is not a security boundary.

After completing work:

1. Verify the implementation.
2. Decide whether project-level knowledge materially changed.
3. If yes, refresh deterministic evidence yourself with `ai-context update` when structure, architecture, dependencies, or important state changed. Do not ask the user to run it.
4. Update only the affected `.ai-context/` files.

When bootstrapping an existing project, do not require the user to describe the project's history. Establish the baseline from the current repository state and mark unknown history as unknown.

The user should only need to describe the work they want done. Do not ask them to repeat this context-maintenance workflow unless a task genuinely requires information that is missing from the repository.

Never invent project facts. Keep `.ai-context/` compact and high-signal.
<!-- ai-context:end -->
