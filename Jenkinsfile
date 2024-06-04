pipeline {
    agent any

    tools {
            maven 'Maven'
            jdk 'OpenJDK 11'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    // 使用 retry 和 timeout 包裹 Git 命令
                    retry(3) {  // 如果命令失败，最多重试3次
                        timeout(time: 5, unit: 'MINUTES') {  // 单次尝试最大持续5分钟
                            git branch: 'master',
                                url: 'https://github.com/HyxiaoGe/xiaohub-backend.git',
                                credentialsId: 'login_credentials'
                        }
                    }
                }
            }
        }
        stage('Prepare') {
            steps {
                echo 'prepare necessary environment...'
            }
        }
        stage('Clean') {
            steps {
                echo 'clean maven cache...'
                sh 'mvn clean'  // 执行 Maven 清除命令
            }
        }
        stage('Install') {
            steps {
                echo 'install maven dependencies...'
                sh 'mvn install -DskipTests'  // 执行 Maven 安装命令
            }
        }
        stage('Rename JAR') {
            steps {
                sh 'mv target/xiaohub-backend-1.0-SNAPSHOT-shaded.jar target/xiaohub.jar'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying to xiaohub directory...'
                sh 'if [ -f /home/xiaohub/xiaohub.jar ]; then rm -rf /home/xiaohub/xiaohub.jar; fi'  // 仅在文件存在时删除
                sh 'cp target/xiaohub.jar /home/xiaohub/xiaohub.jar'  // 复制新文件
                sh 'chmod +x /home/xiaohub/restart.sh'  // 确保脚本具有执行权限
                sh '/home/xiaohub/restart.sh'  // 执行重启脚本
            }
        }
    }
    post {
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}