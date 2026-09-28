# AI Context Generation Protocol

`.ai-context/` is the project's persistent, compact AI context layer. It is not a copy of the source tree.

## When generating or updating context

### Bootstrap an existing project

Use this mode when the semantic context files are missing or clearly incomplete. The repository may already be 50%, 80%, or nearly 100% implemented; the current source tree is the baseline.

1. Read `evidence.json` first.
2. Use the evidence to identify the project type, technology stack, important directories, files, dependencies, configuration, and repository state.
3. Inspect only the source/configuration files needed to understand the existing implementation.
4. Create a baseline `MAP.md`, `CONVENTIONS.md`, `STATE.md`, and `DECISIONS.md` from confirmed facts.
5. Do not reconstruct missing history. Do not fabricate decisions. Mark uncertainty explicitly.
6. Keep the context compact, navigational, and high-signal.

### Normal updates

1. Read the current semantic context and `evidence.json`.
2. Inspect only the source files needed to validate material changes.
3. Generate or update only the semantic context files that are materially affected.
4. Keep the context compact, navigational, and high-signal.

## Semantic files

- `README.md` — explains the purpose of `.ai-context/` and how an AI agent should use it.
- `MAP.md` — compact architectural/project map: important directories, module responsibilities, important files, entry points, and major relationships.
- `CONVENTIONS.md` — confirmed development conventions: language/runtime, framework, package manager, build/test/linting/formatting systems, naming conventions, and important development rules.
- `STATE.md` — meaningful current project state: major progress, known issues, completed work, current focus, and relevant pending work.
- `DECISIONS.md` — meaningful architectural or implementation decisions. Record only confirmed decisions; clearly label inferred understanding when useful.
- `evidence.json` — deterministic evidence produced by AI Context Kit. Treat it as evidence, not semantic understanding.

## Update rule

Do not update context for every code change. Update only when the change materially affects:

- project structure
- architecture or module responsibilities
- important file locations
- dependencies or technology stack
- build/test/development conventions
- meaningful project state
- important known issues
- architectural decisions
- important workflows

A useful test is: would a future AI agent need to know this change to understand, navigate, modify, or maintain the project correctly?

## Source of truth and evidence

- Source code is always the final source of truth.
- If context conflicts with source code, correct the context.
- Never invent project facts.
- Base facts on source code, configuration, repository structure, Git history, documentation, or explicit decisions.
- Distinguish confirmed facts from inference.
- Mark unknown information as unknown rather than guessing.

## Evidence refresh

If structure, architecture, dependencies, or important repository state has materially changed, refresh deterministic evidence with `ai-context update` before revising the semantic context.

Do not manually edit `evidence.json` unless the tool itself is being developed and the change is intentionally part of its generated output.

## Secret protection

AI Context Kit applies built-in secret protection independently from `.ai-contextignore`.

- Do not copy secret values, credentials, tokens, private keys, passwords, API keys, or environment secrets into semantic context.
- Treat protected files and directories as unavailable unless the user's task explicitly requires their structure and there is no safer alternative.
- Prefer `.env.example`, configuration schemas, variable names, or redacted values.
- `.ai-contextignore` controls context inclusion for ordinary project files; it is not a security boundary.
