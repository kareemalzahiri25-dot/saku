# AI Context

`.ai-context/` is the persistent AI context layer for this repository.

Use it to locate relevant source code quickly without repeatedly scanning the whole project. The source code remains the final source of truth.

## How to use it

### Existing project bootstrap

If the semantic files are missing or incomplete, bootstrap the context automatically from the project as it exists now. Do not ask the user for a separate bootstrap prompt:

1. Read `evidence.json` for deterministic facts.
2. Inspect only the source/configuration needed to understand the current structure and stack.
3. Create a compact baseline in `MAP.md`, `CONVENTIONS.md`, `STATE.md`, and `DECISIONS.md`.
4. Record only confirmed facts; do not invent project history or decisions.

### Normal development

1. Start with `MAP.md` to understand the project structure and important entry points.
2. Read `CONVENTIONS.md`, `STATE.md`, and `DECISIONS.md` when relevant to the task.
3. Use `evidence.json` when deterministic project or repository facts need verification.
4. Inspect the relevant source files before making changes.
5. When `evidence.json` contains a `git_head`, compare it with the current Git `HEAD` to detect stale deterministic evidence.
6. After meaningful project-level changes, update only the affected context files and refresh deterministic evidence with `ai-context update` when required.

## Secret protection

AI Context Kit filters common secret-bearing files, paths, and secret-like text out of deterministic evidence. This protection is **not a security boundary**: the AI agent still has normal filesystem access unless the host environment or agent sandbox restricts it.

Never persist secret values in `.ai-context/`, logs, documentation, or commits. Prefer `.env.example`, configuration schemas, variable names, or redacted values.

Treat built-in protected files and directories as protected by default, including `.env*`, private keys/certificates, Android signing/config files, cloud credentials, Docker credentials, SSH/GPG material, Terraform state/variables, Kubernetes credentials, and common token-bearing configuration files.

`AI-GENERATION.md` contains the full model-agnostic protocol for creating and maintaining the semantic context.
