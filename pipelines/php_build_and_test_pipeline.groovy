pipeline {
    agent {
        label 'php-agent'
    }
    
    environment {
        PHP_PROJECT_REPO = 'https://github.com/OhKangWoo/containers05.git'
        PHP_PROJECT_BRANCH = 'main'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning PHP project repository...'
                git branch: "${PHP_PROJECT_BRANCH}", 
                    url: "${PHP_PROJECT_REPO}"
            }
        }
        
        stage('Install Dependencies') {
            steps {
                echo 'Installing Composer dependencies...'
                sh 'composer install --no-interaction --prefer-dist --optimize-autoloader'
            }
        }
        
        stage('Run Unit Tests') {
            steps {
                echo 'Running PHPUnit tests...'
                sh 'vendor/bin/phpunit --testdox --log-junit reports/phpunit.xml'
            }
        }
        
        stage('Report Test Results') {
            steps {
                echo 'Publishing test results...'
                junit 'reports/phpunit.xml'
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed.'
            cleanWs()
        }
        success {
            echo 'All tests passed successfully!'
        }
        failure {
            echo 'Tests failed or errors detected in the pipeline.'
        }
    }
}