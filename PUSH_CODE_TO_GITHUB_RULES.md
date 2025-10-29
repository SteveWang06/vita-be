
# Git Push Rules

## 1. General Rules
- **Do not push directly** to `main` or `master` (except urgent hotfix approved by Tech Lead).
- All changes must go through a **Pull Request (PR)**.
- Commit messages must follow **Conventional Commits** (see Section 4).
- Always pull the latest code from remote before pushing.
- Do not commit build artifacts, log files, local data, or IDE settings.
- Keep commits clean, small, and meaningful.

---

## 2. Standard Workflow (Checklist)
Follow these steps for every change:

1. **Create a new branch** from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/<feature-name>   # e.g., feature/user-service-crud
   ```

2. **Commit code**:
   - Stage only necessary files.
   - Use the correct commit message format.
   ```bash
   git add .
   git commit -m "feat(user-service): add create user API"
   ```

3. **Sync with the latest remote changes** to avoid conflicts:
   ```bash
   git pull origin develop --rebase
   ```

4. **Push the branch**:
   ```bash
   git push origin feature/<feature-name>
   ```

5. **Open a Pull Request**:
   - Target `develop` (or `release/x.x.x` if preparing a release).
   - Requires **at least one reviewer approval**.
   - CI/CD pipeline must pass before merging.

---

## 3. Branch Naming Convention
| Type       | Pattern                        | Example                          |
|------------|--------------------------------|-----------------------------------|
| Feature    | `feature/<feature-name>`       | feature/auth-service-jwt          |
| Bugfix     | `bugfix/<short-desc>`          | bugfix/user-login-error           |
| Hotfix     | `hotfix/<short-desc>`          | hotfix/payment-timeout            |
| Chore      | `chore/<short-desc>`           | chore/update-spring-boot-3.3      |

---

## 4. Commit Message Convention (Conventional Commits)
**Format:**
```
<type>(<scope>): <short description>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (no logic change)
- `refactor`: Refactoring without changing behavior
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Examples**:
```
feat(auth-service): implement JWT authentication
fix(user-service): resolve NPE when updating profile
docs(api-gateway): add README for caching config
```

---

## 5. Pre-Push Checklist
- [ ] Run `mvn clean install` with no errors.
- [ ] All unit and integration tests pass.
- [ ] Code formatted according to `CODING_STYLE.md`.
- [ ] No sensitive data committed (passwords, API keys, `.env`).
- [ ] Pulled the latest changes from remote and resolved conflicts.
- [ ] Verified that changes do not break existing features.

---

## 6. Merge Strategy
- Use **Squash and Merge** for PRs to keep commit history clean.
- Never merge if CI/CD fails.
- Hotfixes on `main` must be **back-merged into develop** immediately after deployment.

---

## 7. Useful Git Commands
```bash
# Create a new branch
git checkout -b feature/<name>

# Sync branch with remote
git pull origin develop --rebase

# Push branch
git push origin feature/<name>

# Undo last commit but keep changes
git reset --soft HEAD~1
```

---

## 8. Git Hooks for Rule Enforcement

### 8.1. commit-msg — Validate commit message format

Create `.git/hooks/commit-msg` with content:
```bash
#!/bin/sh
commit_msg_file=$1
commit_msg=$(cat "$commit_msg_file")
pattern="^(feat|fix|docs|style|refactor|test|chore)\([a-z0-9-]+\): .+"

if ! echo "$commit_msg" | grep -Eq "$pattern"; then
    echo "❌ Invalid commit message format."
    echo "✅ Example: feat(user-service): add create user API"
    exit 1
fi
```

Make it executable:
```bash
chmod +x .git/hooks/commit-msg
```

---

### 8.2. pre-push — Block direct pushes to main/master

Create `.git/hooks/pre-push` with content:
```bash
#!/bin/sh
current_branch=$(git rev-parse --abbrev-ref HEAD)
if [ "$current_branch" = "main" ] || [ "$current_branch" = "master" ]; then
    echo "❌ Direct pushes to $current_branch are not allowed."
    echo "✅ Create a new branch and open a Pull Request."
    exit 1
fi
```

Make it executable:
```bash
chmod +x .git/hooks/pre-push
```

---

## 9. Notes
- Follow `CODING_STYLE.md` before committing.
- Use a **Pull Request template** (`PULL_REQUEST_TEMPLATE.md`) for clear PR descriptions.
- Respect the review process — no self-merging unless approved.
- Keep commit history meaningful for easier debugging.

---
