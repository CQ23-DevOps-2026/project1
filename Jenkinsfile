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

// ─────────────────────────────────────────────────────────────────────────────
// PIPELINE
// ─────────────────────────────────────────────────────────────────────────────
pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk   'Java21'
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

                    if (services.isEmpty()) {
                        echo '=> Không phát hiện service nào thay đổi → Test & Build sẽ bị bỏ qua.'
                    } else {
                        echo "=> Services sẽ được xử lý: ${env.CHANGED_SERVICES}"
                    }
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
        // STAGE 4: Build
        // Req 5: Phase Build — đóng gói JAR, bỏ qua test
        // ==============================================================
        stage('Build') {
            when {
                expression { env.CHANGED_SERVICES != null && env.CHANGED_SERVICES != '' }
            }
            steps {
                script {
                    def services        = env.CHANGED_SERVICES.split(',').toList()
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