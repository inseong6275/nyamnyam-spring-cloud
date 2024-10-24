pipeline {
    agent any

    environment {
        DOCKER_CREDENTIALS_ID = 'inseonghwang0328'
        DOCKER_IMAGE_PREFIX = 'inseonghwang0328/nyamnyam-config-server'
        services = "server/config-server,server/eureka-server,server/gateway-server,service/admin-service,service/chat-service,service/post-service,service/restaurant-service,service/user-service"
        DOCKERHUB_CREDENTIALS = credentials('dockerHubId')
        KUBECONFIG_CREDENTIALS_ID = 'kubeconfig'
        NCP_API_KEY = credentials('ncloud-api-key')
        NCP_SECRET_KEY = credentials('ncloud-secret-key')
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

        stage('Create Namespace') { // 네임스페이스 생성 단계 추가
            steps {
                script {
                 withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                    // 'nyamnyam-namespace.yaml' 파일을 사용해 네임스페이스 생성
                    sh '''
                    kubectl apply -f nyamnyam.kr/deploy/namespace/nyamnyam-namespace.yaml --kubeconfig=$KUBECONFIG
                    '''
                }
            }
        }

        stage('Git Clone') {
            steps {
                script {
                    sh 'pwd'

                    dir('nyamnyam.kr/deploy') {
                        git branch: 'main', url: 'https://github.com/inseong6275/deploy.git', credentialsId: 'gitHubAccessToken'
                    }

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

         stage('Create ConfigMap') {
             steps {
                 script {
                     withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                         // 각 서비스의 ConfigMap 생성
                         sh '''
                         kubectl create configmap config-server --from-file=nyamnyam.kr/server/config-server/src/main/resources/application.yaml -n nyamnyam --dry-run=client -o yaml | kubectl apply -f -
                         kubectl create configmap eureka-server --from-file=nyamnyam.kr/server/eureka-server/src/main/resources/application.yaml -n nyamnyam --dry-run=client -o yaml | kubectl apply -f -
                         kubectl create configmap gateway-server --from-file=nyamnyam.kr/server/gateway-server/src/main/resources/application.yaml -n nyamnyam --dry-run=client -o yaml | kubectl apply -f -
                         kubectl create configmap admin-service --from-file=nyamnyam.kr/service/admin-service/src/main/resources/application.yaml -n nyamnyam --dry-run=client -o yaml | kubectl apply -f -
                         kubectl create configmap chat-service --from-file=nyamnyam.kr/service/chat-service/src/main/resources/application.yaml -n nyamnyam --dry-run=client -o yaml | kubectl apply -f -
                         kubectl create configmap post-service --from-file=nyamnyam.kr/service/post-service/src/main/resources/application.yaml -n nyamnyam --dry-run=client -o yaml | kubectl apply -f -
                         kubectl create configmap restaurant-service --from-file=nyamnyam.kr/service/restaurant-service/src/main/resources/application.yaml -n nyamnyam --dry-run=client -o yaml | kubectl apply -f -
                         kubectl create configmap user-service --from-file=nyamnyam.kr/service/user-service/src/main/resources/application.yaml -n nyamnyam --dry-run=client -o yaml | kubectl apply -f -
                         '''
                     }
                 }
             }
         }


        stage('Deploy to K8s') {
            steps {
               script {
                   withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                     // 환경 변수로 API Key와 Secret Key 설정 후 ncp-iam-authenticator에 전달
                       sh '''
                       export NCP_ACCESS_KEY=$NCP_API_KEY
                       export NCP_SECRET_KEY=$NCP_SECRET_KEY
                       export TOKEN=$(ncp-iam-authenticator token --clusterUuid f67e3902-54be-44a9-be88-e18e477991d9 --region KR)

                       kubectl apply -f nyamnyam.kr/deploy/was/config-server/config-server.yaml --kubeconfig=$KUBECONFIG --token=$TOKEN
                       kubectl apply -f nyamnyam.kr/deploy/was/eureka-server/eureka-server.yaml --kubeconfig=$KUBECONFIG --token=$TOKEN
                       kubectl apply -f nyamnyam.kr/deploy/was/gateway-server/gateway-server.yaml --kubeconfig=$KUBECONFIG --token=$TOKEN
                       kubectl apply -f nyamnyam.kr/deploy/was/admin-service/admin-service.yaml --kubeconfig=$KUBECONFIG --token=$TOKEN
                       kubectl apply -f nyamnyam.kr/deploy/was/chat-service/chat-service.yaml --kubeconfig=$KUBECONFIG --token=$TOKEN
                       kubectl apply -f nyamnyam.kr/deploy/was/post-service/post-service.yaml --kubeconfig=$KUBECONFIG --token=$TOKEN
                       kubectl apply -f nyamnyam.kr/deploy/was/restaurant-service/restaurant-service.yaml --kubeconfig=$KUBECONFIG --token=$TOKEN
                       kubectl apply -f nyamnyam.kr/deploy/was/user-service/user-service.yaml --kubeconfig=$KUBECONFIG --token=$TOKEN
                       '''
               }
             }
        }
    }
  }
}
