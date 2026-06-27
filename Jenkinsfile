// ─── NonCPS: thu thập tất cả file thay đổi từ Jenkins changeSets ─────────────
@com.cloudbees.groovy.cps.NonCPS
def getAffectedPaths() {
    def paths = []
    for (changeSet in currentBuild.changeSets) {
        for (entry in changeSet.items) {
            for (file in entry.affectedFiles) {
                paths.add(file.path)
            }
        }
    }
    return paths
}

// ─── NonCPS: lấy các thư mục top-level từ danh sách path ────────────────────
@com.cloudbees.groovy.cps.NonCPS
def extractUniqueFolders(List paths) {
    def folders = [] as Set
    for (path in paths) {
        if (path.contains('/')) {
            folders.add(path.split('/')[0])
        }
    }
    return folders.toList()
}

// ─── Detect changed Maven services — TRẢ VỀ sorted List<String> ─────────────
def getChangedServices() {
    def gitDiffOutput = ''
    try {
        sh(script: 'git fetch --no-tags --prune --depth=1 origin +refs/heads/main:refs/remotes/origin/main',
           returnStdout: false)
        gitDiffOutput = sh(
            script: 'git diff --name-only origin/main...HEAD',
            returnStdout: true
        ).trim()
    } catch (e) {
        echo "git diff thất bại, dùng changeSets làm fallback: ${e.message}"
    }

    def paths = gitDiffOutput
        ? gitDiffOutput.split('\n').toList()
        : getAffectedPaths()

    def changedServices = [] as Set
    for (folder in extractUniqueFolders(paths)) {
        if (fileExists("${folder}/pom.xml")) {
            changedServices.add(folder)
        }
    }
    return changedServices.toList().sort()
}

// ─── Detect all Docker image services for main baseline builds ───────────────
def getAllDockerServices() {
    def output = sh(
        script: "find . -maxdepth 2 -name Dockerfile | sed 's#^./##; s#/Dockerfile##' | sort",
        returnStdout: true
    ).trim()

    return output ? output.split('\n').toList() : []
}

def dockerImageNameForService(String service) {
    if (service == 'backoffice') {
        return 'yas-backoffice'
    }
    if (service == 'storefront') {
        return 'yas-storefront'
    }
    return "yas-${service}"
}

def isMainBranch() {
    def branchName = env.BRANCH_NAME ?: ''
    def gitBranch = env.GIT_BRANCH ?: ''
    return branchName == 'main' || gitBranch == 'main' || gitBranch == 'origin/main'
}

// ─────────────────────────────────────────────────────────────────────────────
// PIPELINE
// ─────────────────────────────────────────────────────────────────────────────
pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk   'Java21'
    }

    environment {
        // Jenkins Credentials (Secret text)
        // - snyk-token: Snyk API token
        SNYK_TOKEN  = credentials('snyk-token')
        DOCKERHUB_NAMESPACE = 'vinny2707'
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        // ==============================================================
        // STAGE 1: Checkout
        // Req 4: Jenkins chạy pipeline cho từng branch
        // ==============================================================
        stage('Checkout') {
            steps {
                checkout scm
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        // ==============================================================
        // STAGE 1.5: Secrets scan (Gitleaks)
        // - Dùng gitleaks.toml ở root
        // - Report-only: không đánh rớt pipeline, chỉ đánh dấu UNSTABLE + xuất report
        // ==============================================================
        stage('Security: Gitleaks') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh '''
                    set -euo pipefail

                    TOOLS_DIR="${WORKSPACE:-.}/.tools/bin"
                    mkdir -p "$TOOLS_DIR"

                    if [ ! -x "$TOOLS_DIR/gitleaks" ]; then
                      echo "=> Installing gitleaks locally..."
                      OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
                      ARCH="$(uname -m)"
                      case "$ARCH" in
                        x86_64|amd64) ARCH="x64" ;;
                        arm64|aarch64) ARCH="arm64" ;;
                        *) echo "Unsupported arch: $ARCH"; exit 2 ;;
                      esac

                      VERSION="8.21.2"
                      TARBALL="gitleaks_${VERSION}_${OS}_${ARCH}.tar.gz"
                      URL="https://github.com/gitleaks/gitleaks/releases/download/v${VERSION}/${TARBALL}"

                      curl -sSfL "$URL" -o "${WORKSPACE:-.}/.tools/${TARBALL}"
                      tar -xzf "${WORKSPACE:-.}/.tools/${TARBALL}" -C "$TOOLS_DIR" gitleaks
                      chmod +x "$TOOLS_DIR/gitleaks"
                    fi

                    export PATH="$TOOLS_DIR:$PATH"
                    gitleaks version

                    # Scan working tree (không scan git history). Nếu muốn scan commits: dùng `gitleaks detect --log-opts origin/main...HEAD`.
                    gitleaks detect \
                      --config="gitleaks.toml" \
                      --source="." \
                      --no-git \
                      --redact \
                      --report-format sarif \
                      --report-path "gitleaks.sarif" \
                      --exit-code 1
                '''
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'gitleaks.sarif', allowEmptyArchive: true
                    // Requires Jenkins plugin: Warnings Next Generation (Warnings NG)
                    // Shows results in "SARIF Warnings" UI.
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        recordIssues(tools: [sarif(pattern: 'gitleaks.sarif')])
                    }
                }
            }
        }

        // ==============================================================
        // STAGE 2: Detect Changed Services
        // Req 6: Monorepo — chỉ build/test service có thay đổi
        // → Gọi getChangedServices() MỘT LẦN, lưu vào env.CHANGED_SERVICES
        //   để tránh chạy lại git fetch/diff ở mỗi stage
        // ==============================================================
        stage('Detect Changed Services') {
            steps {
                script {
                    def services = getChangedServices()
                    env.CHANGED_SERVICES = services.join(',')

                    if (isMainBranch()) {
                        def imageServices = getAllDockerServices()
                        def mavenBuildServices = imageServices.findAll { fileExists("${it}/pom.xml") }

                        env.IMAGE_SERVICES = imageServices.join(',')
                        env.MAVEN_BUILD_SERVICES = mavenBuildServices.join(',')
                        env.IMAGE_TAG = 'main'

                        echo '=> Main branch detected: baseline Docker images will be built with tag main.'
                    } else {
                        env.IMAGE_SERVICES = env.CHANGED_SERVICES
                        env.MAVEN_BUILD_SERVICES = env.CHANGED_SERVICES
                        env.IMAGE_TAG = sh(script: 'git rev-parse --short=8 HEAD', returnStdout: true).trim()
                        echo "=> Feature branch detected: changed Docker images will be built with tag ${env.IMAGE_TAG}."
                    }

                    if (services.isEmpty()) {
                        echo '=> Không phát hiện service nào thay đổi → Test & Build sẽ bị bỏ qua.'
                    } else {
                        echo "=> Services sẽ được xử lý: ${env.CHANGED_SERVICES}"
                    }

                    echo "=> Maven build services: ${env.MAVEN_BUILD_SERVICES ?: '<none>'}"
                    echo "=> Docker image services: ${env.IMAGE_SERVICES ?: '<none>'}"
                    echo "=> Docker image tag: ${env.IMAGE_TAG}"
                }
            }
        }

        // ==============================================================
        // STAGE 3: Test & Coverage
        // Req 5: Phase Test — chạy unit test, sinh báo cáo JaCoCo
        //   - mvn clean test   → compile + chạy test (JaCoCo agent tự inject)
        //   - jacoco:report    → sinh XML/HTML coverage
        //   - -pl/-am          → chỉ build service thay đổi + dependencies
        //   - Loại trừ IT tests (cần infrastructure: Keycloak, DB...)
        // ==============================================================
        stage('Test & Coverage') {
            when {
                // Bỏ qua nếu không có service nào thay đổi
                expression { env.CHANGED_SERVICES != null && env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def services        = env.CHANGED_SERVICES.split(',').toList()
                    def serviceSelector = services.join(',')
                    echo "=> [Test] Chạy unit test cho: ${serviceSelector}"

                    // FIX: dùng triple-quote + escape \$ để tránh lỗi shell quoting
                    sh """
                        mvn clean verify \
                            -pl '${serviceSelector}' \
                            -am \
                            -DskipITs \
                            -Dsurefire.excludes='**/*IT.java,**/*IT\$*.java,**/ProductCdcConsumerTest.java,**/ProductVectorRepositoryTest.java,**/VectorQueryTest.java'
                    """
                }
            }
            post {
                always {
                    // Upload JUnit XML → hiển thị kết quả test trên Jenkins UI
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

                    // Upload JaCoCo coverage → hiển thị biểu đồ coverage
                    // Yêu cầu Jenkins plugin: JaCoCo Plugin
                    script {
                        if (env.CHANGED_SERVICES) {
                            def services       = env.CHANGED_SERVICES.split(',').toList()
                            def execPatterns   = services.collect { "${it}/target/jacoco.exec" }.join(',')
                            def classPatterns  = services.collect { "${it}/target/classes" }.join(',')
                            def sourcePatterns = services.collect { "${it}/src/main/java" }.join(',')

                            jacoco(
                                execPattern:        execPatterns,
                                classPattern:       classPatterns,
                                sourcePattern:      sourcePatterns,
                                // ── Req: fail build nếu line coverage < 70% ──
                                minimumLineCoverage: '70',
                                minimumBranchCoverage: '70',
                                changeBuildStatus:   true
                            )

                            if (currentBuild.result == 'UNSTABLE') {
                                error("Coverage < 70% (line/branch) cho: ${env.CHANGED_SERVICES}. Build FAIL.")
                            }
                        }
                    }
                }
            }
        }

        // ==============================================================
        // STAGE 3.5: SonarCloud/SonarQube analysis (Maven)
        // - Dùng cấu hình sonar.* trong root pom.xml
        // - Đẩy coverage JaCoCo XML (tìm tất cả jacoco.xml dưới các module đã build)
        // ==============================================================
        stage('Quality: Sonar (Maven)') {
            when {
                expression { env.CHANGED_SERVICES != null && env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def services        = env.CHANGED_SERVICES.split(',').toList()
                    def serviceSelector = services.join(',')

                                        // Sonar token + URL được Jenkins tự inject từ cấu hình "SonarQube servers".
                                        def sonarServerName = 'Sonar-Server'

                                        withSonarQubeEnv(sonarServerName) {
                                                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                                                sh '''
                                                        set -euo pipefail

                                                        # Collect JaCoCo XML reports generated in previous stage (only changed modules).
                                                        JACOCO_XML_PATHS="$(echo "${CHANGED_SERVICES}" | tr ',' '\n' | while read -r svc; do
                                                            [ -f "${svc}/target/site/jacoco/jacoco.xml" ] && printf '%s\n' "${svc}/target/site/jacoco/jacoco.xml" || true
                                                        done | paste -sd, - || true)"
                                                        echo "=> JaCoCo XML paths: ${JACOCO_XML_PATHS:-<none>}"

                                                        # Note: sonar analysis needs compiled classes; align reactor with CHANGED_SERVICES.
                                                        mvn -DskipTests -DskipITs \
                                                            -pl "${CHANGED_SERVICES}" \
                                                            -am \
                                                            sonar:sonar \
                                                            -Dsonar.host.url="$SONAR_HOST_URL" \
                                                            -Dsonar.login="$SONAR_AUTH_TOKEN" \
                                                            ${JACOCO_XML_PATHS:+-Dsonar.coverage.jacoco.xmlReportPaths="$JACOCO_XML_PATHS"}
                                                '''
                                                }
                    }
                }
            }
        }

        // ==============================================================
        // STAGE 4: Build
        // Req 5: Phase Build — đóng gói JAR, bỏ qua test
        // ==============================================================
        stage('Build') {
            when {
                expression { env.MAVEN_BUILD_SERVICES != null && env.MAVEN_BUILD_SERVICES != '' }
            }
            steps {
                script {
                    def services        = env.MAVEN_BUILD_SERVICES.split(',').toList()
                    def serviceSelector = services.join(',')
                    echo "=> [Build] Đóng gói service: ${serviceSelector}"

                    sh """
                        mvn package \
                            -pl '${serviceSelector}' \
                            -am \
                            -DskipTests
                    """
                }
            }
        }

        // ==============================================================
        // STAGE 4.5: Docker build & push baseline images
        // - main branch: build all service Dockerfiles and push tag "main"
        // - Developer branch commit-id images will be added in the next phase
        // ==============================================================
        stage('Docker: Build & Push Images') {
            when {
                expression { env.IMAGE_SERVICES != null && env.IMAGE_SERVICES != '' }
            }
            steps {
                script {
                    def services = env.IMAGE_SERVICES.split(',').toList()

                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )]) {
                        sh '''
                            set -euo pipefail
                            echo "$DOCKERHUB_PASSWORD" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
                        '''

                        for (svc in services) {
                            def imageName = dockerImageNameForService(svc)
                            def fullImage = "${env.DOCKERHUB_NAMESPACE}/${imageName}:${env.IMAGE_TAG}"

                            sh """
                                set -euo pipefail
                                echo "=> Building ${fullImage}"
                                docker build -t '${fullImage}' './${svc}'
                                docker push '${fullImage}'
                            """
                        }

                        sh 'docker logout || true'
                    }
                }
            }
        }

        // ==============================================================
        // STAGE 5: Dependency & code scan (Snyk)
        // - Snyk CLI tự tải về theo OS/arch
        // - Report-only: không đánh rớt pipeline, chỉ đánh dấu UNSTABLE + xuất report
        // ==============================================================
        stage('Security: Snyk') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh '''
                        set -euo pipefail

                        TOOLS_DIR="${WORKSPACE:-.}/.tools/bin"
                        mkdir -p "$TOOLS_DIR"

                        if [ ! -x "$TOOLS_DIR/snyk" ]; then
                          echo "=> Installing snyk locally..."
                          OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
                          ARCH="$(uname -m)"
                          case "$ARCH" in
                            x86_64|amd64) ARCH="amd64" ;;
                            arm64|aarch64) ARCH="arm64" ;;
                            *) echo "Unsupported arch: $ARCH"; exit 2 ;;
                          esac

                          case "$OS" in
                            linux)  SNYK_URL="https://static.snyk.io/cli/latest/snyk-linux" ;;
                            darwin) SNYK_URL="https://static.snyk.io/cli/latest/snyk-macos" ;;
                            *) echo "Unsupported OS: $OS"; exit 2 ;;
                          esac

                          curl -sSfL "$SNYK_URL" -o "$TOOLS_DIR/snyk"
                          chmod +x "$TOOLS_DIR/snyk"
                        fi

                        export PATH="$TOOLS_DIR:$PATH"
                        snyk --version

                        export SNYK_TOKEN="$SNYK_TOKEN"
                        snyk auth "$SNYK_TOKEN" >/dev/null 2>&1 || true

                        # Dependency scan (Java/Maven). Exit code 1 => vulnerabilities found.
                        # catchError sẽ giữ build SUCCESS và đánh dấu stage UNSTABLE.
                        snyk test \
                          --all-projects \
                          --severity-threshold=high \
                          --sarif-file-output="snyk.sarif"
                    '''
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'snyk.sarif', allowEmptyArchive: true
                    // Requires Jenkins plugin: Warnings Next Generation (Warnings NG)
                    // Shows results in "SARIF Warnings" UI.
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        recordIssues(tools: [sarif(pattern: 'snyk.sarif')])
                    }
                }
            }
        }

    }

    post {
        always {
            script {
                echo "=> Pipeline kết thúc: ${currentBuild.result ?: 'SUCCESS'}"
                try {
                    cleanWs()
                    echo '=> Workspace đã được dọn dẹp.'
                } catch (e) {
                    echo "=> cleanWs() skipped: ${e.message}"
                }
            }
        }
        success {
            echo '=> ✅ Pipeline thành công!'
        }
        failure {
            echo '=> ❌ Pipeline THẤT BẠI! Kiểm tra log của stage bị lỗi.'
        }
    }
}
