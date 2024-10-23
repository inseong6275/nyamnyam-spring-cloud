pipeline {
    agent any

    environment {
        DOCKER_CREDENTIALS_ID = 'inseonghwang0328'
        DOCKER_IMAGE_PREFIX = 'inseonghwang0328/nyamnyam-config-server'
        services = "server/config-server,server/eureka-server,server/gateway-server,service/admin-service,service/chat-service,service/post-service,service/restaurant-service,service/user-service"
        DOCKERHUB_CREDENTIALS = credentials('dockerHubId')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                script {
                    dir('nyamnyam.kr') {
                        checkout scm
                    }
                }
            }
        }

        stage('Git Clone') {
            steps {
                script {
                    dir('nyamnyam.kr/server/config-server') {
                        git branch: 'main', url: 'https://github.com/inseong6275/nyamnyam-config-server.git', credentialsId: 'gitHubAccessToken'
                    }

                    dir ('nyamnyam.kr/server/config-server/src/main/resources/secret-server') {
                        git branch: 'main', url: 'https://github.com/inseong6275/nyamnyam-secret-server.git', credentialsId: 'gitHubAccessToken'
                    }
                }
            }
        }

        stage('Build JAR') {
            steps {
                script {
                    dir('nyamnyam.kr') {
                        sh 'chmod +x gradlew'

                        def servicesList = env.services.split(',')

                        servicesList.each { service ->
                            dir(service) {
                                sh "../../gradlew clean bootJar"
                            }
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    dir('nyamnyam.kr') {
                        sh "cd server/config-server && docker build -t ${DOCKER_CREDENTIALS_ID}/nyamnyam-config-server:latest ."
                    }

                    dir('nyamnyam.kr') {
                        sh "docker-compose build"
                    }
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                sh '''
                echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin
                '''
            }
        }

        stage('Docker Push') {
            steps {
                script {
                    def servicesList = env.services.split(',')

                    servicesList.each { service ->
                        def serviceName = service.split('/')[1] // 서비스 이름 추출
                        // 각 서비스의 Docker 이미지를 푸시
                        sh "docker push ${DOCKER_CREDENTIALS_ID}/nyamnyam-${serviceName}:latest"
                    }
                }
            }
        }

        stage('Cleaning up') {
            steps {
                script {
                    // 각 서비스의 이미지 삭제
                    def servicesList = env.services.split(',')
                    servicesList.each { service ->
                        def serviceName = service.split('/')[1] // 서비스 이름 추출
                        sh "docker rmi ${DOCKER_CREDENTIALS_ID}/nyamnyam-${serviceName}:latest"
                    }
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                script {
                    // deploy.yaml 파일 수정
                    sh "sed -i 's,TEST_IMAGE_NAME,${DOCKER_IMAGE_PREFIX}:latest,' deploy.yaml"
                    sh "cat deploy.yaml"
                    // Kubernetes에서 현재 Pod 상태 확인
                    sh "kubectl --kubeconfig=/home/ec2-user/config get pods"
                    // deploy.yaml을 Kubernetes에 적용
                    sh "kubectl --kubeconfig=/home/ec2-user/config apply -f deploy.yaml"
                }
            }
        }
    }
}
