# CI/CD Pipeline with GitLab - Complete Setup Guide

A comprehensive guide to set up a Continuous Integration/Continuous Deployment (CI/CD) pipeline in GitLab using webhook triggers from Git.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [GitLab CI/CD Concepts](#gitlab-cicd-concepts)
4. [Setting Up GitLab CI/CD](#setting-up-gitlab-cicd)
5. [Creating .gitlab-ci.yml](#creating-gitlab-ciyml)
6. [Webhook Configuration](#webhook-configuration)
7. [Pipeline Stages](#pipeline-stages)
8. [Environment Variables](#environment-variables)
9. [Deployment Strategies](#deployment-strategies)
10. [Troubleshooting](#troubleshooting)
11. [Best Practices](#best-practices)

---

## Overview

### What is CI/CD?

- **Continuous Integration (CI):** Automatically build and test code changes when pushed to the repository
- **Continuous Deployment (CD):** Automatically deploy code to environments after successful tests

### Benefits

- ✅ Automated testing and deployment
- ✅ Faster feedback on code changes
- ✅ Reduced manual errors
- ✅ Consistent deployment process
- ✅ Easy rollback capabilities

### GitLab CI/CD Flow

```
Developer pushes code → GitLab Webhook triggers → CI/CD Pipeline starts
    ↓
Build & Test → Code Quality Checks → Deploy to Staging → Deploy to Production
```

---

## Prerequisites

### Required Accounts & Tools

1. **GitLab Account**
   - Sign up at [gitlab.com](https://gitlab.com) or use self-hosted GitLab
   - Create a new project or use existing repository

2. **Git Repository**
   - Project code in a Git repository
   - Repository connected to GitLab

3. **Runner Access**
   - GitLab Runner installed and registered (or use GitLab.com shared runners)
   - Docker installed (if using Docker executor)

4. **Deployment Targets**
   - Staging server (optional)
   - Production server
   - Access credentials configured

### System Requirements

- Git installed locally
- Docker (for containerized builds)
- SSH access to deployment servers
- Required build tools (Java, Gradle, etc.)

---

## GitLab CI/CD Concepts

### Key Components

#### 1. **Pipeline**
A collection of jobs that run in stages. Triggered by:
- Git push/merge
- Webhook events
- Manual triggers
- Scheduled triggers

#### 2. **Stages**
Logical groups of jobs that run in sequence:
- `build` - Compile code
- `test` - Run tests
- `deploy` - Deploy to servers

#### 3. **Jobs**
Individual tasks within stages:
- `build-job` - Builds the application
- `test-job` - Runs unit tests
- `deploy-job` - Deploys to server

#### 4. **Runners**
Agents that execute jobs:
- GitLab.com shared runners (free tier)
- Self-hosted runners
- Docker executors

#### 5. **Webhooks**
HTTP callbacks triggered by Git events:
- Push events
- Merge request events
- Tag events

---

## Setting Up GitLab CI/CD

### Step 1: Create GitLab Project

1. **Create New Project:**
   - Go to GitLab dashboard
   - Click "New project"
   - Choose "Create blank project"
   - Enter project name: `webstore`
   - Set visibility level
   - Click "Create project"

2. **Push Existing Code:**
```bash
# If you have existing code
cd /path/to/your/project
git remote add origin https://gitlab.com/your-username/webstore.git
git push -u origin main
```

### Step 2: Enable CI/CD

CI/CD is automatically enabled in GitLab. You just need to create the configuration file.

### Step 3: Verify Runner Availability

1. Go to **Settings → CI/CD → Runners**
2. Check if runners are available:
   - **Shared runners** (GitLab.com) - Usually enabled by default
   - **Specific runners** - Need to be registered

---

## Creating .gitlab-ci.yml

### File Location

Create `.gitlab-ci.yml` in the root of your repository:

```
webstore/
├── .gitlab-ci.yml          ← CI/CD configuration
├── src/
├── build.gradle
└── ...
```

### Basic Pipeline Structure

```yaml
# .gitlab-ci.yml

stages:
  - build
  - test
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

# Build Stage
build:
  stage: build
  image: gradle:7.6-jdk21
  script:
    - echo "Building application..."
    - ./gradlew clean build -x test
  artifacts:
    paths:
      - build/libs/*.jar
    expire_in: 1 week
  only:
    - main
    - develop
    - merge_requests

# Test Stage
test:
  stage: test
  image: gradle:7.6-jdk21
  services:
    - postgres:15
  variables:
    POSTGRES_DB: webstore_test
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
    DATABASE_URL: "postgresql://postgres:postgres@postgres:5432/webstore_test"
  script:
    - echo "Running tests..."
    - ./gradlew test
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit: build/test-results/test/*.xml
    paths:
      - build/reports/tests/
    expire_in: 1 week
  only:
    - main
    - develop
    - merge_requests

# Code Quality Stage
code-quality:
  stage: test
  image: gradle:7.6-jdk21
  script:
    - echo "Running code quality checks..."
    - ./gradlew checkstyleMain checkstyleTest
    - ./gradlew pmdMain pmdTest
  artifacts:
    paths:
      - build/reports/checkstyle/
      - build/reports/pmd/
    expire_in: 1 week
  allow_failure: true
  only:
    - main
    - develop
    - merge_requests

# Deploy to Staging
deploy-staging:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
    - ssh-keyscan -H $STAGING_SERVER >> ~/.ssh/known_hosts
  script:
    - echo "Deploying to staging..."
    - scp build/libs/*.jar $STAGING_USER@$STAGING_SERVER:/opt/webstore/
    - ssh $STAGING_USER@$STAGING_SERVER "cd /opt/webstore && ./restart.sh"
  environment:
    name: staging
    url: https://staging.webstore.com
  only:
    - develop
  when: manual

# Deploy to Production
deploy-production:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
    - ssh-keyscan -H $PRODUCTION_SERVER >> ~/.ssh/known_hosts
  script:
    - echo "Deploying to production..."
    - scp build/libs/*.jar $PRODUCTION_USER@$PRODUCTION_SERVER:/opt/webstore/
    - ssh $PRODUCTION_USER@$PRODUCTION_SERVER "cd /opt/webstore && ./restart.sh"
  environment:
    name: production
    url: https://webstore.com
  only:
    - main
  when: manual
```

### Advanced Pipeline with Docker

```yaml
# .gitlab-ci.yml - Docker-based deployment

stages:
  - build
  - test
  - package
  - deploy

variables:
  DOCKER_DRIVER: overlay2
  DOCKER_TLS_CERTDIR: "/certs"
  IMAGE_TAG: $CI_REGISTRY_IMAGE:$CI_COMMIT_REF_SLUG
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

# Build Application
build:
  stage: build
  image: gradle:7.6-jdk21
  script:
    - ./gradlew clean build -x test
  artifacts:
    paths:
      - build/libs/*.jar
    expire_in: 1 week

# Run Tests
test:
  stage: test
  image: gradle:7.6-jdk21
  services:
    - postgres:15
  variables:
    POSTGRES_DB: webstore_test
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
  script:
    - ./gradlew test
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit: build/test-results/test/*.xml

# Build Docker Image
package:
  stage: package
  image: docker:24
  services:
    - docker:24-dind
  before_script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
  script:
    - docker build -t $IMAGE_TAG -t $CI_REGISTRY_IMAGE:latest .
    - docker push $IMAGE_TAG
    - docker push $CI_REGISTRY_IMAGE:latest
  only:
    - main
    - develop

# Deploy to Staging
deploy-staging:
  stage: deploy
  image: docker:24
  services:
    - docker:24-dind
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    - docker pull $CI_REGISTRY_IMAGE:develop
    - docker stop webstore-staging || true
    - docker rm webstore-staging || true
    - docker run -d --name webstore-staging -p 8080:8080 $CI_REGISTRY_IMAGE:develop
  environment:
    name: staging
    url: http://staging.webstore.com:8080
  only:
    - develop

# Deploy to Production
deploy-production:
  stage: deploy
  image: docker:24
  services:
    - docker:24-dind
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    - docker pull $CI_REGISTRY_IMAGE:main
    - docker stop webstore-production || true
    - docker rm webstore-production || true
    - docker run -d --name webstore-production -p 8080:8080 $CI_REGISTRY_IMAGE:main
  environment:
    name: production
    url: https://webstore.com
  only:
    - main
  when: manual
```

### Dockerfile Example

Create `Dockerfile` in project root:

```dockerfile
# Dockerfile

FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy JAR file
COPY build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Webhook Configuration

### Understanding GitLab Webhooks

Webhooks are HTTP callbacks triggered by Git events. GitLab can:
- **Send webhooks** to external services
- **Receive webhooks** from external services
- **Trigger pipelines** via webhooks

### Setting Up Outgoing Webhooks

#### Step 1: Configure Webhook in GitLab

1. Go to **Settings → Webhooks**
2. Click **"Add webhook"**
3. Configure webhook:

**URL:** `https://your-external-service.com/webhook`

**Trigger:**
- ✅ Push events
- ✅ Tag push events
- ✅ Merge request events
- ✅ Comments

**Secret token:** (optional) Generate a secure token

4. Click **"Add webhook"**

#### Step 2: Test Webhook

1. Click **"Test"** dropdown
2. Select **"Push events"**
3. Check webhook delivery status

### Setting Up Incoming Webhooks (Trigger Pipeline)

#### Method 1: Using GitLab API

Create a webhook endpoint that triggers pipeline:

```bash
# Trigger pipeline via API
curl -X POST \
  -F token=YOUR_PROJECT_TOKEN \
  -F ref=main \
  https://gitlab.com/api/v4/projects/PROJECT_ID/trigger/pipeline
```

#### Method 2: Using Pipeline Triggers

1. Go to **Settings → CI/CD → Pipeline triggers**
2. Click **"Add trigger"**
3. Copy the **trigger token**
4. Use in webhook:

```yaml
# External service calls this URL
POST https://gitlab.com/api/v4/projects/PROJECT_ID/trigger/pipeline
Headers:
  Content-Type: application/json
Body:
{
  "token": "YOUR_TRIGGER_TOKEN",
  "ref": "main",
  "variables": {
    "DEPLOY_ENV": "production"
  }
}
```

### Webhook Security

#### Using Secret Tokens

```yaml
# In .gitlab-ci.yml
trigger-job:
  script:
    - |
      if [ "$HTTP_X_GITLAB_TOKEN" != "$WEBHOOK_SECRET_TOKEN" ]; then
        echo "Unauthorized"
        exit 1
      fi
      # Process webhook
```

#### Verify Webhook Source

```bash
# Verify GitLab webhook signature
# GitLab sends X-Gitlab-Token header
if [ "$HTTP_X_GITLAB_TOKEN" = "$EXPECTED_TOKEN" ]; then
  echo "Valid webhook"
fi
```

---

## Pipeline Stages

### Stage 1: Build

**Purpose:** Compile and package the application

```yaml
build:
  stage: build
  image: gradle:7.6-jdk21
  script:
    - ./gradlew clean build -x test
  artifacts:
    paths:
      - build/libs/*.jar
    expire_in: 1 week
```

### Stage 2: Test

**Purpose:** Run automated tests

```yaml
test:
  stage: test
  image: gradle:7.6-jdk21
  services:
    - postgres:15
  variables:
    POSTGRES_DB: webstore_test
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
  script:
    - ./gradlew test
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit: build/test-results/test/*.xml
```

### Stage 3: Code Quality

**Purpose:** Static code analysis

```yaml
code-quality:
  stage: test
  image: gradle:7.6-jdk21
  script:
    - ./gradlew checkstyleMain pmdMain
  artifacts:
    paths:
      - build/reports/checkstyle/
      - build/reports/pmd/
  allow_failure: true
```

### Stage 4: Deploy

**Purpose:** Deploy to target environment

```yaml
deploy:
  stage: deploy
  script:
    - echo "Deploying application..."
    - ./deploy.sh
  environment:
    name: production
    url: https://webstore.com
  only:
    - main
```

---

## Environment Variables

### Setting Up CI/CD Variables

1. Go to **Settings → CI/CD → Variables**
2. Click **"Add variable"**
3. Add required variables:

#### Required Variables

| Variable | Description | Protected | Masked |
|----------|-------------|-----------|--------|
| `SSH_PRIVATE_KEY` | SSH key for server access | ✅ | ✅ |
| `STAGING_SERVER` | Staging server hostname | ✅ | ❌ |
| `STAGING_USER` | Staging server username | ✅ | ❌ |
| `PRODUCTION_SERVER` | Production server hostname | ✅ | ❌ |
| `PRODUCTION_USER` | Production server username | ✅ | ❌ |
| `DATABASE_PASSWORD` | Database password | ✅ | ✅ |
| `JWT_SECRET` | JWT secret key | ✅ | ✅ |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID | ✅ | ❌ |

### Using Variables in Pipeline

```yaml
deploy:
  script:
    - echo "Deploying to $STAGING_SERVER"
    - ssh $STAGING_USER@$STAGING_SERVER "deploy.sh"
  variables:
    DEPLOY_ENV: "staging"
```

### Predefined Variables

GitLab provides many predefined variables:

| Variable | Description |
|----------|-------------|
| `CI_COMMIT_REF_NAME` | Branch or tag name |
| `CI_COMMIT_SHA` | Commit SHA |
| `CI_PROJECT_ID` | Project ID |
| `CI_PIPELINE_ID` | Pipeline ID |
| `CI_JOB_NAME` | Job name |
| `CI_REGISTRY` | Container registry URL |

---

## Deployment Strategies

### Strategy 1: Direct SSH Deployment

```yaml
deploy:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
    - ssh-keyscan -H $DEPLOY_SERVER >> ~/.ssh/known_hosts
  script:
    - scp build/libs/*.jar $DEPLOY_USER@$DEPLOY_SERVER:/opt/webstore/
    - ssh $DEPLOY_USER@$DEPLOY_SERVER "systemctl restart webstore"
  only:
    - main
```

### Strategy 2: Docker Deployment

```yaml
deploy:
  stage: deploy
  image: docker:24
  services:
    - docker:24-dind
  script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
    - docker pull $CI_REGISTRY_IMAGE:latest
    - docker stop webstore || true
    - docker rm webstore || true
    - docker run -d --name webstore -p 8080:8080 $CI_REGISTRY_IMAGE:latest
  only:
    - main
```

### Strategy 3: Kubernetes Deployment

```yaml
deploy:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl set image deployment/webstore webstore=$CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
    - kubectl rollout status deployment/webstore
  only:
    - main
```

### Strategy 4: Blue-Green Deployment

```yaml
deploy-blue:
  stage: deploy
  script:
    - docker run -d --name webstore-blue -p 8081:8080 $IMAGE_TAG
    - ./health-check.sh http://localhost:8081
  environment:
    name: blue
  only:
    - main

deploy-green:
  stage: deploy
  script:
    - docker stop webstore-blue || true
    - docker run -d --name webstore-green -p 8080:8080 $IMAGE_TAG
  environment:
    name: green
  only:
    - main
```

---

## Troubleshooting

### Common Issues

#### 1. Pipeline Not Triggering

**Problem:** Pipeline doesn't start on push

**Solutions:**
- Check `.gitlab-ci.yml` syntax (use GitLab CI/CD Lint)
- Verify runner is available
- Check branch protection rules
- Verify webhook configuration

#### 2. Runner Not Available

**Problem:** Jobs stuck in "pending" state

**Solutions:**
- Go to **Settings → CI/CD → Runners**
- Check runner status
- Verify runner tags match job tags
- Check runner executor configuration

#### 3. Build Failures

**Problem:** Build job fails

**Solutions:**
- Check build logs
- Verify dependencies are available
- Check Java/Gradle version compatibility
- Verify build.gradle configuration

#### 4. Test Failures

**Problem:** Tests fail in CI but pass locally

**Solutions:**
- Check database configuration
- Verify environment variables
- Check test data setup
- Review test isolation

#### 5. Deployment Failures

**Problem:** Deployment job fails

**Solutions:**
- Verify SSH keys are correct
- Check server connectivity
- Verify deployment scripts
- Check file permissions

### Debugging Tips

#### Enable Debug Logging

```yaml
variables:
  GRADLE_OPTS: "-Dorg.gradle.debug=true --stacktrace"
  LOG_LEVEL: "DEBUG"
```

#### View Job Logs

1. Go to **CI/CD → Pipelines**
2. Click on pipeline
3. Click on failed job
4. Review job logs

#### Test Locally

```bash
# Install GitLab Runner locally
# Test pipeline locally
gitlab-runner exec docker build
gitlab-runner exec docker test
```

---

## Best Practices

### 1. Pipeline Optimization

- **Use caching:**
```yaml
cache:
  paths:
    - .gradle/
    - build/
  key: $CI_COMMIT_REF_SLUG
```

- **Parallel jobs:**
```yaml
test-unit:
  stage: test
  parallel: 3
  script:
    - ./gradlew test --tests "TestClass$CI_NODE_INDEX"
```

- **Use artifacts efficiently:**
```yaml
artifacts:
  paths:
    - build/libs/*.jar
  expire_in: 1 week
  when: on_success
```

### 2. Security

- ✅ Use protected variables for secrets
- ✅ Mask sensitive values in logs
- ✅ Use SSH keys instead of passwords
- ✅ Rotate tokens regularly
- ✅ Limit access to production deployments

### 3. Branch Strategy

```yaml
# Only run on specific branches
only:
  - main
  - develop
  - /^release\/.*$/

# Exclude branches
except:
  - tags
```

### 4. Conditional Execution

```yaml
deploy:
  script:
    - ./deploy.sh
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
      when: manual
    - if: $CI_COMMIT_BRANCH == "develop"
      when: on_success
```

### 5. Notifications

```yaml
deploy:
  script:
    - ./deploy.sh
  after_script:
    - |
      if [ $CI_JOB_STATUS == "success" ]; then
        curl -X POST $SLACK_WEBHOOK_URL -d '{"text":"Deployment successful"}'
      else
        curl -X POST $SLACK_WEBHOOK_URL -d '{"text":"Deployment failed"}'
      fi
```

### 6. Rollback Strategy

```yaml
rollback:
  stage: deploy
  script:
    - docker stop webstore
    - docker run -d --name webstore -p 8080:8080 $PREVIOUS_IMAGE_TAG
  when: manual
  only:
    - main
```

---

## Complete Example: WebStore CI/CD Pipeline

```yaml
# .gitlab-ci.yml - Complete WebStore Pipeline

stages:
  - build
  - test
  - quality
  - package
  - deploy

variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"
  DOCKER_IMAGE: $CI_REGISTRY_IMAGE
  DOCKER_TAG: $CI_COMMIT_REF_SLUG

cache:
  paths:
    - .gradle/
  key: $CI_COMMIT_REF_SLUG

# Build Stage
build:
  stage: build
  image: gradle:7.6-jdk21
  script:
    - echo "Building WebStore application..."
    - ./gradlew clean build -x test
  artifacts:
    paths:
      - build/libs/*.jar
    expire_in: 1 week
  only:
    - main
    - develop
    - merge_requests

# Test Stage
test:
  stage: test
  image: gradle:7.6-jdk21
  services:
    - postgres:15
  variables:
    POSTGRES_DB: webstore_test
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
    DATABASE_URL: "postgresql://postgres:postgres@postgres:5432/webstore_test"
  script:
    - echo "Running tests..."
    - ./gradlew test
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    reports:
      junit: build/test-results/test/*.xml
    paths:
      - build/reports/tests/
    expire_in: 1 week
  only:
    - main
    - develop
    - merge_requests

# Code Quality Stage
code-quality:
  stage: quality
  image: gradle:7.6-jdk21
  script:
    - echo "Running code quality checks..."
    - ./gradlew checkstyleMain checkstyleTest
    - ./gradlew pmdMain pmdTest
  artifacts:
    paths:
      - build/reports/checkstyle/
      - build/reports/pmd/
    expire_in: 1 week
  allow_failure: true
  only:
    - main
    - develop
    - merge_requests

# Package Docker Image
package:
  stage: package
  image: docker:24
  services:
    - docker:24-dind
  before_script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
  script:
    - echo "Building Docker image..."
    - docker build -t $DOCKER_IMAGE:$DOCKER_TAG -t $DOCKER_IMAGE:latest .
    - docker push $DOCKER_IMAGE:$DOCKER_TAG
    - docker push $DOCKER_IMAGE:latest
  only:
    - main
    - develop

# Deploy to Staging
deploy-staging:
  stage: deploy
  image: docker:24
  services:
    - docker:24-dind
  before_script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
  script:
    - echo "Deploying to staging..."
    - docker pull $DOCKER_IMAGE:develop
    - docker stop webstore-staging || true
    - docker rm webstore-staging || true
    - docker run -d --name webstore-staging -p 8080:8080 --env-file .env.staging $DOCKER_IMAGE:develop
  environment:
    name: staging
    url: http://staging.webstore.com:8080
  only:
    - develop
  when: on_success

# Deploy to Production
deploy-production:
  stage: deploy
  image: docker:24
  services:
    - docker:24-dind
  before_script:
    - docker login -u $CI_REGISTRY_USER -p $CI_REGISTRY_PASSWORD $CI_REGISTRY
  script:
    - echo "Deploying to production..."
    - docker pull $DOCKER_IMAGE:main
    - docker stop webstore-production || true
    - docker rm webstore-production || true
    - docker run -d --name webstore-production -p 8080:8080 --env-file .env.production $DOCKER_IMAGE:main
  environment:
    name: production
    url: https://webstore.com
  only:
    - main
  when: manual
```

---

## Webhook Trigger Examples

### Example 1: External Service Triggers Pipeline

```bash
# External service calls this to trigger pipeline
curl -X POST \
  -F token=YOUR_TRIGGER_TOKEN \
  -F ref=main \
  -F "variables[DEPLOY_ENV]=production" \
  https://gitlab.com/api/v4/projects/PROJECT_ID/trigger/pipeline
```

### Example 2: GitHub Webhook Triggers GitLab Pipeline

1. In GitHub repository, go to **Settings → Webhooks**
2. Add webhook URL: `https://gitlab.com/api/v4/projects/PROJECT_ID/trigger/pipeline`
3. Set secret token
4. Select events: Push, Pull Request

### Example 3: Slack Command Triggers Deployment

```yaml
# In .gitlab-ci.yml
deploy-on-demand:
  stage: deploy
  script:
    - ./deploy.sh
  rules:
    - if: $CI_PIPELINE_SOURCE == "trigger"
      when: manual
```

---

## Monitoring & Notifications

### Slack Integration

1. Go to **Settings → Integrations → Slack**
2. Configure webhook URL
3. Select events to notify

### Email Notifications

Configure in **Settings → Notifications**

### Pipeline Status Badge

Add to README.md:

```markdown
![pipeline status](https://gitlab.com/username/webstore/badges/main/pipeline.svg)
```

---

## Summary

### Quick Setup Checklist

- [ ] Create GitLab project
- [ ] Push code to repository
- [ ] Create `.gitlab-ci.yml` file
- [ ] Configure environment variables
- [ ] Set up runners (or use shared runners)
- [ ] Configure webhooks (if needed)
- [ ] Test pipeline with a push
- [ ] Set up deployment targets
- [ ] Configure notifications

### Key Takeaways

1. **CI/CD automates** build, test, and deployment
2. **Webhooks** can trigger pipelines from external events
3. **Environment variables** secure sensitive data
4. **Stages** organize pipeline execution
5. **Artifacts** pass data between jobs
6. **Manual deployments** add safety for production

---

## References

- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- [GitLab CI/CD YAML Reference](https://docs.gitlab.com/ee/ci/yaml/)
- [GitLab Runners](https://docs.gitlab.com/runner/)
- [GitLab Webhooks](https://docs.gitlab.com/ee/user/project/integrations/webhooks.html)
- [Docker Documentation](https://docs.docker.com/)

---

**Document Version:** 1.0  
**Last Updated:** January 2024  
**Author:** WebStore Development Team

---

*This guide provides a comprehensive overview of setting up CI/CD pipelines in GitLab. For specific use cases, refer to GitLab's official documentation.*

