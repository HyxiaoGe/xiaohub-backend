pipeline {
    agent any

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
                echo 'Checking Java and Maven versions...'
                sh 'java -version'
                sh 'mvn -v'
            }
        }
//         stage('Deployment') { test
//             steps {
//                 echo 'Deploying to Nginx directory...'
//                 sh 'rm -rf /docker/nginx/data/html/xiaohub/*'  // 清理旧文件
//                 sh 'cp -r dist/* /docker/nginx/data/html/xiaohub/'  // 复制新文件
//                 sh 'sudo docker restart nginx'  // 重载 Nginx
//             }
//         }
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