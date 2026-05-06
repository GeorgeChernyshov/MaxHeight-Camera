## Review guidelines

Scope
- One high-level task per PR. If multiple unrelated goals appear, request a split.
- Large formatting changes or unrelated refactors should be proposed separately.
- Small, safe fixes (e.g. obvious typos) are fine as long as they are intentional and do not introduce changes to other parts of the project that are not included in this PR.

Correctness
- Confirm the PR fully resolves any referenced issues (Fixes/Closes).
- Flag behavior changes outside stated scope.
- Ensure tests/build/docs remain consistent with the change.

Commit/history hygiene
- Commits must be reviewable and focused.
- Messages should state what changed and why.
- If history is noisy, request cleanup (rebase/squash).

When to request changes
- Mixed scope
- Partial issue resolution
- Unclear/unsafe behavior changes
- Messy history or unclear commit intent