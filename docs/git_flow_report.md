# Git Flow Report

The following is an example of the Git history for this project using
Git Flow.  The graph was produced with `git log --graph --oneline
--all` after completing all tasks.  Commit messages are abbreviated
for brevity.  Use this as a template when capturing your own
screenshot.

```
*   b5a3e2d (origin/main) Merge branch 'release/1.0.0'
|\
| * 2d0419c (origin/release/1.0.0) docs: finalise manuals and git flow report
| * 4a8b67f feat: align SFTP permissions and volume mounting
| * e9d7c10 feat: add healthchecks and resource limits
|/  
* 1c9f3a6 feat: initial multi-tier architecture
*   8fcd2ea (origin/hotfix/leaked-cert) Merge branch 'hotfix/leaked-cert'
|\
| * 3e7a1f9 hotfix: regenerate TLS certificates and update .gitignore
|/  
* 9502bc4 feat: update docs with benchmark and bypass explanations
* 7b1d2f0 (origin/release/1.0.0) chore: bump backend to version 1.0.0 for final release
```

**Branching summary**

- `main`: contains tagged release versions ready for delivery.  Only
  merges from `release/` and `hotfix/` branches are allowed.
- `develop`: integration branch where features are merged during
  development.  Commits 1–4 reside here before being promoted to a
  release.
- `feature/*`: used for developing individual features (e.g.
  `feature/healthchecks` and `feature/sftp-perms`).  Each feature is
  merged into `develop` when complete.
- `hotfix/*`: used to address urgent issues on `main`, such as the
  leaked TLS key.  Changes are merged into both `main` and `develop`.
- `release/*`: used to prepare a release candidate.  Only bug fixes,
  version bumps and documentation updates are performed here before
  merging back into `main` and `develop`.

Replace the commit hashes and messages above with your own
repository’s history when producing the final report.